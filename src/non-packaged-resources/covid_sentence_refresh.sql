\echo
\echo refreshing covid_n3c...
\echo
\echo -n 'by DOI...     '
insert into covid_n3c.sentence select * from covid_n3c.sentence_staging where doi is not null and not exists (select doi from covid_n3c.sentence where sentence.doi=sentence_staging.doi);
\echo -n 'by PMCID...   '
insert into covid_n3c.sentence select * from covid_n3c.sentence_staging where pmcid > 0 and not exists (select pmcid from covid_n3c.sentence where sentence.pmcid=sentence_staging.pmcid);
\echo -n 'by PMID...    '
insert into covid_n3c.sentence select * from covid_n3c.sentence_staging where pmid > 0 and not exists (select pmid from covid_n3c.sentence where sentence.pmid=sentence_staging.pmid);
refresh materialized view covid_n3c.drugs_by_week;
refresh materialized view covid_n3c.source_by_week;

\echo
\echo refreshing covid_ncats...
\echo
\echo -n 'by DOI...     '
insert into covid_ncats.sentence select * from covid_ncats.sentence_staging where doi is not null and not exists (select doi from covid_ncats.sentence where sentence.doi=sentence_staging.doi);
\echo -n 'by PMCID...   '
insert into covid_ncats.sentence select * from covid_ncats.sentence_staging where pmcid > 0 and not exists (select pmcid from covid_ncats.sentence where sentence.pmcid=sentence_staging.pmcid);
\echo -n 'by PMID...    '
insert into covid_ncats.sentence select * from covid_ncats.sentence_staging where pmid > 0 and not exists (select pmid from covid_ncats.sentence where sentence.pmid=sentence_staging.pmid);
refresh materialized view covid_ncats.drugs_by_week;
refresh materialized view covid_ncats.source_by_week;

\echo
\echo refreshing covid_pubchem compounds...
\echo
\echo -n 'by DOI...     '
insert into covid_pubchem.sentence_compound select * from covid_pubchem.sentence_compound_staging where doi is not null and not exists (select doi from covid_pubchem.sentence_compound where sentence_compound.doi=sentence_compound_staging.doi);
\echo -n 'by PMCID...   '
insert into covid_pubchem.sentence_compound select * from covid_pubchem.sentence_compound_staging where pmcid > 0 and not exists (select pmcid from covid_pubchem.sentence_compound where sentence_compound.pmcid=sentence_compound_staging.pmcid);
\echo -n 'by PMID...    '
insert into covid_pubchem.sentence_compound select * from covid_pubchem.sentence_compound_staging where pmid > 0 and not exists (select pmid from covid_pubchem.sentence_compound where sentence_compound.pmid=sentence_compound_staging.pmid);
refresh materialized view covid_pubchem.compounds_drugs_by_week;

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