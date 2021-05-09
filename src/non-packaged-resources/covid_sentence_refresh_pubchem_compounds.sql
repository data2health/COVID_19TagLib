\echo
\echo refreshing covid_pubchem compounds...
\echo
\echo -n 'by DOI...     '
insert into covid_pubchem.sentence_compound select * from covid_pubchem.sentence_compound_staging
where doi is not null
  and exists (select doi from covid_pubchem.refresh_queue where sentence_staging.doi=refresh_queue.doi)
  and not exists (select doi from covid_pubchem.sentence_compound where sentence_compound.doi=sentence_compound_staging.doi);
\echo -n 'by PMCID...   '
insert into covid_pubchem.sentence_compound select * from covid_pubchem.sentence_compound_staging
where pmcid > 0
  and exists (select pmcid from covid_pubchem.refresh_queue where sentence_staging.pmcid=refresh_queue.pmcid)
  and not exists (select pmcid from covid_pubchem.sentence_compound where sentence_compound.pmcid=sentence_compound_staging.pmcid);
\echo -n 'by PMID...    '
insert into covid_pubchem.sentence_compound select * from covid_pubchem.sentence_compound_staging
where pmid > 0
  and exists (select pmid from covid_pubchem.refresh_queue where sentence_staging.pmid=refresh_queue.pmid)
  and not exists (select pmid from covid_pubchem.sentence_compound where sentence_compound.pmid=sentence_compound_staging.pmid);
refresh materialized view covid_pubchem.compounds_drugs_by_week;
