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
	tag text,
	institution text,
	PRIMARY KEY (doi,tag),
	CONSTRAINT FK_institution_1 FOREIGN KEY (doi)
    	REFERENCES document (doi) ON DELETE CASCADE ON UPDATE CASCADE
);

create table affiliation (
	doi text,
	seqnum int,
	tag text,
	PRIMARY KEY (doi,seqnum,tag),
	CONSTRAINT FK_affiliation_1 FOREIGN KEY (doi,seqnum)
    	REFERENCES author (doi,seqnum) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FK_affiliation_2 FOREIGN KEY (doi,tag)
    	REFERENCES institution (doi,tag) ON DELETE CASCADE ON UPDATE CASCADE
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

create table reference_stats (
	doi text,
	lines int,
	refs int,
	PRIMARY KEY (doi),
	CONSTRAINT FK_reference_stats_1 FOREIGN KEY (doi)
    	REFERENCES document (doi) ON DELETE CASCADE ON UPDATE CASCADE
);
