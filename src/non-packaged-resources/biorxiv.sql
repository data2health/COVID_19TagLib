CREATE TABLE covid_biorxiv.raw_biorxiv (
    doi text PRIMARY KEY,
    raw jsonb
);

CREATE MATERIALIZED VIEW covid_biorxiv.biorxiv_current AS
 SELECT (raw_biorxiv.raw ->> 'rel_doi'::text) AS doi,
    (raw_biorxiv.raw ->> 'rel_title'::text) AS title,
    (raw_biorxiv.raw ->> 'rel_authors'::text) AS authors,
    (raw_biorxiv.raw ->> 'rel_link'::text) AS link,
    (raw_biorxiv.raw ->> 'rel_site'::text) AS site,
    ((raw_biorxiv.raw ->> 'rel_date'::text))::date AS pub_date,
    (raw_biorxiv.raw ->> 'rel_abs'::text) AS abstract
   FROM covid_biorxiv.raw_biorxiv
  WITH NO DATA;

create table document (
	doi text,
	title text,
	PRIMARY KEY (doi)
);

create table section (
	doi text,
	seqnum int,
	cateogry text,
	label text,
	PRIMARY KEY (doi,seqnum),
	CONSTRAINT FK_section_1 FOREIGN KEY (doi)
    	REFERENCES document (doi) ON DELETE CASCADE ON UPDATE CASCADE
);

create table sentence (
    doi text,
    seqnum int,
    sentnum int,
    full_text text,
    trimmed_text text,
    pos_tags text,
    PRIMARY KEY (doi,seqnum,sentnum),
    CONSTRAINT FK_sentence_1 FOREIGN KEY (doi,seqnum)
        REFERENCES section (doi,seqnum) ON DELETE CASCADE ON UPDATE CASCADE
);

create table parse (
    doi text,
    seqnum int,
    sentnum int,
    parsenum int,
    parse text,
    PRIMARY KEY (doi,seqnum,sentnum,parsenum),
    CONSTRAINT FK_parse_1 FOREIGN KEY (doi,seqnum,sentnum)
        REFERENCES sentence (doi,seqnum,sentnum) ON DELETE CASCADE ON UPDATE CASCADE
);

create table fragment (
    doi text,
    seqnum int,
    sentnum int,
    node text,
    fragment text,
    CONSTRAINT FK_fragment_1 FOREIGN KEY (doi,seqnum,sentnum)
        REFERENCES sentence (doi,seqnum,sentnum) ON DELETE CASCADE ON UPDATE CASCADE
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

create table citation (
	doi text,
	seqnum int,
	sentnum int,
	refnum int,
	PRIMARY KEY (doi,seqnum,sentnum,refnum),
	CONSTRAINT FK_citation_1 FOREIGN KEY (doi,seqnum,sentnum)
    	REFERENCES sentence (doi,seqnum,sentnum) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FK_citation_2 FOREIGN KEY (doi,refnum)
    	REFERENCES reference (doi,seqnum) ON DELETE CASCADE ON UPDATE CASCADE
);

create table table_mention (
	doi text,
	seqnum int,
	sentnum int,
	table_mention text,
	PRIMARY KEY (doi,seqnum,sentnum,table_mention),
	CONSTRAINT FK_table_mention_1 FOREIGN KEY (doi,seqnum,sentnum)
    	REFERENCES sentence (doi,seqnum,sentnum) ON DELETE CASCADE ON UPDATE CASCADE
);

create table figure_mention (
	doi text,
	seqnum int,
	sentnum int,
	figure_mention text,
	PRIMARY KEY (doi,seqnum,sentnum,figure_mention),
	CONSTRAINT FK_table_mention_1 FOREIGN KEY (doi,seqnum,sentnum)
    	REFERENCES sentence (doi,seqnum,sentnum) ON DELETE CASCADE ON UPDATE CASCADE
);

create table reference_stats (
	doi text,
	lines int,
	refs int,
	PRIMARY KEY (doi),
	CONSTRAINT FK_reference_stats_1 FOREIGN KEY (doi)
    	REFERENCES document (doi) ON DELETE CASCADE ON UPDATE CASCADE
);
