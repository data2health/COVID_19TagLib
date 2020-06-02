CREATE TABLE n3c_training.course (
       id SERIAL NOT NULL
     , offerer TEXT
     , title TEXT
     , description TEXT
     , enrollment_limit INT
     , duration INTERVAL(10)
     , PRIMARY KEY (id)
);

CREATE TABLE n3c_training.person (
       email TEXT NOT NULL
     , first_name TEXT
     , last_name TEXT
     , PRIMARY KEY (email)
);

CREATE TABLE n3c_training.offering (
       id INTEGER NOT NULL
     , seqnum SERIAL NOT NULL
     , delivery_time TIMESTAMP(10) WITH TIME ZONE
     , PRIMARY KEY (id, seqnum)
     , CONSTRAINT FK_offering_1 FOREIGN KEY (id)
                  REFERENCES n3c_training.course (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE n3c_training.registration (
       email TEXT NOT NULL
     , id INTEGER NOT NULL
     , seqnum INTEGER NOT NULL
     , attended BOOLEAN
     , PRIMARY KEY (email, id, seqnum)
     , CONSTRAINT FK_registration_2 FOREIGN KEY (id, seqnum)
                  REFERENCES n3c_training.offering (id, seqnum) ON DELETE CASCADE ON UPDATE CASCADE
     , CONSTRAINT FK_registration_1 FOREIGN KEY (email)
                  REFERENCES n3c_training.person (email) ON DELETE CASCADE ON UPDATE CASCADE
);

