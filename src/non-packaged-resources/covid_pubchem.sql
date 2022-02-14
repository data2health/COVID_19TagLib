create schema covid_pubchem;

create table covid_pubchem.processed (
	doi text,
	pmcid int,
	pmid int
);

create index procdoi on covid_pubchem.processed(doi);
create index procpmcid on covid_pubchem.processed(pmcid);
create index procpmid on covid_pubchem.processed(pmid);

create view covid_pubchem.process_queue as
select distinct
	doi,
	pmcid,
	pmid
from covid.sentence_filter
where not exists (select doi
				 from covid_pubchem.processed
				 where processed.doi=sentence_filter.doi
				   and processed.pmcid = sentence_filter.pmcid
				   and processed.pmid = sentence_filter.pmid
				 )
order by doi,pmcid,pmid;


create table covid_pubchem.sentence_compound_match (
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
	pcid text,
	phrase text,
	count int
);

create index scms on covid_pubchem.sentence_compound_match(doi,pmcid,pmid);
create index scmi on covid_pubchem.sentence_compound_match(pcid);

create table covid_pubchem.sentence_gene_match (
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
	pcid text,
	phrase text,
	count int
);

create index sgms on covid_pubchem.sentence_gene_match(doi,pmcid,pmid);
create index sgmi on covid_pubchem.sentence_gene_match(pcid);

create table covid_pubchem.sentence_protein_match (
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
	pcid text,
	phrase text,
	count int
);

create index spms on covid_pubchem.sentence_protein_match(doi,pmcid,pmid);
create index spmi on covid_pubchem.sentence_protein_match(pcid);

create table covid_pubchem.sentence_substance_match (
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
	pcid text,
	phrase text,
	count int
);

create index ssms on covid_pubchem.sentence_substance_match(doi,pmcid,pmid);
create index ssmi on covid_pubchem.sentence_substance_match(pcid);

create view covid_pubchem.sentence_compound_staging as
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
	name,
	regexp_replace(sentence, '('||regexp_replace(phrase, '([\[\]\(\)])' , '\\\1', 'g')||')', '<b>\1</b>', 'ig') as sentence
from covid_pubchem.sentence_compound_match
natural join pubchem.compound
natural join covid.sentence_filter
;

create view covid_pubchem.sentence_gene_staging as
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
	name,
	regexp_replace(sentence, '('||phrase||')', '<b>\1</b>', 'ig') as sentence
from covid_pubchem.sentence_gene_match
natural join pubchem.gene
natural join covid.sentence_filter
;

create view covid_pubchem.sentence_protein_staging as
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
	name,
	regexp_replace(sentence, '('||phrase||')', '<b>\1</b>', 'ig') as sentence
from covid_pubchem.sentence_protein_match
natural join pubchem.protein
natural join covid.sentence_filter
;

create view covid_pubchem.sentence_substance_staging as
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
	name,
	regexp_replace(sentence, '('||phrase||')', '<b>\1</b>', 'ig') as sentence
from covid_pubchem.sentence_substance_match
natural join pubchem.substance
natural join covid.sentence_filter
;

create table covid_pubchem.sentence_compound (
	source text,
	doi text,
	pmcid int,
	pmid int,
	title text,
	url text,
	section text,
	name text,
	sentence text,
	week text
);

create index s_c_doi on covid_pubchem.sentence_compound(doi);
create index s_c_pmcid on covid_pubchem.sentence_compound(pmcid);
create index s_c_pmid on covid_pubchem.sentence_compound(pmid);
create index s_c_med on covid_pubchem.sentence_compound(name);

create table covid_pubchem.sentence_gene (
	source text,
	doi text,
	pmcid int,
	pmid int,
	title text,
	url text,
	section text,
	name text,
	sentence text,
	week text
);

create index s_g_doi on covid_pubchem.sentence_gene(doi);
create index s_g_pmcid on covid_pubchem.sentence_gene(pmcid);
create index s_g_pmid on covid_pubchem.sentence_gene(pmid);
create index s_g_med on covid_pubchem.sentence_gene(name);

create table covid_pubchem.sentence_protein (
	source text,
	doi text,
	pmcid int,
	pmid int,
	title text,
	url text,
	section text,
	name text,
	sentence text,
	week text
);

create index s_p_doi on covid_pubchem.sentence_protein(doi);
create index s_p_pmcid on covid_pubchem.sentence_protein(pmcid);
create index s_p_pmid on covid_pubchem.sentence_protein(pmid);
create index s_p_med on covid_pubchem.sentence_protein(name);

