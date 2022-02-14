create view covid_ncats.medication as
select
	id,
	regexp_replace(regexp_replace(med_name__generic_, ' exclud.*', ''), ' [iI]njectable.*', '') as medication,
	'['||lower(substring(med_name__generic_ from 1 for 1))||upper(substring(med_name__generic_ from 1 for 1))||']'||substring(med_name__generic_ from 2) as pattern,
	priority,
	rxcui,
	(select max(foo.id) from medications_concept_sets as foo where class__atc_ is not null and foo.id < bar.id) as class_id,
	(select class__atc_ from medications_concept_sets where id in (select max(foo.id) from medications_concept_sets as foo where class__atc_ is not null and foo.id < bar.id)) as class_name
from covid_ncats.medications_concept_sets as bar
where class__atc_ is null
  and med_name__generic_ is not null;

create table covid_ncats.sentence_match (
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
	id int,
	phrase text,
	count int
);

create index sent_doi on covid_ncats.sentence_match(doi);
create index sent_pmcid on covid_ncats.sentence_match(pmcid);
create index sent_pmid on covid_ncats.sentence_match(pmid);
create index sent_id on covid_ncats.sentence_match(id);

create view covid_ncats.sentence_match_filter as
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
from covid_ncats.sentence_match;

create view covid_ncats.process_queue as
select distinct
	doi,
	pmcid,
	pmid
from covid.sentence_filter
where not exists (select doi
				 from covid_ncats.processed
				 where processed.doi=sentence_filter.doi
				   and processed.pmcid = sentence_filter.pmcid
				   and processed.pmid = sentence_filter.pmid
				 )
order by doi,pmcid,pmid;

create view covid_ncats.sentence_staging as
select
	source,
	doi,
	pmcid,
	pmid,
	case
		when source = 'biorxiv' then (select title from covid_biorxiv.biorxiv_current where biorxiv_current.doi = sentence_filter.doi)
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
	regexp_replace(sentence, '('||pattern||')', '<b>\1</b>', 'ig') as sentence
from covid_ncats.sentence_match_filter
natural join covid_ncats.medication
natural join covid.sentence_filter
;

create table covid_ncats.sentence (
	source text,
	doi text,
	pmcid int,
	pmid int,
	title text,
	url text,
	section text,
	medication text,
	sentence text,
	week text
);

create index s_doi on covid_ncats.sentence(doi);
create index s_pmcid on covid_ncats.sentence(pmcid);
create index s_pmid on covid_ncats.sentence(pmid);
create index s_med on covid_ncats.sentence(medication);

create materialized view covid_ncats.drugs_by_week as 
select distinct
	source,
	doi,
	pmcid,
	sentence.pmid,
	medication,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_ncats.sentence,covid_litcovid.article
where source='litcovid'
  and sentence.pmid=article.pmid
union
select distinct
	source,
	doi,
	sentence.pmcid,
	sentence.pmid,
	medication,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_ncats.sentence,covid_litcovid.article natural join covid_pmc.link
where source='pmc'
  and sentence.pmcid=link.pmcid
union
select distinct
	site as source,
	sentence.doi,
	null::int as pmcid,
	null::int as pmid,
	original as medication,
	to_char(pub_date,'yyyy-WW') as week
from covid_ncats.sentence, covid_biorxiv.cohort_match natural join covid_biorxiv.biorxiv_current
where sentence.doi = cohort_match.doi
;

create materialized view covid_ncats.source_by_week as
select source,week,coalesce(count, 0) as count from 
	(select 'bioRxiv' as source,week from covid.weeks) as foo
	natural left outer join
	(select source,week,count(*) from covid_ncats.drugs_by_week group by 1,2)  as bar
union
	select source,week,coalesce(count, 0) as count from 
	(select 'medRxiv' as source,week from covid.weeks) as foo
	natural left outer join
	(select source,week,count(*) from covid_ncats.drugs_by_week group by 1,2)  as bar
union
	select source,week,coalesce(count, 0) as count from 
	(select 'litcovid' as source,week from covid.weeks) as foo
	natural left outer join
	(select source,week,count(*) from covid_ncats.drugs_by_week group by 1,2)  as bar
union
	select source,week,coalesce(count, 0) as count from 
	(select 'pmc' as source,week from covid.weeks) as foo
	natural left outer join
	(select source,week,count(*) from covid_ncats.drugs_by_week group by 1,2)  as bar
order by 1,2;

select source,week,coalesce(count, 0) as count from
	(select 'bioRxiv' as source,week from covid.weeks) as bar
	natural left outer join
	(select source,week,count(*) from covid_ncats.drugs_by_week where medication='Chloroquine' group by 1,2) as foo
union
select source,week,coalesce(count, 0) as count from
	(select 'medRxiv' as source,week from covid.weeks) as bar
	natural left outer join
	(select source,week,count(*) from covid_ncats.drugs_by_week where medication='Chloroquine' group by 1,2) as foo
union
select source,week,coalesce(count, 0) as count from
	(select 'litcovid' as source,week from covid.weeks) as bar
	natural left outer join
	(select source,week,count(*) from covid_ncats.drugs_by_week where medication='Chloroquine' group by 1,2) as foo
union
select source,week,coalesce(count, 0) as count from
	(select 'pmc' as source,week from covid.weeks) as bar
	natural left outer join
	(select source,week,count(*) from covid_ncats.drugs_by_week where medication='Chloroquine' group by 1,2) as foo
order by 1,2;
