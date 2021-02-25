-- a cascading chain of views to scrub XML markup from PMC article paragraphs

drop view sec_para_filter cascade;

create view sec_para_filter as
select
	pmcid,
	p as orig,
	regexp_replace(p::text, '<p[^>]*>(.*)</p>', '\1') as p
from section_paragraph;

create view sec_para_flat_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '[\n\r\t ]+', ' ', 'g') as p
from sec_para_filter;

create view sec_para_sup1_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<sup>([^<]*)</sup>', '\1', 'g') as p
from sec_para_flat_filter;

create view sec_para_italic1_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<italic>([^<]*)</italic>', '\1', 'g') as p
from sec_para_sup1_filter;

create view sec_para_bold1_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<bold>([^<]*)</bold>', '\1', 'g') as p
from sec_para_italic1_filter;

create view sec_para_monospace_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<monospace>([^<]*)</monospace>', '\1', 'g') as p
from sec_para_bold1_filter;

create view sec_para_bib_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '([\[(]<xref[^>]*bibr[^>]*>[^<]*</xref>( *[,;] *<xref[^>]*bibr[^>]*>[^<]*</xref>)*[\])])', '', 'g') as p
from sec_para_monospace_filter;

create view sec_para_bib2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '(<xref[^>]*bibr[^>]*>[^<]*</xref>( *[,;] *<xref[^>]*bibr[^>]*>[^<]*</xref>)*)', '', 'g') as p
from sec_para_bib_filter;

create view sec_para_bib3_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<xref[^>]*bibr[^>]*/>[-–]?', '', 'g') as p
from sec_para_bib2_filter;

create view sec_para_xref_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<xref[^>]*>([^<]*)</xref>', '\1', 'g') as p
from sec_para_bib3_filter;

create view sec_para_fig_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<fig [^>]*>.*</fig>', '', 'g') as p
from sec_para_xref_filter;

create view sec_para_table_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap [^>]*>.*</table-wrap>', '', 'g') as p
from sec_para_fig_filter;

create view sec_para_sub_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<sub>([^<]*)</sub>', '\1', 'g') as p
from sec_para_table_filter;

create view sec_para_sup_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<sup>([^<]*)</sup>', '\1', 'g') as p
from sec_para_sub_filter;

create view sec_para_link_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '\(<ext-link ([^<]*)</ext-link>\)', '', 'g') as p
from sec_para_sup_filter;

create view sec_para_link2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<ext-link[^>]*>([^<]*)</ext-link>', '\1', 'g') as p
from sec_para_link_filter;

create view sec_para_math_space_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mspace[^>]*/>', ' ', 'g') as p
from sec_para_link2_filter;

create view sec_para_math_text_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtext[^>]*>([^<]*)</mml:mtext>', '\1', 'g') as p
from sec_para_math_space_filter;

create view sec_para_math_element_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:m[ion][^>]*>([^<]*)</mml:m[ion]>', '\1', 'g') as p
from sec_para_math_text_filter;

create view sec_para_math_sup_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup[^>]*>([^<]*)</mml:msup>', '\1', 'g') as p
from sec_para_math_element_filter;

create view sec_para_math_sub_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub[^>]*>([^<]*)</mml:msub>', '\1', 'g') as p
from sec_para_math_sup_filter;

create view sec_para_math_subsup_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup[^>]*>([^<]*)</mml:msubsup>', '\1', 'g') as p
from sec_para_math_sub_filter;

create view sec_para_math_fenced_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced[^>]*>([^<]*)</mml:mfenced>', '\1', 'g') as p
from sec_para_math_subsup_filter;

create view sec_para_math_frac_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac[^>]*>([^<]*)</mml:mfrac>', '\1', 'g') as p
from sec_para_math_fenced_filter;

create view sec_para_math_fenced2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced[^>]*>([^<]*)</mml:mfenced>', '\1', 'g') as p
from sec_para_math_frac_filter;

create view sec_para_math_sqrt_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt[^>]*>([^<]*)</mml:msqrt>', '\1', 'g') as p
from sec_para_math_fenced2_filter;

