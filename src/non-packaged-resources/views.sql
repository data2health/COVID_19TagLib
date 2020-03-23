create table raw_zotero (raw jsonb);

create materialized view note as 
select
    (raw->>'key')::text as key,
    ((raw->>'data')::jsonb->>'note')::text as note,
    ((raw->>'data')::jsonb->>'version')::text as version,
    ((raw->>'data')::jsonb->>'dateAdded')::timestamp as added,
    ((raw->>'data')::jsonb->>'dateModified')::timestamp as modified,
    ((raw->>'data')::jsonb->>'parentItem')::text as parent,
    ((raw->>'data')::jsonb->>'tags')::jsonb as tags,
    ((raw->>'data')::jsonb->>'relations')::jsonb as relations
from raw_zotero
where (raw->>'data')::jsonb->>'itemType' = 'note'
;

create materialized view note_tag as
select
    key,
    t.seqnum,
    ((t.tag::jsonb)->>'tag')::text as tag
from
    note
cross join lateral
    jsonb_array_elements_text(tags) with ordinality as t(tag,seqnum)
;

create materialized view journal_article as 
select
    (raw->>'key')::text as key,
    ((raw->>'data')::jsonb->>'DOI')::text as doi,
    ((raw->>'data')::jsonb->>'url')::text as url,
    ((raw->>'data')::jsonb->>'ISSN')::text as issn,
    ((raw->>'data')::jsonb->>'date')::text as date,
    ((raw->>'data')::jsonb->>'extra')::text as extra,
    ((raw->>'data')::jsonb->>'issue')::text as issue,
    ((raw->>'data')::jsonb->>'pages')::text as pub_date,
    ((raw->>'data')::jsonb->>'title')::text as title,
    ((raw->>'data')::jsonb->>'rights')::text as rights,
    ((raw->>'data')::jsonb->>'series')::text as series,
    ((raw->>'data')::jsonb->>'volume')::text as volume,
    ((raw->>'data')::jsonb->>'archive')::text as archive,
    ((raw->>'data')::jsonb->>'version')::text as version,
    ((raw->>'data')::jsonb->>'language')::text as language,
    ((raw->>'data')::jsonb->>'dateAdded')::timestamp as added,
    ((raw->>'data')::jsonb->>'accessDate')::text as accessed,
    ((raw->>'data')::jsonb->>'callNumber')::text as call_number,
    ((raw->>'data')::jsonb->>'seriesText')::text as series_text,
    ((raw->>'data')::jsonb->>'shortTitle')::text as short_title,
    ((raw->>'data')::jsonb->>'seriesTitle')::text as series_title,
    ((raw->>'data')::jsonb->>'abstractNote')::text as abstract,
    ((raw->>'data')::jsonb->>'dateModified')::timestamp as modified,
    ((raw->>'data')::jsonb->>'libraryCatalog')::text as library_catalog,
    ((raw->>'data')::jsonb->>'archiveLocation')::text as archive_location,
    ((raw->>'data')::jsonb->>'publicationTitle')::text as publication_title,
    ((raw->>'data')::jsonb->>'journalAbbreviation')::text as journal_abbreviation,
    ((raw->>'data')::jsonb->>'tags')::jsonb as tags,
    ((raw->>'data')::jsonb->>'creators')::jsonb as creators,
    ((raw->>'data')::jsonb->>'relations')::jsonb as relations,
    ((raw->>'data')::jsonb->>'collections')::jsonb as collections
from raw_zotero
where (raw->>'data')::jsonb->>'itemType' = 'journalArticle'
;

create materialized view journal_article_tag as
select
    key,
    t.seqnum,
    ((t.tag::jsonb)->>'tag')::text as tag
from
    journal_article
cross join lateral
    jsonb_array_elements_text(tags) with ordinality as t(tag,seqnum)
;

create materialized view journal_article_creator as
select
    key,
    t.seqnum,
    ((t.creator::jsonb)->>'lastName')::text as last_name,
    ((t.creator::jsonb)->>'firstName')::text as first_name,
    ((t.creator::jsonb)->>'creatorType')::text as creator_type
