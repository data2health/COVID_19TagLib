create view covid.weeks as
SELECT DISTINCT to_char(generate_series('2020-01-01 00:00:00-06'::timestamp with time zone, now(), '7 days'::interval), 'yyyy-WW'::text) AS week;

create view covid.sentence_staging as
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
	from covid_biorxiv.sentence natural join covid_biorxiv.cohort_match
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