create table covid_pubchem.sentence_substance (
	source text,
	doi text,
	pmcid int,
	pmid int,
	title text,
	url text,
	section text,
	name text,
	sentence text,
	week text
);

create index s_s_doi on covid_pubchem.sentence_substance(doi);
create index s_s_pmcid on covid_pubchem.sentence_substance(pmcid);
create index s_s_pmid on covid_pubchem.sentence_substance(pmid);
create index s_s_med on covid_pubchem.sentence_substance(name);

create materialized view covid_pubchem.compounds_drugs_by_week as 
select distinct
	source,
	doi,
	pmcid,
	sentence_compound.pmid,
	sentence_compound.name,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_pubchem.sentence_compound,covid_litcovid.article
where source='litcovid'
  and sentence_compound.pmid=article.pmid
union
select distinct
	source,
	doi,
	sentence_compound.pmcid,
	sentence_compound.pmid,
	sentence_compound.name,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_pubchem.sentence_compound,covid_litcovid.article natural join covid_pmc.link
where source='pmc'
  and sentence_compound.pmcid=link.pmcid
union
select distinct
	site as source,
	sentence_compound.doi,
	null::int as pmcid,
	null::int as pmid,
	sentence_compound.name,
	to_char(pub_date,'yyyy-WW') as week
from covid_pubchem.sentence_compound, covid_biorxiv.cohort_match natural join covid_biorxiv.biorxiv_current
where sentence_compound.doi = cohort_match.doi
;

create materialized view covid_pubchem.genes_drugs_by_week as 
select distinct
	source,
	doi,
	pmcid,
	sentence_gene.pmid,
	sentence_gene.name,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_pubchem.sentence_gene,covid_litcovid.article
where source='litcovid'
  and sentence_gene.pmid=article.pmid
union
select distinct
	source,
	doi,
	sentence_gene.pmcid,
	sentence_gene.pmid,
	sentence_gene.name,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_pubchem.sentence_gene,covid_litcovid.article natural join covid_pmc.link
where source='pmc'
  and sentence_gene.pmcid=link.pmcid
union
select distinct
	site as source,
	sentence_gene.doi,
	null::int as pmcid,
	null::int as pmid,
	sentence_gene.name,
	to_char(pub_date,'yyyy-WW') as week
from covid_pubchem.sentence_gene, covid_biorxiv.cohort_match natural join covid_biorxiv.biorxiv_current
where sentence_gene.doi = cohort_match.doi
;

create materialized view covid_pubchem.proteins_drugs_by_week as 
select distinct
	source,
	doi,
	pmcid,
	sentence_protein.pmid,
	sentence_protein.name,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_pubchem.sentence_protein,covid_litcovid.article
where source='litcovid'
  and sentence_protein.pmid=article.pmid
union
select distinct
	source,
	doi,
	sentence_protein.pmcid,
	sentence_protein.pmid,
	sentence_protein.name,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_pubchem.sentence_protein,covid_litcovid.article natural join covid_pmc.link
where source='pmc'
  and sentence_protein.pmcid=link.pmcid
union
select distinct
	site as source,
	sentence_protein.doi,
	null::int as pmcid,
	null::int as pmid,
	sentence_protein.name,
	to_char(pub_date,'yyyy-WW') as week
from covid_pubchem.sentence_protein, covid_biorxiv.cohort_match natural join covid_biorxiv.biorxiv_current
where sentence_protein.doi = cohort_match.doi
;

create materialized view covid_pubchem.substances_drugs_by_week as 
select distinct
	source,
	doi,
	pmcid,
	sentence_substance.pmid,
	sentence_substance.name,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_pubchem.sentence_substance,covid_litcovid.article
where source='litcovid'
  and sentence_substance.pmid=article.pmid
union
select distinct
	source,
	doi,
	sentence_substance.pmcid,
	sentence_substance.pmid,
	sentence_substance.name,
	to_char((pub_date_year||'-'||pub_date_month||'-'||coalesce(pub_date_day,'01'))::date,'yyyy-WW') as week
from covid_pubchem.sentence_substance,covid_litcovid.article natural join covid_pmc.link
where source='pmc'
  and sentence_substance.pmcid=link.pmcid
union
select distinct
	site as source,
	sentence_substance.doi,
	null::int as pmcid,
	null::int as pmid,
	sentence_substance.name,
	to_char(pub_date,'yyyy-WW') as week
from covid_pubchem.sentence_substance, covid_biorxiv.cohort_match natural join covid_biorxiv.biorxiv_current
where sentence_substance.doi = cohort_match.doi
;
