DROP FUNCTION getroute(bigint,bigint);

CREATE OR REPLACE FUNCTION public.getroute(
    id1 bigint,
    id2 bigint)
  RETURNS text[] AS
$BODY$
DECLARE	
	result text[];
	edgenodes bigint[];
	linepoint text;
	line bigint;
	prevnode bigint;
BEGIN
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
				FOR i IN 1 .. array_upper(edgenodes, 1)
				LOOP
					select ST_AsText(geom) into linepoint from network_nodes where id = edgenodes[i];
					result := array_append(result, linepoint);
				END LOOP;
				prevnode := edgenodes[array_upper(edgenodes, 1)];
			else
				FOR i IN REVERSE  array_upper(edgenodes, 1) .. 1
				LOOP
					select ST_AsText(geom) into linepoint from network_nodes where id = edgenodes[i];
					result := array_append(result, linepoint);
				END LOOP;
				prevnode := edgenodes[1];
			end if;
		end if;		
	end loop;

	RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.getroute(bigint, bigint)
  OWNER TO postgres;


select * from getroute(282742858, 282742862);
