create view covid_n3c.cohort_med as {
 SELECT foo.temp AS original,
    ((('(^|[^a-zA-Z])['::text || lower("substring"(foo.temp, 1, 1))) || upper("substring"(foo.temp, 1, 1))) || ']'::text) || lower("substring"(foo.temp, 2)) AS normalized
   FROM ( SELECT regexp_replace(regexp_replace(med_use_frequency_for_export.value, '_gtt'::text, ''::text), '_'::text, ' '::text, 'g'::text) AS temp
           FROM enclave_cohort_release_18.med_use_frequency_for_export
          WHERE med_use_frequency_for_export.value !~ 'systemic'::text AND med_use_frequency_for_export.value <> 'Totals'::text) foo;

create view covid_n3c.sentence_staging as
select
	source,
	doi,
	pmcid,
	pmid,
	case
		when source = 'bioRxiv' then (select title from covid_biorxiv.biorxiv_current where biorxiv_current.doi = sentence_filter.doi)
		when source = 'medRxiv' then (select title from covid_biorxiv.biorxiv_current where biorxiv_current.doi = sentence_filter.doi)
		when source = 'litcovid' then (select article_title from covid_litcovid.article_title where article_title.pmid = sentence_filter.pmid)
		when source = 'pmc' then (select article_title from covid_litcovid.article_title natural join covid_pmc.xml_link where xml_link.pmcid = sentence_filter.pmcid limit 1)
		else ''
	end as title,
	case
		when source = 'bioRxiv' then 'http://dx.doi.org/'||doi
		when source = 'medRxiv' then 'http://dx.doi.org/'||doi
		when source = 'litcovid' and exists (select article_id from covid_litcovid.article_id where article_id.pmid = sentence_filter.pmid and id_type='doi' limit 1)
			then 'http://dx.doi.org/'||(select article_id from covid_litcovid.article_id where article_id.pmid = sentence_filter.pmid and id_type='doi' limit 1)
		when source = 'litcovid' then 'https://pubmed.ncbi.nlm.nih.gov/'||pmid
		when source = 'pmc' and exists (select article_id from covid_litcovid.article_id natural join covid_pmc.xml_link where xml_link.pmcid = sentence_filter.pmcid and id_type='doi' limit 1)
		    then 'http://dx.doi.org/'||(select article_id from covid_litcovid.article_id natural join covid_pmc.xml_link where xml_link.pmcid = sentence_filter.pmcid and id_type='doi' limit 1)
		when source = 'pmc' then 'https://www.ncbi.nlm.nih.gov/pmc/articles/PMC'||pmcid
		else ''
	end as url,
	coalesce(section_map.label, '** undetermined **') as section,
	original,
	regexp_replace(sentence, '('||normalized||')', '<b>\1</b>', 'ig') as sentence,
	week
from covid_n3c.cohort_med, covid.sentence_filter, covid.section_map
where sentence ~ normalized
  and sentence_filter.section = section_map.section
;

create table covid_n3c.sentence (
	source text,
	doi text,
	pmcid int,
	pmid int,
	title text,
	url text,
	section text,
	original text,
	sentence text,
	week text
);

create index s_doi on covid_n3c.sentence(doi);
create index s_pmcid on covid_n3c.sentence(pmcid);
create index s_pmid on covid_n3c.sentence(pmid);
create index s_med on covid_n3c.sentence(original);

create materialized view covid_n3c.drugs_by_week as 
select distinct
	source,
	doi,
	pmcid,
	sentence.pmid,
	sentence.original as medication,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_n3c.sentence,covid_litcovid.article
where source='litcovid'
  and sentence.pmid=article.pmid
union
select distinct
	source,
	doi,
	sentence.pmcid,
	sentence.pmid,
	sentence.original as medication,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_n3c.sentence,covid_litcovid.article natural join covid_pmc.link
where source='pmc'
  and sentence.pmcid=link.pmcid
union
select distinct
	site as source,
	sentence.doi,
	null::int as pmcid,
	null::int as pmid,
	sentence.original as medication,
	to_char(pub_date,'yyyy-WW') as week
from covid_n3c.sentence, covid_biorxiv.cohort_match natural join covid_biorxiv.biorxiv_current
where sentence.doi = cohort_match.doi
;

create materialized view covid_n3c.source_by_week as
select source,week,coalesce(count, 0) as count from 
	(select 'bioRxiv' as source,week from covid.weeks) as foo
	natural left outer join
	(select source,week,count(*) from covid_n3c.drugs_by_week group by 1,2)  as bar
union
	select source,week,coalesce(count, 0) as count from 
	(select 'medRxiv' as source,week from covid.weeks) as foo
	natural left outer join
	(select source,week,count(*) from covid_n3c.drugs_by_week group by 1,2)  as bar
union
	select source,week,coalesce(count, 0) as count from 
	(select 'litcovid' as source,week from covid.weeks) as foo
	natural left outer join
	(select source,week,count(*) from covid_n3c.drugs_by_week group by 1,2)  as bar
union
	select source,week,coalesce(count, 0) as count from 
	(select 'pmc' as source,week from covid.weeks) as foo
	natural left outer join
	(select source,week,count(*) from covid_n3c.drugs_by_week group by 1,2)  as bar
order by 1,2;

create table covid_n3c.processed (
	doi text,
	pmcid int,
	pmid int
);

create index procdoi on covid_n3c.processed(doi);
create index procpmcid on covid_n3c.processed(pmcid);
create index procpmid on covid_n3c.processed(pmid);

create materialized view covid_n3c.refresh_queue as
 SELECT process_queue.doi,
    process_queue.pmcid,
    process_queue.pmid
   FROM covid_n3c.process_queue;

create table covid_n3c.sentence_concept_match (
	doi text,
	pmcid int,
	pmid int,
	seqnum int,
	seqnum2 int,
	seqnum3 int,
	seqnum4 int,
	seqnum5 int,
	seqnum6 int,
	sentnum int,
	concept text,
	phrase text,
	count int
);

