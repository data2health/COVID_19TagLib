\echo
\echo refreshing covid_pubchem substances...
\echo
\echo -n 'by DOI...     '
insert into covid_pubchem.sentence_substance select * from covid_pubchem.sentence_substance_staging where doi is not null and not exists (select doi from covid_pubchem.sentence_substance where sentence_substance.doi=sentence_substance_staging.doi);
\echo -n 'by PMCID...   '
insert into covid_pubchem.sentence_substance select * from covid_pubchem.sentence_substance_staging where pmcid > 0 and not exists (select pmcid from covid_pubchem.sentence_substance where sentence_substance.pmcid=sentence_substance_staging.pmcid);
\echo -n 'by PMID...    '
insert into covid_pubchem.sentence_substance select * from covid_pubchem.sentence_substance_staging where pmid > 0 and not exists (select pmid from covid_pubchem.sentence_substance where sentence_substance.pmid=sentence_substance_staging.pmid);
refresh materialized view covid_pubchem.substances_drugs_by_week;
