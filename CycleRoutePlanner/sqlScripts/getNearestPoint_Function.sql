CREATE OR REPLACE FUNCTION public."getnearestpoint"(lat numeric, lng numeric)
  RETURNS record AS $$
DECLARE
	result record;
BEGIN
	with center as (
			select ST_SetSRID(ST_MakePoint(lng, lat),4326) c
	) 
	SELECT id, ST_AsText(geom) into result
	FROM network_nodes2, center
	where st_dwithin(network_nodes2.geom::geography, center.c::geography, 250)
	ORDER BY network_nodes2.geom <-> center.c
	limit 1;
	if result is not null then
		return result;
	end if;
	
	with center as (
			select ST_SetSRID(ST_MakePoint(lng, lat),4326) c
	) 
	SELECT id, ST_AsText(geom) into result
	FROM network_nodes2, center
	ORDER BY network_nodes2.geom <-> center.c
	limit 1;
	
	return result;
	
END
$$ language plpgsql; 

select * from getnearestpoint(10.0, 10.0) f(col1 bigint, col2 text);

