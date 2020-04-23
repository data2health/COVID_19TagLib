create view raw_message as
select
	doi,
    (raw->>'message')::jsonb as message
from
	raw_crossref
;

create materialized view research_output as
select
	doi,
    (message->>'type')::text as type,
    (message->>'source')::text as source,
    (message->>'subtype')::text as subtype,
    (message->>'abstract')::text as abstract,
    (message->>'publisher')::text as publisher,
    (message->>'group-title')::text as group_title,
    (((message->>'institution')::jsonb)->>'name')::text as institution_name,
    true
from
	raw_message
;

create materialized view link as
select
    doi,
    t.seqnum,
    ((t.link)::jsonb->>'URL')::text as url,
    ((t.link)::jsonb->>'content-type')::text as content_type,
    ((t.link)::jsonb->>'content-version')::text as content_version,
    ((t.link)::jsonb->>'intended-application')::text as intended_application
from
    raw_message
cross join lateral
    jsonb_array_elements_text((message->>'link')::jsonb) with ordinality as t(link,seqnum)
;

create materialized view title as
select
    doi,
    t.seqnum,
    t.title
from
    raw_message
cross join lateral
    jsonb_array_elements_text((message->>'title')::jsonb) with ordinality as t(title,seqnum)
;

create materialized view subtitle as
select
    doi,
    t.seqnum,
    t.sub_title
from
    raw_message
cross join lateral
    jsonb_array_elements_text((message->>'subtitle')::jsonb) with ordinality as t(sub_title,seqnum)
;

create materialized view short_title as
select
    doi,
    t.seqnum,
    t.short_title
from
    raw_message
cross join lateral
    jsonb_array_elements_text((message->>'short-title')::jsonb) with ordinality as t(short_title,seqnum)
;

create materialized view container_title as
select
    doi,
    t.seqnum,
    t.container_title
from
    raw_message
cross join lateral
    jsonb_array_elements_text((message->>'container-title')::jsonb) with ordinality as t(container_title,seqnum)
;

create materialized view author as
select
    doi,
    t.seqnum,
    ((t.author)::jsonb->>'ORCID')::text as orcid,
    ((t.author)::jsonb->>'given')::text as given,
    ((t.author)::jsonb->>'family')::text as family,
    ((t.author)::jsonb->>'sequence')::text as sequence,
    ((t.author)::jsonb->>'authenticated-orcid')::boolean as authenticated_orcid,
    ((t.author)::jsonb->>'affiliation')::jsonb as affiliation
from
    raw_message
cross join lateral
    jsonb_array_elements_text((message->>'author')::jsonb) with ordinality as t(author,seqnum)
;

create materialized view reference as
select
    doi,
    t.seqnum,
    ((t.ref)::jsonb->>'DOI')::text as ref_doi,
    ((t.ref)::jsonb->>'key')::text as key,
    ((t.ref)::jsonb->>'doi-asserted-by')::text as doi_asserted_by,
    ((t.ref)::jsonb->>'unstructured')::text as unstructured,
    ((t.ref)::jsonb->>'year')::text as year,
    ((t.ref)::jsonb->>'volume')::text as volume,
    ((t.ref)::jsonb->>'first-page')::text as first_page,
    ((t.ref)::jsonb->>'article-title')::text as article_title,
    ((t.ref)::jsonb->>'journal-title')::text as journal_title
from
    raw_message
cross join lateral
    jsonb_array_elements_text((message->>'reference')::jsonb) with ordinality as t(ref,seqnum)
;
