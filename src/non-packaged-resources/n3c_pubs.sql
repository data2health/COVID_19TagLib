create table n3c_pubs.match(ms_id text, doi text, pmid int);

create table n3c_pubs.suppress(doi text, pmid int);

select corresponding_author_last_name as author,
	count(*)
from n3c_pubs.manuscript
where not exists (select ms_id from n3c_pubs.match where manuscript.ms_id=match.ms_id)
  and exists (select doi from covid_biorxiv.author
  			  where author.name ~ corresponding_author_last_name
  			    and not exists (select * from n3c_pubs.suppress where suppress.doi = author.doi)
  			  union
  			  select pmid::text from covid_litcovid.author
  			  where author.last_name = corresponding_author_last_name
  			    and not exists (select * from n3c_pubs.suppress where suppress.pmid = author.pmid)
  			  )
group by 1
order by 1
;

select
	ms_id,
	tentative_manuscript_or_publication_title as title,
	corresponding_author_last_name as author_last_name,
	corresponding_author_name as author_name
from n3c_pubs.manuscript
where not exists (select ms_id from n3c_pubs.match where match.ms_id = manuscript.ms_id)
  and corresponding_author_last_name = 'Haendel'
;

select
	doi,
	title,
	seqnum,
	name
from covid_biorxiv.document natural join covid_biorxiv.author
where not exists (select doi from n3c_pubs.suppress where suppress.doi=document.doi)
  and name ~'Haendel';

select
	article_title.pmid,
	article_title,
	author.seqnum,
	last_name,
	fore_name
from covid_litcovid.article_title, covid_litcovid.author
where article_title.pmid=author.pmid
  and not exists (select pmid from n3c_pubs.suppress where suppress.pmid=article_title.pmid)
  and author.last_name='Haendel'
;

create view n3c_pubs.litcovid_staging as
SELECT 
	match.pmid,
	article_title as title,
	medline_ta as journal,
	volume,
	issue,
	(pub_date_year||'-'||pub_date_month||'-'||pub_date_day)::date as published,
	medline_pgn as pages,
	(select jsonb_agg(bar) from (select seqnum,last_name,fore_name as first_name from covid_litcovid.author where author.pmid=article.pmid order by seqnum) as bar) as authors,
	pmcid
FROM covid_litcovid.medline_journal_info natural join covid_litcovid.article_title natural join covid_litcovid.article natural join n3c_pubs.match natural left outer join n3c_pubs.pmc_staging
;

create table n3c_pubs.litcovid_cache as select * from n3c_pubs.litcovid_staging ;

create view n3c_pubs.biorxiv_staging as
select
	doi,
	title,
	site,
	pub_date,
	(select jsonb_agg(bar) from (select rank,name,institution from covid_biorxiv.biorxiv_current_author where biorxiv_current_author.doi=biorxiv_current.doi order by rank) as bar) as authors
from covid_biorxiv.biorxiv_current natural join n3c_pubs.match
;
create table n3c_pubs.biorxiv_cache as select * from n3c_pubs.biorxiv_staging;
