create view covid.sentence as
select distinct * from (
	select
		'biorxiv' as source,
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
