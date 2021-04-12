\echo
\echo refreshing covid_pubchem proteins...
\echo
\echo -n 'by DOI...     '
insert into covid_pubchem.sentence_protein select * from covid_pubchem.sentence_protein_staging where doi is not null and not exists (select doi from covid_pubchem.sentence_protein where sentence_protein.doi=sentence_protein_staging.doi);
\echo -n 'by PMCID...   '
insert into covid_pubchem.sentence_protein select * from covid_pubchem.sentence_protein_staging where pmcid > 0 and not exists (select pmcid from covid_pubchem.sentence_protein where sentence_protein.pmcid=sentence_protein_staging.pmcid);
\echo -n 'by PMID...    '
insert into covid_pubchem.sentence_protein select * from covid_pubchem.sentence_protein_staging where pmid > 0 and not exists (select pmid from covid_pubchem.sentence_protein where sentence_protein.pmid=sentence_protein_staging.pmid);
refresh materialized view covid_pubchem.proteins_drugs_by_week;
