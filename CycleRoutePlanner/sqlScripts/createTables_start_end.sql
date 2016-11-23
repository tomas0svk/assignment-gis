drop table network;
create table network(id serial, startp bigint, endp bigint, cost float, PRIMARY KEY(id));

do $$
DECLARE
	entry bigint[];
BEGIN
for entry in select nodes from ways loop
	FOR i IN 1 .. array_length(entry,1)-1 LOOP
		insert into network (startp, endp, cost) values (entry[i], entry[i+1], 1::float);
		--raise notice 'Value: % % %', entry[i], entry[i+1], 1;
	END LOOP;
end loop;
end;
$$;

create table network_nodes(id bigint, geom geometry(Point,4326), PRIMARY KEY(id));
delete from network_nodes;

insert into network_nodes (
(select startp as point, geom from network nw1 join nodes nd1 on nw1.startp = nd1.id)
union 
(select endp as point, geom from network nw2 join nodes nd2 on nw2.endp = nd2.id));


create table network_nodes2(id bigint, geom geometry(Point,4326), PRIMARY KEY(id));

insert into network_nodes2 (
(select startp as point, geom from network2 nw1 join nodes nd1 on nw1.startp = nd1.id)
union 
(select endp as point, geom from network2 nw2 join nodes nd2 on nw2.endp = nd2.id));