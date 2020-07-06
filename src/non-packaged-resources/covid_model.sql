CREATE SCHEMA covid_model;

CREATE TABLE covid_model.anatomical_structure (
       id serial NOT NULL
     , anatomical_structure TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.activity (
       id serial NOT NULL
     , activity TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.biological_function (
       id serial NOT NULL
     , biological_function TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.body_part (
       id serial NOT NULL
     , body_part TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.concept (
       id serial NOT NULL
     , concept TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.conceptual_relationship (
       id serial NOT NULL
     , conceptual_relationship TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.discipline (
       id serial NOT NULL
     , discipline TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.disease (
       id serial NOT NULL
     , disease TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.entity (
       id serial NOT NULL
     , entity TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.event (
       id serial NOT NULL
     , event TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.finding (
       id serial NOT NULL
     , finding TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.functional_relationship (
       id serial NOT NULL
     , functional_relationship TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.grp (
       id serial NOT NULL
     , grp TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.group_attribute (
       id serial NOT NULL
     , group_attribute TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.human_process (
       id serial NOT NULL
     , human_process TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.injury (
       id serial NOT NULL
     , injury TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.intellectual_product (
       id serial NOT NULL
     , intellectual_product TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.language (
       id serial NOT NULL
     , language TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.manufactured_object (
       id serial NOT NULL
     , manufactured_object TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.natural_process (
       id serial NOT NULL
     , natural_process TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.organism (
       id serial NOT NULL
     , organism TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.organism_attribute (
       id serial NOT NULL
     , organism_attribute TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.organic_chemical (
       id serial NOT NULL
     , organic_chemical TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.organization (
       id serial NOT NULL
     , organization TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.pathological_function (
       id serial NOT NULL
     , pathological_function TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.physical_relationship (
       id serial NOT NULL
     , physical_relationship TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.physiological_function (
       id serial NOT NULL
     , physiological_function TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.process (
       id serial NOT NULL
     , process TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.relationship (
       id serial NOT NULL
     , relationship TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.spatial_relationship (
       id serial NOT NULL
     , spatial_relationship TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.substance (
       id serial NOT NULL
     , substance TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.temporal_relationship (
       id serial NOT NULL
     , temporal_relationship TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.transcription_factor (
       id serial NOT NULL
     , transcription_factor TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.technique (
       id serial NOT NULL
     , technique TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.activity_mention (
       activity_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (activity_id, doi)
     , CONSTRAINT FK_TABLE_activity_1 FOREIGN KEY (activity_id)
                  REFERENCES covid_model.activity (id)
);

CREATE TABLE covid_model.anatomical_structure_mention (
       anatomical_structure_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (anatomical_structure_id, doi)
     , CONSTRAINT FK_TABLE_as_1 FOREIGN KEY (anatomical_structure_id)
                  REFERENCES covid_model.anatomical_structure (id)
);

CREATE TABLE covid_model.biological_function_mention (
       biological_function_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (biological_function_id, doi)
     , CONSTRAINT FK_TABLE_bf_1 FOREIGN KEY (biological_function_id)
                  REFERENCES covid_model.biological_function (id)
);

CREATE TABLE covid_model.body_part_mention (
       body_part_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (body_part_id, doi)
     , CONSTRAINT FK_TABLE_bp_1 FOREIGN KEY (body_part_id)
                  REFERENCES covid_model.body_part (id)
);

CREATE TABLE covid_model.concept_mention (
       concept_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (concept_id, doi)
     , CONSTRAINT FK_TABLE_concept_1 FOREIGN KEY (concept_id)
                  REFERENCES covid_model.concept (id)
);

CREATE TABLE covid_model.conceptual_relationship_mention (
       conceptual_relationship_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (conceptual_relationship_id, doi)
     , CONSTRAINT FK_TABLE_conceptual_relationship_id_1 FOREIGN KEY (conceptual_relationship_id)
                  REFERENCES covid_model.conceptual_relationship (id)
);

CREATE TABLE covid_model.discipline_mention (
       discipline_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (discipline_id, doi)
     , CONSTRAINT FK_TABLE_discipline_1 FOREIGN KEY (discipline_id)
                  REFERENCES covid_model.discipline (id)
);

CREATE TABLE covid_model.disease_mention (
       disease_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (disease_id, doi)
     , CONSTRAINT FK_TABLE_29_1 FOREIGN KEY (disease_id)
                  REFERENCES covid_model.disease (id)
);

CREATE TABLE covid_model.entity_mention (
       entity_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (entity_id, doi)
     , CONSTRAINT FK_TABLE_entity_1 FOREIGN KEY (entity_id)
                  REFERENCES covid_model.entity (id)
);

CREATE TABLE covid_model.event_mention (
       event_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (event_id, doi)
     , CONSTRAINT FK_TABLE_35_1 FOREIGN KEY (event_id)
                  REFERENCES covid_model.event (id)
);

CREATE TABLE covid_model.finding_mention (
       finding_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (finding_id, doi)
     , CONSTRAINT FK_TABLE_35_1 FOREIGN KEY (finding_id)
                  REFERENCES covid_model.finding (id)
);

CREATE TABLE covid_model.functional_relationship_mention (
       functional_relationship_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (functional_relationship_id, doi)
     , CONSTRAINT FK_TABLE_functional_relationship_1 FOREIGN KEY (functional_relationship_id)
                  REFERENCES covid_model.functional_relationship (id)
);

CREATE TABLE covid_model.group_mention (
       group_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (group_id, doi)
     , CONSTRAINT FK_TABLE_group_1 FOREIGN KEY (group_id)
                  REFERENCES covid_model.grp (id)
);

CREATE TABLE covid_model.group_attribute_mention (
       group_attribute_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (group_attribute_id, doi)
     , CONSTRAINT FK_TABLE_group_attribute_1 FOREIGN KEY (group_attribute_id)
                  REFERENCES covid_model.group_attribute (id)
);

CREATE TABLE covid_model.human_process_mention (
       human_process_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (human_process_id, doi)
     , CONSTRAINT FK_TABLE_human_process_1 FOREIGN KEY (human_process_id)
                  REFERENCES covid_model.human_process (id)
);

CREATE TABLE covid_model.injury_mention (
       injury_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (injury_id, doi)
     , CONSTRAINT FK_TABLE_35_1 FOREIGN KEY (injury_id)
                  REFERENCES covid_model.injury (id)
);

CREATE TABLE covid_model.intellectual_product_mention (
       intellectual_product_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (intellectual_product_id, doi)
     , CONSTRAINT FK_TABLE_intellectual_product_1 FOREIGN KEY (intellectual_product_id)
                  REFERENCES covid_model.intellectual_product (id)
);

CREATE TABLE covid_model.language_mention (
       language_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (language_id, doi)
     , CONSTRAINT FK_TABLE_language_1 FOREIGN KEY (language_id)
                  REFERENCES covid_model.language (id)
);

CREATE TABLE covid_model.manufactured_object_mention (
       manufactured_object_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (manufactured_object_id, doi)
     , CONSTRAINT FK_TABLE_35_1 FOREIGN KEY (manufactured_object_id)
                  REFERENCES covid_model.manufactured_object (id)
);

CREATE TABLE covid_model.natural_process_mention (
       natural_process_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (natural_process_id, doi)
     , CONSTRAINT FK_TABLE_natural_process_1 FOREIGN KEY (natural_process_id)
                  REFERENCES covid_model.natural_process (id)
);

CREATE TABLE covid_model.organism_mention (
       organism_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (organism_id, doi)
     , CONSTRAINT FK_TABLE_24_1 FOREIGN KEY (organism_id)
                  REFERENCES covid_model.organism (id)
);

CREATE TABLE covid_model.organism_attribute_mention (
       organism_attribute_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (organism_attribute_id, doi)
     , CONSTRAINT FK_TABLE_organism_attribute_1 FOREIGN KEY (organism_attribute_id)
                  REFERENCES covid_model.organism_attribute (id)
);

CREATE TABLE covid_model.organic_chemical_mention (
       organic_chemical_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (organic_chemical_id, doi)
     , CONSTRAINT FK_TABLE_30_1 FOREIGN KEY (organic_chemical_id)
                  REFERENCES covid_model.organic_chemical (id)
);

CREATE TABLE covid_model.organization_mention (
       organization_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (organization_id, doi)
     , CONSTRAINT FK_TABLE_organization_1 FOREIGN KEY (organization_id)
                  REFERENCES covid_model.organization (id)
);

CREATE TABLE covid_model.pathological_function_mention (
       pathological_function_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (pathological_function_id, doi)
     , CONSTRAINT FK_TABLE_25_1 FOREIGN KEY (pathological_function_id)
                  REFERENCES covid_model.pathological_function (id)
);

CREATE TABLE covid_model.physical_relationship_mention (
       physical_relationship_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (physical_relationship_id, doi)
     , CONSTRAINT FK_TABLE_physical_relationship_1 FOREIGN KEY (physical_relationship_id)
                  REFERENCES covid_model.physical_relationship (id)
);

CREATE TABLE covid_model.physiological_function_mention (
       physiological_function_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (physiological_function_id, doi)
     , CONSTRAINT FK_TABLE_25_1 FOREIGN KEY (physiological_function_id)
                  REFERENCES covid_model.physiological_function (id)
);

CREATE TABLE covid_model.process_mention (
       process_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (process_id, doi)
     , CONSTRAINT FK_TABLE_process_1 FOREIGN KEY (process_id)
                  REFERENCES covid_model.process (id)
);

CREATE TABLE covid_model.relationship_mention (
       relationship_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (relationship_id, doi)
     , CONSTRAINT FK_TABLE_relationship_1 FOREIGN KEY (relationship_id)
                  REFERENCES covid_model.relationship (id)
);

CREATE TABLE covid_model.spatial_relationship_mention (
       spatial_relationship_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (spatial_relationship_id, doi)
     , CONSTRAINT FK_TABLE_spatial_relationship_1 FOREIGN KEY (spatial_relationship_id)
                  REFERENCES covid_model.spatial_relationship (id)
);

CREATE TABLE covid_model.substance_mention (
       substance_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (substance_id, doi)
     , CONSTRAINT FK_TABLE_substance_1 FOREIGN KEY (substance_id)
                  REFERENCES covid_model.substance (id)
);

CREATE TABLE covid_model.temporal_relationship_mention (
       temporal_relationship_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (temporal_relationship_id, doi)
     , CONSTRAINT FK_TABLE_temporal_relationship_1 FOREIGN KEY (temporal_relationship_id)
                  REFERENCES covid_model.temporal_relationship (id)
);

CREATE TABLE covid_model.transcription_factor_mention (
       transcription_factor_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (transcription_factor_id, doi)
     , CONSTRAINT FK_TABLE_25_1 FOREIGN KEY (transcription_factor_id)
                  REFERENCES covid_model.transcription_factor (id)
);


CREATE TABLE covid_model.technique_mention (
       technique_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (technique_id, doi)
     , CONSTRAINT FK_TABLE_25_1 FOREIGN KEY (technique_id)
                  REFERENCES covid_model.technique (id)
);


