CREATE SCHEMA covid_model;

CREATE TABLE covid_model.anatomical_structure (
       id serial NOT NULL
     , anatomical_structure TEXT UNIQUE
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

CREATE TABLE covid_model.disease (
       id serial NOT NULL
     , disease TEXT UNIQUE
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

CREATE TABLE covid_model.injury (
       id serial NOT NULL
     , injury TEXT UNIQUE
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

CREATE TABLE covid_model.organism (
       id serial NOT NULL
     , organism TEXT UNIQUE
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

CREATE TABLE covid_model.pathological_function (
       id serial NOT NULL
     , pathological_function TEXT UNIQUE
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

CREATE TABLE covid_model.disease_mention (
       disease_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (disease_id, doi)
     , CONSTRAINT FK_TABLE_29_1 FOREIGN KEY (disease_id)
                  REFERENCES covid_model.disease (id)
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

CREATE TABLE covid_model.injury_mention (
       injury_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (injury_id, doi)
     , CONSTRAINT FK_TABLE_35_1 FOREIGN KEY (injury_id)
                  REFERENCES covid_model.injury (id)
);

CREATE TABLE covid_model.manufactured_object_mention (
       manufactured_object_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (manufactured_object_id, doi)
     , CONSTRAINT FK_TABLE_35_1 FOREIGN KEY (manufactured_object_id)
                  REFERENCES covid_model.manufactured_object (id)
);

CREATE TABLE covid_model.organism_mention (
       organism_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (organism_id, doi)
     , CONSTRAINT FK_TABLE_24_1 FOREIGN KEY (organism_id)
                  REFERENCES covid_model.organism (id)
);

CREATE TABLE covid_model.organic_chemical_mention (
       organic_chemical_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (organic_chemical_id, doi)
     , CONSTRAINT FK_TABLE_30_1 FOREIGN KEY (organic_chemical_id)
                  REFERENCES covid_model.organic_chemical (id)
);

CREATE TABLE covid_model.pathological_function_mention (
       pathological_function_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (pathological_function_id, doi)
     , CONSTRAINT FK_TABLE_25_1 FOREIGN KEY (pathological_function_id)
                  REFERENCES covid_model.pathological_function (id)
);

CREATE TABLE covid_model.physiological_function_mention (
       physiological_function_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (physiological_function_id, doi)
     , CONSTRAINT FK_TABLE_25_1 FOREIGN KEY (physiological_function_id)
                  REFERENCES covid_model.physiological_function (id)
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


