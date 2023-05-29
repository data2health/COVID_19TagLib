CREATE SCHEMA extraction;

CREATE TABLE extraction.biological_function (
    id serial primary key,
    biological_function text unique
);

CREATE TABLE extraction.biological_function_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references biological_function(id)
);

CREATE TABLE extraction.biological_function_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references biological_function(id)
);

CREATE VIEW summary_biological_function AS
SELECT biological_function,
    sum(count) AS sum
FROM biological_function NATURAL JOIN biological_function_mention
GROUP BY biological_function
ORDER BY 2 DESC;

CREATE TABLE extraction.collaboration (
    id serial primary key,
    collaboration text
);

CREATE TABLE extraction.collaboration_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references collaboration(id)
);

CREATE VIEW summary_collaboration AS
SELECT collaboration,
    sum(count) AS sum
FROM collaboration NATURAL JOIN collaboration_mention
GROUP BY collaboration
ORDER BY 2 DESC;

CREATE TABLE extraction.discipline (
    id serial primary key,
    discipline text
);

CREATE TABLE extraction.discipline_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references discipline(id)
);

CREATE VIEW summary_discipline AS
SELECT discipline,
    sum(count) AS sum
FROM discipline NATURAL JOIN discipline_mention
GROUP BY discipline
ORDER BY 2 DESC;

CREATE TABLE extraction.disease (
    id serial primary key,
    disease text unique
);

CREATE TABLE extraction.disease_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references disease(id)
);

CREATE TABLE extraction.disease_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references disease(id)
);

CREATE VIEW summary_disease AS
SELECT disease,
    sum(count) AS sum
FROM disease NATURAL JOIN disease_mention
GROUP BY disease
ORDER BY 2 DESC;

CREATE TABLE extraction.event (
    id serial primary key,
    event text
);

CREATE TABLE extraction.event_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references event(id)
);

CREATE VIEW summary_event AS
SELECT event,
    sum(count) AS sum
FROM event NATURAL JOIN event_mention
GROUP BY event
ORDER BY 2 DESC;

CREATE TABLE extraction.finding (
    id serial primary key,
    finding text unique
);

CREATE TABLE extraction.finding_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references finding(id)
);

CREATE TABLE extraction.finding_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references finding(id)
);

CREATE VIEW summary_finding AS
SELECT finding,
    sum(count) AS sum
FROM finding NATURAL JOIN finding_mention
GROUP BY finding
ORDER BY 2 DESC;

CREATE TABLE extraction.location (
    id serial primary key,
    location text,
    geonames_id int,
    geonames_match_string text
);

CREATE TABLE extraction.location_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references location(id)
);

CREATE VIEW summary_location AS
SELECT location,
    sum(count) AS sum
FROM location NATURAL JOIN location_mention
GROUP BY location
ORDER BY 2 DESC;

CREATE TABLE extraction.organic_chemical (
    id serial primary key,
    organic_chemical text unique
);

CREATE TABLE extraction.organic_chemical_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references organic_chemical(id)
);

CREATE TABLE extraction.organic_chemical_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references organic_chemical(id)
);

CREATE VIEW summary_organic_chemical AS
SELECT organic_chemical,
    sum(count) AS sum
FROM organic_chemical NATURAL JOIN organic_chemical_mention
GROUP BY organic_chemical
ORDER BY 2 DESC;

CREATE TABLE extraction.organism (
    id serial primary key,
    organism text unique
);

CREATE TABLE extraction.organism_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references organism(id)
);

CREATE TABLE extraction.organism_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references organism(id)
);

CREATE VIEW summary_organism AS
SELECT organism,
    sum(count) AS sum
FROM organism NATURAL JOIN organism_mention
GROUP BY organism
ORDER BY 2 DESC;

CREATE TABLE extraction.organization (
    id serial primary key,
    organization text,
    grid_id text,
    grid_match_string text,
    geonames_id int,
    geonames_match_string text
);

CREATE TABLE extraction.organization_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references organization(id)
);

CREATE VIEW summary_organization AS
SELECT organization,
    sum(count) AS sum
FROM organization NATURAL JOIN organization_mention
GROUP BY organization
ORDER BY 2 DESC;

CREATE TABLE extraction.outbreak (
    id serial primary key,
    outbreak text unique
);

CREATE TABLE extraction.outbreak_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references outbreak(id)
);

CREATE TABLE extraction.outbreak_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references outbreak(id)
);

CREATE VIEW summary_outbreak AS
SELECT outbreak,
    sum(count) AS sum
FROM outbreak NATURAL JOIN outbreak_mention
GROUP BY outbreak
ORDER BY 2 DESC;

CREATE TABLE extraction.person (
    id serial primary key,
    first_name text,
    last_name text,
    middle_name text,
    title text,
    appendix text
);

CREATE TABLE extraction.person_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references person(id)
);

CREATE VIEW summary_person AS
SELECT person,
    sum(count) AS sum
FROM person NATURAL JOIN person_mention
GROUP BY person
ORDER BY 2 DESC;

CREATE TABLE extraction.person_concept (
    id serial primary key,
    person_concept text unique
);

CREATE TABLE extraction.person_concept_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references person_concept(id)
);

