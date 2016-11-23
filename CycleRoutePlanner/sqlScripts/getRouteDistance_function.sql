drop function public.getroutedcost(bigint);

CREATE OR REPLACE FUNCTION public.getroutedistance(rt_id bigint)
  RETURNS real AS $$
DECLARE
	d real;
	nds bigint[];
BEGIN
	d := 0;

	SELECT nodes into nds 
	from network2 n
	where n.id = rt_id;

	FOR i IN 1 .. array_upper(nds, 1)-1
	LOOP
			d := d + public.getpointdistance(nds[i], nds[i+1]);
	END LOOP;
	
	return d;
END
$$ language plpgsql; 

update network2 SET cost = getroutedistance(id);
select * from network2 limit 10;
