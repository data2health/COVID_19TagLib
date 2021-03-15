create view medication as
select
	id,
	regexp_replace(regexp_replace(med_name__generic_, ' exclud.*', ''), ' [iI]njectable.*', '') as medication,
	'['||lower(substring(med_name__generic_ from 1 for 1))||upper(substring(med_name__generic_ from 1 for 1))||']'||substring(med_name__generic_ from 2) as pattern,
	priority,
	rxcui,
	(select max(foo.id) from medications_concept_sets as foo where class__atc_ is not null and foo.id < bar.id) as class_id,
	(select class__atc_ from medications_concept_sets where id in (select max(foo.id) from medications_concept_sets as foo where class__atc_ is not null and foo.id < bar.id)) as class_name
from medications_concept_sets as bar
where class__atc_ is null
  and med_name__generic_ is not null;
