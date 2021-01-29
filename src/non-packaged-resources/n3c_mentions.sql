select sentence.doi,full_text,refnum,reference
from sentence natural join citation,reference
where sentence.doi=reference.doi
  and citation.refnum=reference.seqnum
  and sentence.doi in (select doi from n3c_mention_suppress where not suppress)
  and reference ~'[nN]3[cC]';

select doi,seqnum,sentnum,full_text
from covid_biorxiv.sentence
where full_text ~'[nN]3[cC]'
  and doi in (select doi from n3c_mention_suppress where not suppress)
order by 1,2,3;

select doi,seqnum,reference
from covid_biorxiv.reference
where reference ~'[nN]3[cC]'
  and doi in (select doi from n3c_mention_suppress where not suppress)
order by 1,2;

create view covid_biorxiv.cohort_med as
select  temp as original, '(^|[^a-zA-Z])['||lower(substring(temp from 1 for 1))||upper(substring(temp from 1 for 1))||']'||lower(substring(temp from 2)) as normalized
from
(select regexp_replace(regexp_replace(value,'_gtt',''),'_',' ','g') as temp
from enclave_cohort.med_use_frequency_for_export
where value!~'systemic' and value!='Totals'
) as foo;

create materialized view covid_biorxiv.cohort_match as
select distinct doi, seqnum, name, sentnum, full_text, original
from covid_biorxiv.sentence natural join covid_biorxiv.section, covid_biorxiv.cohort_med
where full_text ~ original or full_text ~ normalized
;

CREATE MATERIALIZED VIEW umls_local.disease AS
SELECT
	mrsty.cui,
    mrsty.stn,
    mrsty.sty,
    uistr.str,
    ((('(^|[^a-zA-Z])[' || lower(substring(uistr.str, 1, 1))) || upper(substring(uistr.str, 1, 1))) || ']') || lower(substring(uistr.str, 2))||'([^a-zA-Z0-9]|$)' AS normalized
FROM umls.mrsty NATURAL JOIN uistr
WHERE mrsty.stn ~ '^B2.2.1.2'
  AND uistr.str ~ '^[a-zA-Z0-9 ]+$';

create materialized view covid_biorxiv.umls_disease_match as
select doi,seqnum,sentnum,cui,str
from covid_biorxiv.sentence,disease
where full_text ~ normalized
;

create materialized view cohort_med_site_count as select original,site,count(*) from biorxiv_current natural join sentence,cohort_med where full_text ~ normalized group by 1,2;

----------- PubChem ----------

create materialized view compound as
select cid,cmpdname as name, unnest(string_to_array(cmpdsynonym,'|')) as synonym from pubchem_compound_text_covid_19;

create materialized view gene as
select geneid,srcname,title as name, unnest(string_to_array(synos,'|')) as synonym from pubchem_gene_text_covid_19 where synos != 'NULL'
union
select geneid,srcname,title as name, trim(substring(title from '([^ ]+) - ')) as synonym from pubchem_gene_text_covid_19 where synos ='NULL'
union
select geneid,srcname,title as name, trim(substring(title from ' - ([^(]+)')) as synonym from pubchem_gene_text_covid_19 where synos ='NULL'
;

create materialized view protein as
select protacxn,srcname,title as name, unnest(string_to_array(synos,'|')) as synonym from pubchem_protein_text_covid_19
union
select protacxn,srcname,title as name, trim(substring(title from '(.+)[(][^)]+[)]$')) as synonym from pubchem_protein_text_covid_19
;

create materialized view substance as
select sid, substring(subssynonym from '[^|]+') as name, unnest(string_to_array(subssynonym,'|')) as synonym from pubchem_substance_text_covid_19
;
