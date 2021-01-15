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
