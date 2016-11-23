CREATE OR REPLACE FUNCTION array_reverse(anyarray) RETURNS anyarray AS $$
SELECT ARRAY(
    SELECT $1[i]
    FROM generate_subscripts($1,1) AS s(i)
    ORDER BY i DESC
);
$$ LANGUAGE 'sql' STRICT IMMUTABLE;
CREATE OR REPLACE FUNCTION public.getroute(
    id1 bigint,
    id2 bigint)
  RETURNS text[] AS
$BODY$
DECLARE	
	result text[];
	edgenodes bigint[];
	routenodes bigint[];
	linepoint text;
	line bigint;
	prevnode bigint;

		
BEGIN

	prevnode := id1;
	for line in SELECT edge FROM _pgr_dijkstra(
		'with bound as (
			select createboundingpolygon(' || id1 || '::bigint, ' || id2 || '::bigint) b
		)
		SELECT id::integer, 
		startp AS source, 
		endp AS target, 
		cost AS cost
		FROM network2, bound
		WHERE st_within(startpg, bound.b) or st_within(endpg, bound.b)',
		id1,
		id2,
		false,
		false) loop

		if line != -1 then
			select nodes into edgenodes from network2 where id=line;
			if edgenodes[1] = prevnode then
				routenodes := routenodes || edgenodes;
				prevnode := edgenodes[array_upper(edgenodes, 1)];
			else
				routenodes := routenodes || array_reverse(edgenodes);
				prevnode := edgenodes[1];
			end if;
		end if;		
	end loop;

	
	select array(
		SELECT ST_AsText(n.geom)||' '||n.id
		FROM network_nodes n 
		RIGHT JOIN unnest(routenodes) as v(id)
			ON (n.id=v.id)
	) into result;

	if array_length(result,1) > 0 then
		RETURN result;
	end if;

	prevnode := id1;
	for line in SELECT edge FROM _pgr_dijkstra(
		'SELECT id::integer, 
		startp AS source, 
		endp AS target, 
		cost AS cost
		FROM network2',
		id1,
		id2,
		false,
		false) loop

		if line != -1 then
			select nodes into edgenodes from network2 where id=line;
			if edgenodes[1] = prevnode then
				routenodes := routenodes || edgenodes;
				prevnode := edgenodes[array_upper(edgenodes, 1)];
			else
				routenodes := routenodes || array_reverse(edgenodes);
				prevnode := edgenodes[1];
			end if;
		end if;		
	end loop;

	
	select array(
		SELECT ST_AsText(n.geom)||' '||n.id
		FROM network_nodes n 
		RIGHT JOIN unnest(routenodes) as v(id)
			ON (n.id=v.id)
	) into result;

	return result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.getroute(bigint, bigint)
  OWNER TO postgres;

--------------------------------------------------------------------------

select * from public.getroute(300508392, 282742845); 
select nodes from network2 where id=92885;

SELECT * FROM _pgr_dijkstra(
		'with bound as (
			select createboundingpolygon(300508392::bigint, 282742845::bigint) b
		)
		SELECT id::integer, 
		startp AS source, 
		endp AS target, 
		cost AS cost
		FROM network2, bound
		WHERE st_within(startpg, bound.b) or st_within(endpg, bound.b)',
		300508392,
		282742845,
		false,
		false)

SELECT ST_AsText(geom) from network_nodes where id = 28874029
  