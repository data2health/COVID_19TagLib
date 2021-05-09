\echo
\echo refreshing covid_ncats...
\echo
\echo -n 'by DOI...     '
insert into covid_ncats.sentence select * from covid_ncats.sentence_staging
where doi is not null
  and exists (select doi from covid_ncats.refresh_queue where sentence_staging.doi=refresh_queue.doi)
  and not exists (select doi from covid_ncats.sentence where sentence.doi=sentence_staging.doi);
\echo -n 'by PMCID...   '
insert into covid_ncats.sentence select * from covid_ncats.sentence_staging
where pmcid > 0
  and exists (select pmcid from covid_ncats.refresh_queue where sentence_staging.pmcid=refresh_queue.pmcid)
  and not exists (select pmcid from covid_ncats.sentence where sentence.pmcid=sentence_staging.pmcid);
\echo -n 'by PMID...    '
insert into covid_ncats.sentence select * from covid_ncats.sentence_staging
where pmid > 0
  and exists (select pmid from covid_ncats.refresh_queue where sentence_staging.pmid=refresh_queue.pmid)
  and not exists (select pmid from covid_ncats.sentence where sentence.pmid=sentence_staging.pmid);
refresh materialized view covid_ncats.drugs_by_week;
refresh materialized view covid_ncats.source_by_week;
