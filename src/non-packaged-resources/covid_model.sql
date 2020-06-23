CREATE TABLE covid_model.biological_function (
       id serial NOT NULL
     , biological_function TEXT UNIQUE
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

CREATE TABLE covid_model.technique (
       id serial NOT NULL
     , technique TEXT UNIQUE
     , umls_id TEXT
     , umls_match_string TEXT
     , PRIMARY KEY (id)
);

CREATE TABLE covid_model.biological_function_mention (
       biological_function_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (biological_function_id, doi)
     , CONSTRAINT FK_TABLE_bf_1 FOREIGN KEY (biological_function_id)
                  REFERENCES covid_model.biological_function (id)
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

CREATE TABLE covid_model.technique_mention (
       technique_id INT NOT NULL
     , doi text NOT NULL
     , PRIMARY KEY (technique_id, doi)
     , CONSTRAINT FK_TABLE_25_1 FOREIGN KEY (technique_id)
                  REFERENCES covid_model.technique (id)
);

