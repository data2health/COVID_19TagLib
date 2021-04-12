\echo
\echo refreshing covid_pubchem genes...
\echo
\echo -n 'by DOI...     '
insert into covid_pubchem.sentence_gene select * from covid_pubchem.sentence_gene_staging where doi is not null and not exists (select doi from covid_pubchem.sentence_gene where sentence_gene.doi=sentence_gene_staging.doi);
\echo -n 'by PMCID...   '
insert into covid_pubchem.sentence_gene select * from covid_pubchem.sentence_gene_staging where pmcid > 0 and not exists (select pmcid from covid_pubchem.sentence_gene where sentence_gene.pmcid=sentence_gene_staging.pmcid);
\echo -n 'by PMID...    '
insert into covid_pubchem.sentence_gene select * from covid_pubchem.sentence_gene_staging where pmid > 0 and not exists (select pmid from covid_pubchem.sentence_gene where sentence_gene.pmid=sentence_gene_staging.pmid);
refresh materialized view covid_pubchem.genes_drugs_by_week;
