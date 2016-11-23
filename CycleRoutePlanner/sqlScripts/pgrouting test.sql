SELECT * FROM pgr_dijkstra('
   SELECT id::integer, 
          startp AS source, 
          endp AS target, 
          cost AS cost
   FROM network',
282742858,
282742862);

CREATE INDEX route_nodes_inx ON route_nodes (geom);
--select count(*) from (select distinct geom from route_nodes) s;

SELECT *
FROM network_nodes 
ORDER BY network_nodes.geom <-> ST_SetSRID(ST_MakePoint(18.581235, 48.788310),4326)
limit 1;

SELECT *
FROM route_nodes 
ORDER BY route_nodes.geom <-> ST_SetSRID(ST_MakePoint(18.579090, 48.790872),4326)
limit 1;


