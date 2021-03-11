select
	id,
	regexp_replace(regexp_replace(med_name__generic_, ' exclud.*', ''), ' [iI]njectable.*', '') as med,
	(select max(foo.id) from medications_concept_sets as foo where class__atc_ is not null and foo.id < bar.id) as parent
from medications_concept_sets as bar
where class__atc_ is null
  and med_name__generic_ is not null;
