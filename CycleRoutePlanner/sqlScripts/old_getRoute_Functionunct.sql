DROP FUNCTION getroute(bigint,bigint);

CREATE OR REPLACE FUNCTION public.getroute(
    id1 bigint,
    id2 bigint)
  RETURNS text[] AS
$BODY$
DECLARE	
	result text[];
	linepoint text;
	line bigint;
BEGIN
	for line in SELECT node FROM _pgr_dijkstra(
		'SELECT id::integer, 
		startp AS source, 
		endp AS target, 
		cost AS cost
		FROM network',
		id1,
		id2,
		false,
		false) loop

	select ST_AsText(geom) into linepoint from network_nodes where id = line;

	result := array_append(result, linepoint);
	end loop;

	RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.getroute(bigint, bigint)
  OWNER TO postgres;

select * from getroute(282742858, 282742862);