------- compounds

create materialized view cid_mesh as
select
	substring(string from '^[0-9]*')::int as cid,
	seqnum-1 as seqnum,
	mesh
from cid_mesh_staging, unnest(string_to_array(string,chr(9)))
with ordinality as t(mesh,seqnum)
where seqnum > 1
;

create index cmcid on cid_mesh(cid);
create index cmmesh on cid_mesh(mesh);

create index smsid on sid_mesh(sid);
create index smmesh on sid_mesh(mesh);

create materialized view mesh_pharm as
select
	substring(string from '^[^\t]*') as mesh,
	seqnum-1 as seqnum,
	pharm
from mesh_pharm_staging, unnest(string_to_array(string,chr(9)))
with ordinality as t(pharm,seqnum)
where seqnum > 1
;

create index mpmesh on mesh_pharm(mesh);
create index mppharm on mesh_pharm(pharm);

------- substances

create materialized view sid_mesh as
select
	substring(string from '^[0-9]*')::int as sid,
	seqnum-1 as seqnum,
	mesh
from sid_mesh_staging, unnest(string_to_array(string,chr(9)))
with ordinality as t(mesh,seqnum)
where seqnum > 1
;

create index smsid on sid_mesh(sid);
create index smmesh on sid_mesh(mesh);

create materialized view mesh_pharm as
select
	substring(string from '^[^\t]*') as mesh,
	seqnum-1 as seqnum,
	pharm
from mesh_pharm_staging, unnest(string_to_array(string,chr(9)))
with ordinality as t(pharm,seqnum)
where seqnum > 1
;

create index mpmesh on mesh_pharm(mesh);
create index mppharm on mesh_pharm(pharm);
