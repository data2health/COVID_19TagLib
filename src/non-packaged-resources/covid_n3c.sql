create view covid_n3c.cohort_med as {
 SELECT foo.temp AS original,
    ((('(^|[^a-zA-Z])['::text || lower("substring"(foo.temp, 1, 1))) || upper("substring"(foo.temp, 1, 1))) || ']'::text) || lower("substring"(foo.temp, 2)) AS normalized
   FROM ( SELECT regexp_replace(regexp_replace(med_use_frequency_for_export.value, '_gtt'::text, ''::text), '_'::text, ' '::text, 'g'::text) AS temp
           FROM enclave_cohort_release_18.med_use_frequency_for_export
          WHERE med_use_frequency_for_export.value !~ 'systemic'::text AND med_use_frequency_for_export.value <> 'Totals'::text) foo;

create materialized view covid_n3c.sentence as 
select
	sentence_filter.*,
	original
from covid.sentence_filter,cohort_med
where sentence ~ normalized
   or sentence ~ original
;

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

create view covid_n3c.process_queue as
select distinct
	doi,
	pmcid,
	pmid
from covid.sentence_filter
where not exists (select doi
				 from covid_n3c.processed
				 where processed.doi=sentence_filter.doi
				   and processed.pmcid = sentence_filter.pmcid
				   and processed.pmid = sentence_filter.pmid
				 )
order by doi,pmcid,pmid;

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