from
    journal_article
cross join lateral
    jsonb_array_elements_text(creators) with ordinality as t(creator,seqnum)
;

create materialized view journal_article_collection as
select
    key,
    t.seqnum,
    t.collection::text as collection
from
    journal_article
cross join lateral
    jsonb_array_elements_text(collections) with ordinality as t(collection,seqnum)
;

create materialized view book as 
select
    (raw->>'key')::text as key,
    ((raw->>'data')::jsonb->>'url')::text as url,
    ((raw->>'data')::jsonb->>'ISBN')::text as isbn,
    ((raw->>'data')::jsonb->>'date')::text as date,
    ((raw->>'data')::jsonb->>'extra')::text as extra,
    ((raw->>'data')::jsonb->>'place')::text as place,
    ((raw->>'data')::jsonb->>'title')::text as title,
    ((raw->>'data')::jsonb->>'rights')::text as rights,
    ((raw->>'data')::jsonb->>'series')::text as series,
    ((raw->>'data')::jsonb->>'volume')::text as volume,
    ((raw->>'data')::jsonb->>'archive')::text as archive,
    ((raw->>'data')::jsonb->>'edition')::text as edition,
    ((raw->>'data')::jsonb->>'language')::text as language,
    ((raw->>'data')::jsonb->>'version')::text as version,
    ((raw->>'data')::jsonb->>'numPages')::text as pages,
    ((raw->>'data')::jsonb->>'dateAdded')::timestamp as added,
    ((raw->>'data')::jsonb->>'publisher')::text as publisher,
    ((raw->>'data')::jsonb->>'accessDate')::text as accessed,
    ((raw->>'data')::jsonb->>'callNumber')::text as call_number,
    ((raw->>'data')::jsonb->>'shortTitle')::text as short_title,
    ((raw->>'data')::jsonb->>'abstractNote')::text as abstract,
    ((raw->>'data')::jsonb->>'dateModified')::timestamp as modified,
    ((raw->>'data')::jsonb->>'seriesNumber')::text as series_number,
    ((raw->>'data')::jsonb->>'libraryCatalog')::text as library_catalog,
    ((raw->>'data')::jsonb->>'archiveLocation')::text as archive_location,
    ((raw->>'data')::jsonb->>'numberOfVolumes')::text as number_of_volumes,
    ((raw->>'data')::jsonb->>'tags')::jsonb as tags,
    ((raw->>'data')::jsonb->>'creators')::jsonb as creators,
    ((raw->>'data')::jsonb->>'relations')::jsonb as relations,
    ((raw->>'data')::jsonb->>'collections')::jsonb as collections
from raw_zotero
where (raw->>'data')::jsonb->>'itemType' = 'book'
;

create materialized view book_tag as
select
    key,
    t.seqnum,
    ((t.tag::jsonb)->>'tag')::text as tag
from
    book
cross join lateral
    jsonb_array_elements_text(tags) with ordinality as t(tag,seqnum)
;

create materialized view book_creator as
select
    key,
    t.seqnum,
    ((t.creator::jsonb)->>'lastName')::text as last_name,
    ((t.creator::jsonb)->>'firstName')::text as first_name,
    ((t.creator::jsonb)->>'creatorType')::text as creator_type
from
    book
cross join lateral
    jsonb_array_elements_text(creators) with ordinality as t(creator,seqnum)
;

create materialized view book_collection as
select
    key,
    t.seqnum,
    t.collection::text as collection
from
    book
cross join lateral
    jsonb_array_elements_text(collections) with ordinality as t(collection,seqnum)
;