CREATE TABLE extraction.person_concept_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references person_concept(id)
);

CREATE VIEW summary_person_concept AS
SELECT person_concept,
    sum(count) AS sum
FROM person_concept NATURAL JOIN person_concept_mention
GROUP BY person_concept
ORDER BY 2 DESC;

CREATE TABLE extraction.process (
    id serial primary key,
    process text unique
);

CREATE TABLE extraction.process_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references process(id)
);

CREATE TABLE extraction.process_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references process(id)
);

CREATE VIEW summary_process AS
SELECT process,
    sum(count) AS sum
FROM process NATURAL JOIN process_mention
GROUP BY process
ORDER BY 2 DESC;

CREATE TABLE extraction.project (
    id serial primary key,
    project text
);

CREATE TABLE extraction.project_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references project(id)
);

CREATE VIEW summary_project AS
SELECT project,
    sum(count) AS sum
FROM project NATURAL JOIN project_mention
GROUP BY project
ORDER BY 2 DESC;

CREATE TABLE extraction.resource (
    id serial primary key,
    resource text unique
);

CREATE TABLE extraction.resource_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references resource(id)
);

CREATE TABLE extraction.resource_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references resource(id)
);

CREATE VIEW summary_resource AS
SELECT resource,
    sum(count) AS sum
FROM resource NATURAL JOIN resource_mention
GROUP BY resource
ORDER BY 2 DESC;

CREATE TABLE extraction.technique (
    id serial primary key,
    technique text unique
);

CREATE TABLE extraction.technique_mention (
    id integer,
    pmcid integer,
    count integer,
    primary key(id,pmcid),
    foreign key(id) references technique(id)
);

CREATE TABLE extraction.technique_match (
    id integer,
    seqnum integer,
    umls_id text,
    umls_match_string text,
    primary key(id,seqnum),
    foreign key(id) references technique(id)
);

CREATE VIEW summary_technique AS
SELECT technique,
    sum(count) AS sum
FROM technique NATURAL JOIN technique_mention
GROUP BY technique
ORDER BY 2 DESC;


CREATE TABLE extraction.component (
    element text,
    name text
);

CREATE TABLE extraction.template (
    fragment text,
    tgrep text,
    mode text,
    relation text,
    slot0 text,
    slot1 text,
    slot2 text,
    slot3 text,
    slot4 text,
    slot5 text,
    slot6 text,
    slot7 text,
    slot8 text,
    slot9 text,
    instances integer
);

CREATE TABLE extraction.template_defer (
    fragment text
);

CREATE TABLE extraction.template_suppress (
    fragment text
);

-------------------------

create materialized view umls_biological_function as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from biological_function natural join biological_function_match natural join biological_function_mention, gkg.gkg
	where biological_function_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_disease as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from disease natural join disease_match natural join disease_mention, gkg.gkg
	where disease_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_finding as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from finding natural join finding_match natural join finding_mention, gkg.gkg
	where finding_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_organic_chemical as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from organic_chemical natural join organic_chemical_match natural join organic_chemical_mention, gkg.gkg
	where organic_chemical_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_organism as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from organism natural join organism_match natural join organism_mention, gkg.gkg
	where organism_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_outbreak as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from outbreak natural join outbreak_match natural join outbreak_mention, gkg.gkg
	where outbreak_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_person_concept as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from person_concept natural join person_concept_match natural join person_concept_mention, gkg.gkg
	where person_concept_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_process as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from process natural join process_match natural join process_mention, gkg.gkg
	where process_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_resource as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from resource natural join resource_match natural join resource_mention, gkg.gkg
	where resource_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_technique as
select
	umls_id as cui,
	date,
	sum(count) as count
from (
	select umls_id,date,count
	from technique natural join technique_match natural join technique_mention, gkg.gkg
	where technique_mention.pmcid=gkg.id
	) as foo
group by 1,2;

create materialized view umls_all as
select
	cui,
	date,
	sum(count) as count
from (
	select * from umls_biological_function
union
	select * from umls_disease
union
	select * from umls_finding
union
	select * from umls_organic_chemical
union
	select * from umls_organism
union
	select * from umls_outbreak
union
	select * from umls_person_concept
union
	select * from umls_process
union
	select * from umls_resource
union
	select * from umls_technique
) as foo
group by 1,2;

---------------

create materialized view fragment_staging as 
select gkg_covid.date,document.id
from gkg_local.document,gkg_local.source_url_covid,gkg_local.gkg_covid
where document.source_url=source_url_covid.source_url
  and source_url_covid.id=gkg_covid.id
;

create index fsdate on fragment_staging(date);
create index fsid on fragment_staging(id);

select date,id from
	(select distinct id from gkg_local.parse where id not in (select id from gkg_local.fragment)) as foo
natural join
	gkg_local.fragment_staging
order by 1,2;

select foo.date,foo.id,source_url from 
	(select date,id,source_url,row_number() over(partition by date order by id) as rownum from
		(select id,source_url from gkg_local.document where html is not null and id not in (select id from gkg_local.sentence)) as fud
	natural join
		gkg_local.fragment_staging
	 ) as foo
where rownum <= 3
order by 1,2;
