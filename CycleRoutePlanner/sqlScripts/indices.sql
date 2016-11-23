CREATE INDEX spointinx
  ON public.network2
  USING gist
  (startpg);
  
  CREATE INDEX epointinx
  ON public.network2
  USING gist
  (endpg);
  
CREATE INDEX network2inx
  ON public.network2
  USING btree
  (id);
  
  CREATE INDEX network_nodesinx
  ON public.network_nodes
  USING btree
  (id);
  
  CREATE INDEX network_nodes2geominx
  ON public.network_nodes2
  USING gist
  (geom);
  
  CREATE INDEX network_nodes2stgfinx
  ON public.network_nodes2
  USING gist
  ((geom::geography));

  
CREATE INDEX network_nodes2inx
  ON public.network_nodes2
  USING btree
  (id);
  
  CREATE INDEX idx_nodes_geom
  ON public.nodes
  USING gist
  (geom);
  
  CREATE INDEX idx_relation_members_member_id_and_type
  ON public.relation_members
  USING btree
  (member_id, member_type COLLATE pg_catalog."default");
  
  CREATE INDEX idx_way_nodes_node_id
  ON public.way_nodes
  USING btree
  (node_id);