create materialized view book_section as 
select
    (raw->>'key')::text as key,
    ((raw->>'data')::jsonb->>'url')::text as url,
    ((raw->>'data')::jsonb->>'ISBN')::text as isbn,
    ((raw->>'data')::jsonb->>'date')::text as date,
    ((raw->>'data')::jsonb->>'extra')::text as extra,
    ((raw->>'data')::jsonb->>'pages')::text as pages,
    ((raw->>'data')::jsonb->>'place')::text as place,
    ((raw->>'data')::jsonb->>'title')::text as title,
    ((raw->>'data')::jsonb->>'rights')::text as rights,
    ((raw->>'data')::jsonb->>'series')::text as series,
    ((raw->>'data')::jsonb->>'volume')::text as volume,
    ((raw->>'data')::jsonb->>'archive')::text as archive,
    ((raw->>'data')::jsonb->>'edition')::text as edition,
    ((raw->>'data')::jsonb->>'language')::text as language,
    ((raw->>'data')::jsonb->>'bookTitle')::text as book_title,
    ((raw->>'data')::jsonb->>'dateAdded')::timestamp as added,
    ((raw->>'data')::jsonb->>'publisher')::text as publisher,
    ((raw->>'data')::jsonb->>'accessDate')::text as accessed,
    ((raw->>'data')::jsonb->>'callNumber')::text as call_number,
    ((raw->>'data')::jsonb->>'shortTitle')::text as short_title,
    ((raw->>'data')::jsonb->>'abstractNote')::text as abstract,
    ((raw->>'data')::jsonb->>'dateModified')::timestamp as modified,
    ((raw->>'data')::jsonb->>'seriesNumber')::text as series_number,
    ((raw->>'data')::jsonb->>'libraryCatalog')::text as library_catalog,
    ((raw->>'data')::jsonb->>'archiveLocation')::text as archive_location,
    ((raw->>'data')::jsonb->>'numberOfVolumes')::text as number_of_volumes,
    ((raw->>'data')::jsonb->>'tags')::jsonb as tags,
    ((raw->>'data')::jsonb->>'creators')::jsonb as creators,
    ((raw->>'data')::jsonb->>'relations')::jsonb as relations,
    ((raw->>'data')::jsonb->>'collections')::jsonb as collections
from raw_zotero
where (raw->>'data')::jsonb->>'itemType' = 'bookSection'
;

create materialized view book_section_tag as
select
    key,
    t.seqnum,
    ((t.tag::jsonb)->>'tag')::text as tag
from
    book_section
cross join lateral
    jsonb_array_elements_text(tags) with ordinality as t(tag,seqnum)
;

create materialized view book_section_creator as
select
    key,
    t.seqnum,
    ((t.creator::jsonb)->>'lastName')::text as last_name,
    ((t.creator::jsonb)->>'firstName')::text as first_name,
    ((t.creator::jsonb)->>'creatorType')::text as creator_type
from
    book_section
cross join lateral
    jsonb_array_elements_text(creators) with ordinality as t(creator,seqnum)
;

create materialized view book_section_collection as
select
    key,
    t.seqnum,
    t.collection::text as collection
from
    book_section
cross join lateral
    jsonb_array_elements_text(collections) with ordinality as t(collection,seqnum)
;

create materialized view report as 
select
    (raw->>'key')::text as key,
    ((raw->>'data')::jsonb->>'url')::text as url,
    ((raw->>'data')::jsonb->>'date')::text as date,
    ((raw->>'data')::jsonb->>'extra')::text as extra,
    ((raw->>'data')::jsonb->>'pages')::text as pages,
    ((raw->>'data')::jsonb->>'place')::text as place,
    ((raw->>'data')::jsonb->>'title')::text as title,
    ((raw->>'data')::jsonb->>'rights')::text as rights,
    ((raw->>'data')::jsonb->>'archive')::text as archive,
    ((raw->>'data')::jsonb->>'version')::text as version,
    ((raw->>'data')::jsonb->>'language')::text as language,
    ((raw->>'data')::jsonb->>'dateAdded')::timestamp as added,
    ((raw->>'data')::jsonb->>'accessDate')::text as accessed,
    ((raw->>'data')::jsonb->>'callNumber')::text as call_number,
    ((raw->>'data')::jsonb->>'reportType')::text as report_type,
    ((raw->>'data')::jsonb->>'shortTitle')::text as short_title,
    ((raw->>'data')::jsonb->>'institution')::text as institution,
    ((raw->>'data')::jsonb->>'seriesTitle')::text as series_title,
    ((raw->>'data')::jsonb->>'abstractNote')::text as abstract,
    ((raw->>'data')::jsonb->>'dateModified')::timestamp as modified,
    ((raw->>'data')::jsonb->>'reportNumber')::text as report_number,
    ((raw->>'data')::jsonb->>'libraryCatalog')::text as library_catalog,
    ((raw->>'data')::jsonb->>'archiveLocation')::text as archive_location,
    ((raw->>'data')::jsonb->>'tags')::jsonb as tags,
    ((raw->>'data')::jsonb->>'creators')::jsonb as creators,
    ((raw->>'data')::jsonb->>'relations')::jsonb as relations,
    ((raw->>'data')::jsonb->>'collections')::jsonb as collections
