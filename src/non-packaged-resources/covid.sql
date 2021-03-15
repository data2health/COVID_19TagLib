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

create materialized view covid.sentence_ncats_match as
select doi,pmcid,pmid,seqnum,seqnum2,seqnum3,seqnum4,seqnum5,seqnum6,sentnum,id
from covid.sentence_staging,covid_ncats.medication
where sentence~pattern
;

create index sent_doi on covid.sentence_ncats_match(doi);
create index sent_pmcid on covid.sentence_ncats_match(pmcid);
create index sent_pmid on covid.sentence_ncats_match(pmid);
create index sent_id on covid.sentence_ncats_match(id);

create view covid.sentence_ncats_match_filter as
select
	coalesce(doi, '') as doi,
	coalesce(pmcid, -1) as pmcid,
	coalesce(pmid, -1) as pmid,
	coalesce(seqnum, -1) as seqnum,
	coalesce(seqnum2, -1) as seqnum2,
	coalesce(seqnum3, -1) as seqnum3,
	coalesce(seqnum4, -1) as seqnum4,
	coalesce(seqnum5, -1) as seqnum5,
	coalesce(seqnum6, -1) as seqnum6,
	coalesce(sentnum, -1) as sentnum,
	id	
from covid.sentence_ncats_match;

create materialized view covid.sentence as
select
	source,
	doi,
	pmcid,
	pmid,
	case
		when source = 'biorxiv' then (select title from covid_biorxiv.document where document.doi = sentence_filter.doi)
		when source = 'litcovid' then (select article_title from covid_litcovid.article_title where article_title.pmid = sentence_filter.pmid)
		when source = 'pmc' then (select article_title from covid_litcovid.article_title natural join covid_pmc.xml_link where xml_link.pmcid = sentence_filter.pmcid limit 1)
		else ''
	end as title,
	case
		when source = 'biorxiv' then 'http://dx.doi.org/'||doi
		when source = 'litcovid' and exists (select article_id from covid_litcovid.article_id where article_id.pmid = sentence_filter.pmid and id_type='doi' limit 1)
			then 'http://dx.doi.org/'||(select article_id from covid_litcovid.article_id where article_id.pmid = sentence_filter.pmid and id_type='doi' limit 1)
		when source = 'litcovid' then 'https://pubmed.ncbi.nlm.nih.gov/'||pmid
		when source = 'pmc' and exists (select article_id from covid_litcovid.article_id natural join covid_pmc.xml_link where xml_link.pmcid = sentence_filter.pmcid and id_type='doi' limit 1)
		    then 'http://dx.doi.org/'||(select article_id from covid_litcovid.article_id natural join covid_pmc.xml_link where xml_link.pmcid = sentence_filter.pmcid and id_type='doi' limit 1)
		when source = 'pmc' then 'https://www.ncbi.nlm.nih.gov/pmc/articles/PMC'||pmcid
		else ''
	end as url,
	section,
	medication,
	regexp_replace(sentence, '('||pattern||')', '<b>\1</b>', 'i') as sentence
from covid.sentence_ncats_match_filter
natural join covid_ncats.medication
natural join covid.sentence_filter
;

create index s_doi on covid.sentence(doi);
create index s_pmcid on covid.sentence(pmcid);
create index s_pmid on covid.sentence(pmid);
create index s_med on covid.sentence(medication);
