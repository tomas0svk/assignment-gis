DROP FUNCTION getpointdistance(bigint,bigint);

CREATE OR REPLACE FUNCTION public.getpointdistance(id1 bigint, id2 bigint)
  RETURNS real AS $$
DECLARE
	d real;
	
	p1g geometry;
	p2g geometry;
BEGIN

	select geom from network_nodes into p1g where id = id1;
	select geom from network_nodes into p2g where id = id2;
	
	select st_distance(p1g::geography, p2g::geography) into d;
	
	return d;	
END
$$ language plpgsql; 

select * from getpointdistance(26407350::bigint, 26407351::bigint);

select ST_astext(geom), * from network_nodes limit 2;

update network SET cost = getpointdistance(startp, endp);
select * from network limit 10;