from raw_zotero
where (raw->>'data')::jsonb->>'itemType' = 'report'
;

create materialized view report_tag as
select
    key,
    t.seqnum,
    ((t.tag::jsonb)->>'tag')::text as tag
from
    report
cross join lateral
    jsonb_array_elements_text(tags) with ordinality as t(tag,seqnum)
;

create materialized view report_creator as
select
    key,
    t.seqnum,
    ((t.creator::jsonb)->>'name')::text as name,
    ((t.creator::jsonb)->>'creatorType')::text as creator_type
from
    report
cross join lateral
    jsonb_array_elements_text(creators) with ordinality as t(creator,seqnum)
;

create materialized view report_collection as
select
    key,
    t.seqnum,
    t.collection::text as collection
from
    report
cross join lateral
    jsonb_array_elements_text(collections) with ordinality as t(collection,seqnum)
;

create materialized view webpage as 
select
    (raw->>'key')::text as key,
    ((raw->>'data')::jsonb->>'url')::text as url,
    ((raw->>'data')::jsonb->>'date')::text as date,
    ((raw->>'data')::jsonb->>'extra')::text as extra,
    ((raw->>'data')::jsonb->>'title')::text as title,
    ((raw->>'data')::jsonb->>'rights')::text as rights,
    ((raw->>'data')::jsonb->>'version')::text as version,
    ((raw->>'data')::jsonb->>'language')::text as language,
    ((raw->>'data')::jsonb->>'dateAdded')::timestamp as added,
    ((raw->>'data')::jsonb->>'accessDate')::text as accessed,
    ((raw->>'data')::jsonb->>'shortTitle')::text as short_title,
    ((raw->>'data')::jsonb->>'websiteType')::text as website_type,
    ((raw->>'data')::jsonb->>'abstractNote')::text as abstract,
    ((raw->>'data')::jsonb->>'dateModified')::timestamp as modified,
    ((raw->>'data')::jsonb->>'websiteTitle')::text as website_title,
    ((raw->>'data')::jsonb->>'tags')::jsonb as tags,
    ((raw->>'data')::jsonb->>'creators')::jsonb as creators,
    ((raw->>'data')::jsonb->>'relations')::jsonb as relations,
    ((raw->>'data')::jsonb->>'collections')::jsonb as collections
from raw_zotero
where (raw->>'data')::jsonb->>'itemType' = 'webpage'
;

create materialized view webpage_tag as
select
    key,
    t.seqnum,
    ((t.tag::jsonb)->>'tag')::text as tag
from
    webpage
cross join lateral
    jsonb_array_elements_text(tags) with ordinality as t(tag,seqnum)
;

create materialized view webpage_creator as
select
    key,
    t.seqnum,
    ((t.creator::jsonb)->>'name')::text as name,
    ((t.creator::jsonb)->>'creatorType')::text as creator_type
from
    webpage
cross join lateral
    jsonb_array_elements_text(creators) with ordinality as t(creator,seqnum)
;

create materialized view webpage_collection as
select
    key,
    t.seqnum,
    t.collection::text as collection
from
    webpage
cross join lateral
    jsonb_array_elements_text(collections) with ordinality as t(collection,seqnum)
;

///////////////////////

create materialized view biorxiv_current as 
select
    (raw->>'rel_doi')::text as doi,
    (raw->>'rel_title')::text as title,
    (raw->>'rel_date')::date as pub_date
from raw_biorxiv
;
