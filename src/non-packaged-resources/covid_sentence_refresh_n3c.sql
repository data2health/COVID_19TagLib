\echo
\echo refreshing covid_n3c...
\echo
\echo -n 'by DOI...     '
insert into covid_n3c.sentence select * from covid_n3c.sentence_staging
where doi is not null
  and exists (select doi from covid_n3c.refresh_queue where sentence_staging.doi=refresh_queue.doi)
  and not exists (select doi from covid_n3c.sentence where sentence.doi=sentence_staging.doi);
\echo -n 'by PMCID...   '
insert into covid_n3c.sentence select * from covid_n3c.sentence_staging
where pmcid > 0
  and exists (select pmcid from covid_n3c.refresh_queue where sentence_staging.pmcid=refresh_queue.pmcid)
  and not exists (select pmcid from covid_n3c.sentence where sentence.pmcid=sentence_staging.pmcid);
\echo -n 'by PMID...    '
insert into covid_n3c.sentence select * from covid_n3c.sentence_staging
where pmid > 0
  and exists (select pmid from covid_n3c.refresh_queue where sentence_staging.pmid=refresh_queue.pmid)
  and not exists (select pmid from covid_n3c.sentence where sentence.pmid=sentence_staging.pmid);
refresh materialized view covid_n3c.drugs_by_week;
refresh materialized view covid_n3c.source_by_week;