create view sec_para_math_under_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder[^>]*>([^<]*)</mml:munder>', '\1', 'g') as p
from sec_para_math_sqrt_filter;

create view sec_para_math_over_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mover[^>]*>([^<]*)</mml:mover>', '\1', 'g') as p
from sec_para_math_under_filter;

create view sec_para_math_row_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow[^>]*>([^<]*)</mml:mrow>', '\1', 'g') as p
from sec_para_math_over_filter;

create view sec_para_row_sup_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup[^>]*>([^<]*)</mml:msup>', '\1', 'g') as p
from sec_para_math_row_filter;

create view sec_para_row_sub_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub[^>]*>([^<]*)</mml:msub>', '\1', 'g') as p
from sec_para_row_sup_filter;

create view sec_para_row_subsup_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup[^>]*>([^<]*)</mml:msubsup>', '\1', 'g') as p
from sec_para_row_sub_filter;

create view sec_para_row_fenced_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced[^>]*>([^<]*)</mml:mfenced>', '\1', 'g') as p
from sec_para_row_subsup_filter;

create view sec_para_row_frac_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac[^>]*>([^<]*)</mml:mfrac>', '\1', 'g') as p
from sec_para_row_fenced_filter;

create view sec_para_row_fenced2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced[^>]*>([^<]*)</mml:mfenced>', '\1', 'g') as p
from sec_para_row_frac_filter;

create view sec_para_row_sqrt_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt[^>]*>([^<]*)</mml:msqrt>', '\1', 'g') as p
from sec_para_row_fenced2_filter;

create view sec_para_row_under_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder[^>]*>([^<]*)</mml:munder>', '\1', 'g') as p
from sec_para_row_sqrt_filter;

create view sec_para_row_row_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow[^>]*>([^<]*)</mml:mrow>', '\1', 'g') as p
from sec_para_row_under_filter;

create view sec_para_row_row2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow[^>]*>([^<]*)</mml:mrow>', '\1', 'g') as p
from sec_para_row_row_filter;

create view sec_para_row_mtd_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd[^>]*>([^<]*)</mml:mtd>', '\1', 'g') as p
from sec_para_row_row2_filter;

create view sec_para_row_mtr_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr[^>]*>([^<]*)</mml:mtr>', '\1', 'g') as p
from sec_para_row_row2_filter;

create view sec_para_row_mtable_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable[^>]*>([^<]*)</mml:mtable>', '\1', 'g') as p
from sec_para_row_mtr_filter;

create view sec_para_math_math_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math[^>]*>([^<]*)</mml:math>', '\1', 'g') as p
from sec_para_row_mtable_filter;

create view sec_para_tex_math_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<tex-math[^>]*>([^<]*)</tex-math>', '', 'g') as p
from sec_para_math_math_filter;

create view sec_para_inline_graphic_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<(inline-)?graphic [^/]*/>', '', 'g') as p
from sec_para_tex_math_filter;

create view sec_para_alternatives_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives>([^<]*)</alternatives>', '', 'g') as p
from sec_para_inline_graphic_filter;

create view sec_para_math_formula_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula[^>]*>([^<]*)</inline-formula>', '\1', 'g') as p
from sec_para_alternatives_filter;

create view sec_para_label_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<label>([^<]*)</label>', ' \1 ', 'g') as p
from sec_para_math_formula_filter;

create view sec_para_dformula_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula[^>]*>([^<]*)</disp-formula>', '\1', 'g') as p
from sec_para_label_filter;

create view sec_para_dgroup_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula-group[^>]*>([^<]*)</disp-formula-group>', '', 'g') as p
from sec_para_dformula_filter;

create view sec_para_sub2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<sub>([^<]*)</sub>', '\1', 'g') as p
from sec_para_dgroup_filter;

create view sec_para_sup2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<sup>([^<]*)</sup>', '\1', 'g') as p
from sec_para_sub2_filter;

create view sec_para_italic_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<italic>([^<]*)</italic>', '\1', 'g') as p
from sec_para_sup2_filter;

