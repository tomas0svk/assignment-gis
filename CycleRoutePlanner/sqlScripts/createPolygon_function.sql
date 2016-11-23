DROP FUNCTION createboundingpolygon(bigint,bigint);

CREATE OR REPLACE FUNCTION public.createboundingpolygon(id1 bigint, id2 bigint)
  RETURNS geometry AS $$
DECLARE
	p1 text;
	p1conc text;
	p1spl text[];
	p1lat real;
	p1lng real;
	p2 text;
	p2conc text;
	p2spl text[];
	p2lat real;
	p2lng real;
	
	latdiff real;
	lngdiff real;
	
	polygon real[];
	polygonPts geometry[];
	polygeom geometry;	
BEGIN
	select ST_AsText(geom) into p1 from network_nodes where id = id1;
	select ST_AsText(geom) into p2 from network_nodes where id = id2;
	p1conc := substring(p1 from 7 for char_length(p1)-7);
	p2conc := substring(p2 from 7 for char_length(p2)-7);
	p1spl := string_to_array(p1conc, ' ');
	p2spl := string_to_array(p2conc, ' ');
	p1lng := p1spl[1];
	p1lat := p1spl[2];
	p2lng := p2spl[1];
	p2lat := p2spl[2];
	latdiff := p1lat-p2lat;
	lngdiff := p1lng-p2lng;
	
	polygon[1] := p1lat + latdiff*0.3;
	polygon[2] := p1lng + lngdiff*0.3;
	
	polygon[3] := p1lat + lngdiff*0.3;
	polygon[4] := p1lng - latdiff*0.3;
	
	polygon[5] := (p1lat+p2lat)/2 + lngdiff*0.6;
	polygon[6] := (p1lng+p2lng)/2 - latdiff*0.6;
	
	polygon[7] := p2lat + lngdiff*0.3;
	polygon[8] := p2lng - latdiff*0.3;
	
	polygon[9] := p2lat - latdiff*0.3;
	polygon[10] := p2lng - lngdiff*0.3;
	
	polygon[11] := p2lat - lngdiff*0.3;
	polygon[12] := p2lng + latdiff*0.3;
	
	polygon[13] := (p1lat+p2lat)/2 - lngdiff*0.6;
	polygon[14] := (p1lng+p2lng)/2 + latdiff*0.6;
	
	polygon[15] := p1lat - lngdiff*0.3;
	polygon[16] := p1lng + latdiff*0.3;

	FOR i IN 1 .. 8 
	LOOP
		polygonPts[i] := st_makepoint(polygon[2*i], polygon[2*i-1]);
	END LOOP;
	polygonPts[9] := polygonPts[1];

	polygeom := ST_MakePolygon(ST_MakeLine(polygonPts),4326);

	return polygeom;
END
$$ language plpgsql; 

select * from createboundingpolygon(26407350::bigint, 26407351::bigint);
