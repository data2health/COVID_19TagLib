create table document (
	doi text,
	title text,
	PRIMARY KEY (doi)
);

create table author (
	doi text,
	seqnum int,
	name text,
	affiliations text,
	PRIMARY KEY (doi,seqnum),
	CONSTRAINT FK_author_1 FOREIGN KEY (doi)
    	REFERENCES document (doi) ON DELETE CASCADE ON UPDATE CASCADE
);

create table institution (
	doi text,
	seqnum int,
	tag text,
	institution text,
	PRIMARY KEY (doi,seqnum),
	CONSTRAINT FK_institution_1 FOREIGN KEY (doi)
    	REFERENCES document (doi) ON DELETE CASCADE ON UPDATE CASCADE
);

create table affiliation (
	doi text,
	auth_seqnum int,
	inst_seqnum int,
	tag text,
	PRIMARY KEY (doi,auth_seqnum,inst_seqnum),
	CONSTRAINT FK_affiliation_1 FOREIGN KEY (doi,auth_seqnum)
    	REFERENCES author (doi,seqnum) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FK_affiliation_2 FOREIGN KEY (doi,inst_seqnum)
    	REFERENCES institution (doi,seqnum) ON DELETE CASCADE ON UPDATE CASCADE
);

create table reference (
	doi text,
	seqnum int,
	count int,
	pmid int,
	ref_doi text,
	name text,
	year text,
	reference text,
	PRIMARY KEY (doi,seqnum),
	CONSTRAINT FK_reference_1 FOREIGN KEY (doi)
    	REFERENCES document (doi) ON DELETE CASCADE ON UPDATE CASCADE
);

