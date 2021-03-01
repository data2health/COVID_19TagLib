drop view sec_para_final_strike_filter cascade;

create view sec_para_final_strike_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<strike(?: [^>]*)?>([^<]*)</strike>', '\1', 'g') as p
from sec_staging1;

create view sec_para_final_monospace_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<monospace(?: [^>]*)?>([^<]*)</monospace>', '\1', 'g') as p
from sec_para_final_strike_filter;

create view sec_para_final_code_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<code(?: [^>]*)?>([^<]*)</code>', '\1', 'g') as p
from sec_para_final_monospace_filter;

create view sec_para_final_email_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<email(?: [^>]*)?>([^<]*)</email>', '\1', 'g') as p
from sec_para_final_code_filter;

create view sec_para_final_link_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<ext-link(?: [^>]*)?>([^/<]*)/>', '', 'g') as p
from sec_para_final_email_filter;

create view sec_para_final_fn_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<fn(?: [^>]*)?>([^<]*)</fn>', '\1', 'g') as p
from sec_para_final_link_filter;

create view sec_para_final_glyph_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<glyph-ref[^/>]*/>', '', 'g') as p
from sec_para_final_fn_filter;

create view sec_para_final_char_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<private-char(?: [^>]*)?>([^<]*)</private-char>', '\1', 'g') as p
from sec_para_final_glyph_filter;

create view sec_para_final_sc_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<sc(?: [^>]*)?>([^<]*)</sc>', '\1', 'g') as p
from sec_para_final_char_filter;

create view sec_para_final_xref1_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<xref(?: [^>]*)?>([^<]*)</xref>', '\1', 'g') as p
from sec_para_final_sc_filter;

create view sec_final_list_item_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from sec_para_final_xref1_filter;

create view sec_final_list_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from sec_final_list_item_filter;

create view sec_final_list_item2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from sec_final_list_filter;

create view sec_final_list2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from sec_final_list_item2_filter;

create view sec_final_p_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from sec_final_list2_filter;

create view sec_final_sec_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<sec(?: [^>]*)?>([^<]*)</sec>', '\1', 'g') as p
from sec_final_p_filter;

create view sec_final_sec2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<sec(?: [^>]*)?>([^<]*)</sec>', '\1', 'g') as p
from sec_final_sec_filter;

create view sec_final_statement_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<statement(?: [^>]*)?>([^<]*)</statement>', '\1', 'g') as p
from sec_final_sec2_filter;

create view sec_final_notes_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<notes(?: [^>]*)?>([^<]*)</notes>', '', 'g') as p
from sec_final_statement_filter;

create view sec_final_boxed_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<boxed-text(?: [^>]*)?>([^<]*)</boxed-text>', '\1', 'g') as p
from sec_final_notes_filter;

-- these next "finals" scrub what we haven't handled

create view sec_para_final_inline_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>.*?</inline-formula>', '', 'g') as p
from sec_final_boxed_filter;

create view sec_para_final_disp_group_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula-group(?: [^>]*)?>.*?</disp-formula-group>', '', 'g') as p
from sec_para_final_inline_filter;

create view sec_para_final_disp_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>.*?</disp-formula>', '', 'g') as p
from sec_para_final_disp_group_filter;

create view sec_para_final_math_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>.*?</mml:math>', '', 'g') as p
from sec_para_final_disp_filter;

create view sec_polish_p_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from sec_para_final_math_filter;

create view sec_polish_list_item_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from sec_polish_p_filter;

create view sec_polish_list_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from sec_polish_list_item_filter;

create view sec_polish_statement_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<statement(?: [^>]*)?>([^<]*)</statement>', '\1', 'g') as p
from sec_polish_list_filter;

create view sec_para_final as
select
	pmcid,
	orig,
	p
from sec_polish_statement_filter;

select * from sec_para_final where p ~ '<' limit 10;

