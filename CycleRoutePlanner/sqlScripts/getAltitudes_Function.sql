CREATE OR REPLACE FUNCTION public.getaltitudes(ids bigint[])
  RETURNS real[] AS
$BODY$DECLARE
    result real[];
    tmp real;
BEGIN
	select array(
		SELECT alt
		FROM network_nodes n 
		RIGHT JOIN unnest(ids) as v(id)
			ON (n.id=v.id)
	) into result;

	RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.getaltitudes(bigint[])
  OWNER TO postgres;