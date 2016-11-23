--drop table network_long;
create table network_long(id serial, startp bigint, endp bigint, cost float, PRIMARY KEY(id));

do $$
DECLARE
	entry bigint[];
BEGIN
for entry in select nodes from ways loop
	insert into network_long (startp, endp, cost) values (entry[1], entry[array_length(entry,1)], array_length(entry,1)-1::float);
end loop;
end;
$$;

create table network_nodes(id bigint, geom geometry(Point,4326), PRIMARY KEY(id));
delete from network_nodes;

insert into network_nodes (
(select startp as point, geom from network nw1 join nodes nd1 on nw1.startp = nd1.id)
union 
(select endp as point, geom from network nw2 join nodes nd2 on nw2.endp = nd2.id));