create view covid.weeks as
SELECT DISTINCT to_char(generate_series('2020-01-01 00:00:00-06'::timestamp with time zone, now(), '7 days'::interval), 'yyyy-WW'::text) AS week;

create view covid_biorxiv.sentence_staging as 
	select
		(select site from covid_biorxiv.biorxiv_current where biorxiv_current.doi = sentence.doi) as source,
		doi,
		null::int as pmcid,
		null::int as pmid,
		seqnum,
		lower(name) as section,
		null::int as seqnum2,
		null::int as seqnum3,
		null::int as seqnum4,
		null::int as seqnum5,
		null::int as seqnum6,
		sentnum,
		full_text as sentence
	from covid_biorxiv.sentence natural join covid_biorxiv.section
;
create view covid_litcovid.sentence_staging as
	select
		'litcovid' as source,
		null as doi,
		null::int as pmcid,
		pmid,
		seqnum,
		(case when seqnum = 0 then 'title' else 'abstract' end) as section,
		null::int as seqnum2,
		null::int as seqnum3,
		null::int as seqnum4,
		null::int as seqnum5,
		null::int as seqnum6,
		sentence as sentnum,
		string as sentence
	from covid_litcovid.sentence
;
create view covid_pmc.sentence_staging as
	select
		'pmc' as source,
		null as doi,
		pmcid,
		null::int as pmid,
		seqnum,
		regexp_replace(lower(title), '^[0-9]+[.]? *', '') as section,
		seqnum2,
		seqnum3,
		seqnum4,
		seqnum5,
		seqnum6,
		sentnum,
		string as sentence
	from covid_pmc.sentence natural join covid_pmc.section
;

CREATE TABLE covid.sentence_staging (
    source text,
    doi text,
    pmcid integer,
    pmid integer,
    seqnum integer,
    section text,
    seqnum2 text,
    seqnum3 text,
    seqnum4 text,
    seqnum5 text,
    seqnum6 text,
    sentnum integer,
    sentence text
);

CREATE INDEX ss_doi ON covid.sentence_staging2 USING btree (doi);
CREATE INDEX ss_pmcid ON covid.sentence_staging2 USING btree (pmcid);
CREATE INDEX ss_pmid ON covid.sentence_staging2 USING btree (pmid);

create view covid.sentence_staging_old as
select distinct * from (
	select
		(select site from covid_biorxiv.biorxiv_current where biorxiv_current.doi = sentence.doi) as source,
		doi,
		null::int as pmcid,
		null::int as pmid,
		seqnum,
		lower(name) as section,
		null as seqnum2,
		null as seqnum3,
		null as seqnum4,
		null as seqnum5,
		null as seqnum6,
		sentnum,
		full_text as sentence
	from covid_biorxiv.sentence natural join covid_biorxiv.section
	union
	select
		'pmc' as source,
		null as doi,
		pmcid,
		null as pmid,
		seqnum,
		regexp_replace(lower(title), '^[0-9]+[.]? *', '') as section,
		seqnum2,
		seqnum3,
		seqnum4,
		seqnum5,
		seqnum6,
		sentnum,
		string as sentence
	from covid_pmc.sentence natural join covid_pmc.section
	union
	select
		'litcovid' as source,
		null as doi,
		null::int as pmcid,
		pmid,
		seqnum,
		(case when seqnum = 0 then 'title' else 'abstract' end) as section,
		null as seqnum2,
		null as seqnum3,
		null as seqnum4,
		null as seqnum5,
		null as seqnum6,
		sentence as sentnum,
		string as sentence
	from covid_litcovid.sentence
) as foo;

create view covid.sentence_filter as
select
	source,
	coalesce(doi, '') as doi,
	coalesce(pmcid, -1) as pmcid,
	coalesce(pmid, -1) as pmid,
	coalesce(seqnum, -1) as seqnum,
	section,
	coalesce(seqnum2, -1) as seqnum2,
	coalesce(seqnum3, -1) as seqnum3,
	coalesce(seqnum4, -1) as seqnum4,
	coalesce(seqnum5, -1) as seqnum5,
	coalesce(seqnum6, -1) as seqnum6,
	coalesce(sentnum, -1) as sentnum,
	sentence
from covid.sentence_staging;

create view covid.source_by_week as
select week,source,coalesce(count,0) as count
from
	covid.weeks
natural left join (
	select 'litcovid' as source, to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week,count(*)
	from covid_litcovid.article group by 1,2
	) as foo
union
select week,'pmc' as source,coalesce(count,0) as count
from
	covid.weeks
natural left join (
	select 'pmc' as source, to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week,count(*)
	from covid_litcovid.article natural join covid_pmc.xml_link group by 1,2 order by 2
	) as foo
union
select week, 'medRxiv' as source, coalesce(count,0) as count
from
	covid.weeks
natural left join (
	select site as source, to_char(pub_date,'yyyy-WW') as week, count(*)
from covid_biorxiv.biorxiv_current
where site='medRxiv' group by 1,2
) as foo
union
select week, 'bioRxiv' as source, coalesce(count,0) as count
from
	covid.weeks
natural left join (
	select site as source, to_char(pub_date,'yyyy-WW') as week, count(*)
from covid_biorxiv.biorxiv_current
where site='bioRxiv' group by 1,2
) as foo
;