create view sec_para_bold_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<bold>([^<]*)</bold>', '\1', 'g') as p
from sec_para_italic_filter;

create view sec_para_sc_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<sc>([^<]*)</sc>', '\1', 'g') as p
from sec_para_bold_filter;

create view sec_para_underline_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<underline>([^<]*)</underline>', '\1', 'g') as p
from sec_para_sc_filter;

create view sec_para_email_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<email>([^<]*)</email>', '\1', 'g') as p
from sec_para_underline_filter;

create view sec_para_uri_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<uri[^>]*>([^<]*)</uri>', '\1', 'g') as p
from sec_para_email_filter;

create view sec_para_statement_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<statement[^>]*>([^<]*)</statement>', '\1', 'g') as p
from sec_para_uri_filter;

create view sec_para_institution_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<institution[^>]*>([^<]*)</institution>', '\1', 'g') as p
from sec_para_statement_filter;

create view sec_para_institution_id_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<institution-id[^>]*>([^<]*)</institution-id>', '\1', 'g') as p
from sec_para_institution_filter;

create view sec_para_institution_wrap_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<institution-wrap[^>]*>([^<]*)</institution-wrap>', '\1', 'g') as p
from sec_para_institution_id_filter;

create view sec_para_funding_source_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<funding-source[^>]*>([^<]*)</funding-source>', '\1', 'g') as p
from sec_para_institution_wrap_filter;

create view sec_para_ext_link_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '\((<underline>)?[^<]*<ext-link[^>]*>([^<]*)</ext-link>[^)]*(</underline>)?\)', '', 'g') as p
from sec_para_funding_source_filter;

create view sec_para_list_p_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<p[^>]*>([^<]*)</p>', '\1', 'g') as p
from sec_para_ext_link_filter;

create view sec_para_list_item_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item[^>]*>([^<]*)</list-item>', '\1', 'g') as p
from sec_para_list_p_filter;

create view sec_para_list_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<list[^>]*>([^<]*)</list>', '\1', 'g') as p
from sec_para_list_item_filter;

create view sec_para_title_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<title[^>]*>([^<]*)</title>', '\1', 'g') as p
from sec_para_list_filter;

create view sec_para_caption_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption[^>]*>([^<]*)</caption>', '\1', 'g') as p
from sec_para_title_filter;

create view sec_para_alt_text_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<alt-text[^>]*>([^<]*)</alt-text>', '\1', 'g') as p
from sec_para_caption_filter;

create view sec_para_media_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<media[^>]*>([^<]*)</media>', '\1', 'g') as p
from sec_para_alt_text_filter;

create view sec_para_suppl_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<supplementary-material[^>]*>([^<]*)</supplementary-material>', '', 'g') as p
from sec_para_media_filter;

create view sec_para_disp_quote_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-quote[^>]*>([^<]*)</disp-quote>', '\1', 'g') as p
from sec_para_suppl_filter;

-- these next "finals" scrub what we haven't handled

create view sec_para_final_inline_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula[^>]*>.*?</inline-formula>', '', 'g') as p
from sec_para_disp_quote_filter;

create view sec_para_final_disp_group_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula-group[^>]*>.*?</disp-formula-group>', '', 'g') as p
from sec_para_final_inline_filter;

create view sec_para_final_disp_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula[^>]*>.*?</disp-formula>', '', 'g') as p
from sec_para_final_disp_group_filter;

create view sec_para_final_math_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math[^>]*>.*?</mml:math>', '', 'g') as p
from sec_para_final_disp_filter;

create view sec_para_stat_para_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<p [^>]*>([^<]*)</p>', '\1', 'g') as p
from sec_para_final_math_filter;

create view sec_para_statement2_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<statement[^>]*>([^<]*)</statement>', '\1', 'g') as p
from sec_para_stat_para_filter;

create view sec_para_final as
select
	pmcid,
	orig,
	p
from sec_para_statement2_filter;

select * from sec_para_final where p ~ '<' limit 10;

