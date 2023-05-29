CREATE TABLE gkg.raw (
    date integer,
    numarts integer,
    counts text,
    themes text,
    locations text,
    persons text,
    organizations text,
    tone text,
    cameoeventids text,
    sources text,
    sourceurls text
);

create index raw_date_int on raw(date);

create table gkg_staging (
	id serial primary key,
	date date,
	date_int int,
	numarts int,
	counts text,
	themes text,
	locations text,
	persons text,
	organizations text,
	tone text,
	cameo_event_ids text,
	sources text,
	source_urls text
);

create index gkg_date on gkg_staging(date);
create index gkg_date_int on gkg_staging(date_int);

insert into gkg_staging(
	date,
	date_int,
	numarts,
	counts,
	themes,
	locations,
	persons,
	organizations,
	tone,
	cameo_event_ids,
	sources,
	source_urls
) select
	to_date(date::text,'yyyymmdd'),
	date,
	numarts,
	counts,
	themes,
	locations,
	persons,
	organizations,
	tone,
	cameoeventids,
	sources,
	sourceurls
from raw;

create materialized view gkg as
select
	id,
	date,
	date_int,
	element[1]::float as tone,
	element[2]::float as positivity,
	element[3]::float as negativity,
	element[4]::float as polarity,
	element[5]::float as activity_reference_density,
	element[6]::float as self_group_reference_density
from (select id,date,date_int,string_to_array(tone,',') as element from gkg_staging) as foo
;

create index gkg2_id on gkg(id);
create index gkg2_date on gkg(date);
create index gkg2_date_int on gkg(date_int);

create materialized view theme as
select id,seqnum,theme
from gkg_staging
cross join lateral unnest(string_to_array(themes,';')) with ordinality as s(theme, seqnum)
;

create index theme_id on theme(id);
create index theme_theme on theme(theme);

create materialized view location_staging as
select id,seqnum,location
from gkg_staging
cross join lateral unnest(string_to_array(locations,';')) with ordinality as s(location, seqnum)
;

create materialized view location as
select
	id,
	seqnum,
	element[1] as geo_type,
	element[2] as geo_full_name,
	element[3] as geo_country_code,
	element[4] as geo_adm1_code,
	case when length(element[5]) = 0 then null else element[5]::float end as latitute,
	case when length(element[6]) = 0 then null else element[6]::float end as longitude,
	element[7] as geo_feature_id
from (select id,seqnum,string_to_array(location,'#') as element from location_staging) as foo
;

create index location_id on location(id);
create index location_country_code on location(geo_country_code);
create index location_adm1_code on location(geo_adm1_code);

create materialized view person as
select id,seqnum,person
from gkg_staging
cross join lateral unnest(string_to_array(persons,';')) with ordinality as s(person, seqnum)
;

create index person_id on person(id);
create index person_person on person(person);

create materialized view organization as
select id,seqnum,organization
from gkg_staging
cross join lateral unnest(string_to_array(organizations,';')) with ordinality as s(organization, seqnum)
;

create index organization_id on organization(id);
create index organization_organization on organization(organization);

create materialized view cameo_event_id as
select id,seqnum,cameo_event_id
from gkg_staging
cross join lateral unnest(string_to_array(cameo_event_ids,',')) with ordinality as s(cameo_event_id, seqnum)
;

create index cameo_id on cameo_event_id(id);
create index cameo_cameo on cameo_event_id(cameo_event_id);

create materialized view source as
select id,seqnum,source
from gkg_staging
cross join lateral unnest(string_to_array(sources,';')) with ordinality as s(source, seqnum)
;

create index source_id on source(id);
create index source_source on source(source);

create materialized view source_url as
select id,seqnum,source_url
from gkg_staging
cross join lateral unnest(string_to_array(source_urls,'<UDIV>')) with ordinality as s(source_url, seqnum)
;

create index source_url_id on source_url(id);

create materialized view umls_local.hierarchy as
select
	mrhier.cui,
	mrhier.aui,
	mrconso.str,
	ancestor.cui as ancestor_cui,
	ancestor_aui,
	ancestor.str as ancestor_str,
	seqnum + 1 as depth,
	(
		select max(seqnum)
		from umls.mrhier as x
		cross join lateral unnest(string_to_array(ptr,'.')) with ordinality as s(ancestor_aui, seqnum)
		where mrhier.cui = x.cui and mrhier.ptr = x.ptr
		and mrhier.ptr ~ '^A0434168'
	) - seqnum + 1 as distance
from umls.mrhier
cross join lateral unnest(string_to_array(ptr,'.')) with ordinality as s(ancestor_aui, seqnum),
umls.mrconso,
umls.mrconso as ancestor
where mrhier.aui=mrconso.aui
and ancestor_aui = ancestor.aui
and mrhier.ptr ~ '^A0434168'
;

create materialized view umls_local.hierarchy_full as
select
	mrhier.cui,
	mrhier.aui,
	mrconso.str,
	ancestor.cui as ancestor_cui,
	ancestor_aui,
	ancestor.str as ancestor_str,
	seqnum + 1 as depth,
	(
		select max(seqnum)
		from umls.mrhier as x
		cross join lateral unnest(string_to_array(ptr,'.')) with ordinality as s(ancestor_aui, seqnum)
		where mrhier.cui = x.cui and mrhier.ptr = x.ptr
	) - seqnum + 1 as distance
from umls.mrhier
cross join lateral unnest(string_to_array(ptr,'.')) with ordinality as s(ancestor_aui, seqnum),
umls.mrconso,
umls.mrconso as ancestor
where mrhier.aui=mrconso.aui
and ancestor_aui = ancestor.aui
;

------------------------------

create materialized view gkg_covid as
select distinct gkg.* from gkg.gkg natural join gkg.theme natural join gkg.theme_count where theme_count.theme~'CORONAVIRUS' or theme_count.theme~'COVID';

create index gkg_id on gkg_covid(id);

create materialized view source_url_covid as
select source_url.* from gkg_covid natural join gkg.source_url;

create index source_id on source_url_covid(id);
create index source_url on source_url_covid(source_url);

create table document(
	source_url text primary key,
	visited timestamp,
	html text
);
