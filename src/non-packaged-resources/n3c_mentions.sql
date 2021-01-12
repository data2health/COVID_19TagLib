select sentence.doi,full_text,refnum,reference
from sentence natural join citation,reference
where sentence.doi=reference.doi
  and citation.refnum=reference.seqnum
  and sentence.doi in (select doi from n3c_mention_suppress where not suppress)
  and reference ~'[nN]3[cC]';

select doi,seqnum,sentnum,full_text
from covid_biorxiv.sentence
where full_text ~'[nN]3[cC]'
  and doi in (select doi from n3c_mention_suppress where not suppress)
order by 1,2,3;

select doi,seqnum,reference
from covid_biorxiv.reference
where reference ~'[nN]3[cC]'
  and doi in (select doi from n3c_mention_suppress where not suppress)
order by 1,2;
