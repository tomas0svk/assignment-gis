-- Function: srtm.getaltitude(real, real)

DROP FUNCTION srtm.getaltitude(real, real);

CREATE OR REPLACE FUNCTION srtm.getaltitude(
    lat real,
    lng real)
  RETURNS real AS
$BODY$DECLARE
	ilat integer;
	ilng integer;
	flat real;
	flng real;
	xff integer;
	yff integer;
	xcf integer;
	ycf integer;
	xfc integer;
	yfc integer;
	xcc integer;
	ycc integer;
	tile bytea;
	inxff integer;
	inxcf integer;
	inxfc integer;
	inxcc integer;
	resff integer;
	rescf integer;	
	resfc integer;	
	rescc integer;
	xfloordist real;
	yfloordist real;
	interpc real;
	interpf real;
BEGIN
	--get tile number and residuum
	ilat := trunc(lat);
	ilng := trunc(lng);
	flat := lat - ilat;
	flng := lng - ilng;
	--get pixel coors (arc seconds / 3)
	yff := floor(flat * 1200);
	xff := floor(flng * 1200);
	ycf := ceil(flat * 1200);
	xcf := floor(flng * 1200);
	yfc := floor(flat * 1200);
	xfc := ceil(flng * 1200);
	ycc := ceil(flat * 1200);
	xcc := ceil(flng * 1200);
	--get pixel inx (1201*1201 grid), each encoded by 2B
	SELECT tiledata INTO tile FROM srtm.srtmdata WHERE latitude = ilat AND longitude = ilng;
	inxff := (1201 * (1201 - yff - 1) + xff - 1) * 2;
	resff := get_byte(tile,inxff)*256 + get_byte(tile, inxff+1);
	inxcf := (1201 * (1201 - ycf - 1) + xcf - 1) * 2;
	rescf := get_byte(tile,inxcf)*256 + get_byte(tile, inxcf+1);
	inxfc := (1201 * (1201 - yfc - 1) + xfc - 1) * 2;
	resfc := get_byte(tile,inxfc)*256 + get_byte(tile, inxfc+1);
	inxcc := (1201 * (1201 - ycc - 1) + xcc - 1) * 2;
	rescc := get_byte(tile,inxcc)*256 + get_byte(tile, inxcc+1);
	--quad interpolate
	yfloordist := flat * 1200 - floor(flat * 1200);
	xfloordist := flng * 1200 - floor(flng * 1200);

	interpf := resff * (1-yfloordist) + rescf * yfloordist;
	interpc := resfc * (1-yfloordist) + rescc * yfloordist;

	RETURN interpf * (1-xfloordist) + interpc * xfloordist;
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION srtm.getaltitude(real, real)
  OWNER TO postgres;
  
  
  CREATE OR REPLACE FUNCTION srtm.getaltitude(
    id1 bigint)
  RETURNS real AS
$BODY$DECLARE
    result real;
    p text;
    pconc text;
    pspl text[];
    plat real;
    plng real;
BEGIN
	select ST_AsText(geom) into p from network_nodes where id = id1;
	pconc := substring(p from 7 for char_length(p)-7);
	pspl := string_to_array(pconc, ' ');
	plng := pspl[1];
	plat := pspl[2];

	select srtm.getaltitude(plat, plng) into result;
	return result;
	
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION srtm.getaltitude(bigint)
  OWNER TO postgres;

select *, 1 from srtm.getaltitude(48.786297::real, 18.580756::real) union --325.3 kom 18
select *, 2 from srtm.getaltitude(48.7865939::real, 18.582087::real) union --316.5 kom 14
select *, 3 from srtm.getaltitude(48.784402::real, 18.57897::real) union --331 vysielac
select *, 4 from srtm.getaltitude(48.784586::real, 18.58931::real); --280 nemocnica