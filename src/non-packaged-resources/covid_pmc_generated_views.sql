drop view covid_pmc.paragraph_staging_filter cascade;

create view covid_pmc.paragraph_staging_filter as
select
	pmcid,
	p as orig,
	p::text
from covid_pmc.paragraph_staging;


create view covid_pmc.paragraph_staging_comment_filter as
select
	pmcid,
	orig,
	regexp_replace(p, '<!--[^>]*>', '', 'g') as p
from covid_pmc.paragraph_staging_filter;


create view covid_pmc.paragraph_staging_filter_01_xref as
select
	pmcid,
	orig,
	regexp_replace(p, '<xref(?: [^>]*)?>([^<]*)</xref>', '\1', 'g') as p
from covid_pmc.paragraph_staging_comment_filter;

create view covid_pmc.paragraph_staging_filter_01a_xref as
select
	pmcid,
	orig,
	regexp_replace(p, '<xref(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_xref;

create view covid_pmc.paragraph_staging_filter_01_th as
select
	pmcid,
	orig,
	regexp_replace(p, '<th(?: [^>]*)?>([^<]*)</th>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_xref;

create view covid_pmc.paragraph_staging_filter_01_bold as
select
	pmcid,
	orig,
	regexp_replace(p, '<bold(?: [^>]*)?>([^<]*)</bold>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_th;

create view covid_pmc.paragraph_staging_filter_01_uri as
select
	pmcid,
	orig,
	regexp_replace(p, '<uri(?: [^>]*)?>([^<]*)</uri>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_bold;

create view covid_pmc.paragraph_staging_filter_01_license_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<license-p(?: [^>]*)?>([^<]*)</license-p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_uri;

create view covid_pmc.paragraph_staging_filter_01_col as
select
	pmcid,
	orig,
	regexp_replace(p, '<col(?: [^>]*)?>([^<]*)</col>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_license_p;

create view covid_pmc.paragraph_staging_filter_01a_col as
select
	pmcid,
	orig,
	regexp_replace(p, '<col(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_col;

create view covid_pmc.paragraph_staging_filter_01_graphic as
select
	pmcid,
	orig,
	regexp_replace(p, '<graphic(?: [^>]*)?>([^<]*)</graphic>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_col;

create view covid_pmc.paragraph_staging_filter_01a_graphic as
select
	pmcid,
	orig,
	regexp_replace(p, '<graphic(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_graphic;

create view covid_pmc.paragraph_staging_filter_01_glyph_ref as
select
	pmcid,
	orig,
	regexp_replace(p, '<glyph-ref(?: [^>]*)?>([^<]*)</glyph-ref>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_graphic;

create view covid_pmc.paragraph_staging_filter_01a_glyph_ref as
select
	pmcid,
	orig,
	regexp_replace(p, '<glyph-ref(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_glyph_ref;

create view covid_pmc.paragraph_staging_filter_01_strike as
select
	pmcid,
	orig,
	regexp_replace(p, '<strike(?: [^>]*)?>([^<]*)</strike>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_glyph_ref;

create view covid_pmc.paragraph_staging_filter_01_inline_graphic as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-graphic(?: [^>]*)?>([^<]*)</inline-graphic>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_strike;

create view covid_pmc.paragraph_staging_filter_01a_inline_graphic as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-graphic(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_inline_graphic;

create view covid_pmc.paragraph_staging_filter_01_mml_mo as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mo(?: [^>]*)?>([^<]*)</mml:mo>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_inline_graphic;

create view covid_pmc.paragraph_staging_filter_01a_mml_mo as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mo(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_mo;

create view covid_pmc.paragraph_staging_filter_01_mml_mn as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mn(?: [^>]*)?>([^<]*)</mml:mn>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_mo;

create view covid_pmc.paragraph_staging_filter_01a_mml_mn as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mn(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_mn;

create view covid_pmc.paragraph_staging_filter_01_copyright_statement as
select
	pmcid,
	orig,
	regexp_replace(p, '<copyright-statement(?: [^>]*)?>([^<]*)</copyright-statement>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_mn;

create view covid_pmc.paragraph_staging_filter_01_sup as
select
	pmcid,
	orig,
	regexp_replace(p, '<sup(?: [^>]*)?>([^<]*)</sup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_copyright_statement;

create view covid_pmc.paragraph_staging_filter_01_inline_supplementary_material as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-supplementary-material(?: [^>]*)?>([^<]*)</inline-supplementary-material>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_sup;

create view covid_pmc.paragraph_staging_filter_01_object_id as
select
	pmcid,
	orig,
	regexp_replace(p, '<object-id(?: [^>]*)?>([^<]*)</object-id>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_inline_supplementary_material;

create view covid_pmc.paragraph_staging_filter_01_mml_mi as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mi(?: [^>]*)?>([^<]*)</mml:mi>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_object_id;

create view covid_pmc.paragraph_staging_filter_01a_mml_mi as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mi(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_mi;

create view covid_pmc.paragraph_staging_filter_01_institution_id as
select
	pmcid,
	orig,
	regexp_replace(p, '<institution-id(?: [^>]*)?>([^<]*)</institution-id>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_mi;

create view covid_pmc.paragraph_staging_filter_01_sc as
select
	pmcid,
	orig,
	regexp_replace(p, '<sc(?: [^>]*)?>([^<]*)</sc>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_institution_id;

create view covid_pmc.paragraph_staging_filter_01_glyph_data as
select
	pmcid,
	orig,
	regexp_replace(p, '<glyph-data(?: [^>]*)?>([^<]*)</glyph-data>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_sc;

create view covid_pmc.paragraph_staging_filter_01_italic as
select
	pmcid,
	orig,
	regexp_replace(p, '<italic(?: [^>]*)?>([^<]*)</italic>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_glyph_data;

create view covid_pmc.paragraph_staging_filter_01_preformat as
select
	pmcid,
	orig,
	regexp_replace(p, '<preformat(?: [^>]*)?>([^<]*)</preformat>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_italic;

create view covid_pmc.paragraph_staging_filter_01_email as
select
	pmcid,
	orig,
	regexp_replace(p, '<email(?: [^>]*)?>([^<]*)</email>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_preformat;

create view covid_pmc.paragraph_staging_filter_01_sub as
select
	pmcid,
	orig,
	regexp_replace(p, '<sub(?: [^>]*)?>([^<]*)</sub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_email;

create view covid_pmc.paragraph_staging_filter_01_break as
select
	pmcid,
	orig,
	regexp_replace(p, '<break(?: [^>]*)?>([^<]*)</break>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_sub;

create view covid_pmc.paragraph_staging_filter_01a_break as
select
	pmcid,
	orig,
	regexp_replace(p, '<break(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_break;

create view covid_pmc.paragraph_staging_filter_01_mml_maligngroup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:maligngroup(?: [^>]*)?>([^<]*)</mml:maligngroup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_break;

create view covid_pmc.paragraph_staging_filter_01a_mml_maligngroup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:maligngroup(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_maligngroup;

create view covid_pmc.paragraph_staging_filter_01_tex_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<tex-math(?: [^>]*)?>([^<]*)</tex-math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_maligngroup;

create view covid_pmc.paragraph_staging_filter_01_copyright_holder as
select
	pmcid,
	orig,
	regexp_replace(p, '<copyright-holder(?: [^>]*)?>([^<]*)</copyright-holder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_tex_math;

create view covid_pmc.paragraph_staging_filter_01_monospace as
select
	pmcid,
	orig,
	regexp_replace(p, '<monospace(?: [^>]*)?>([^<]*)</monospace>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_copyright_holder;

create view covid_pmc.paragraph_staging_filter_01_copyright_year as
select
	pmcid,
	orig,
	regexp_replace(p, '<copyright-year(?: [^>]*)?>([^<]*)</copyright-year>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_monospace;

create view covid_pmc.paragraph_staging_filter_01_funding_source as
select
	pmcid,
	orig,
	regexp_replace(p, '<funding-source(?: [^>]*)?>([^<]*)</funding-source>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_copyright_year;

create view covid_pmc.paragraph_staging_filter_01_title as
select
	pmcid,
	orig,
	regexp_replace(p, '<title(?: [^>]*)?>([^<]*)</title>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_funding_source;

create view covid_pmc.paragraph_staging_filter_01_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_title;

create view covid_pmc.paragraph_staging_filter_01a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_mrow;

create view covid_pmc.paragraph_staging_filter_01_mml_mprescripts as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mprescripts(?: [^>]*)?>([^<]*)</mml:mprescripts>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_01a_mml_mprescripts as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mprescripts(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_mprescripts;

create view covid_pmc.paragraph_staging_filter_01_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_mprescripts;

create view covid_pmc.paragraph_staging_filter_01a_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_01_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_01a_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_01_sans_serif as
select
	pmcid,
	orig,
	regexp_replace(p, '<sans-serif(?: [^>]*)?>([^<]*)</sans-serif>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_01_related_article as
select
	pmcid,
	orig,
	regexp_replace(p, '<related-article(?: [^>]*)?>([^<]*)</related-article>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_sans_serif;

create view covid_pmc.paragraph_staging_filter_01_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_related_article;

create view covid_pmc.paragraph_staging_filter_01_label as
select
	pmcid,
	orig,
	regexp_replace(p, '<label(?: [^>]*)?>([^<]*)</label>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_p;

create view covid_pmc.paragraph_staging_filter_01a_label as
select
	pmcid,
	orig,
	regexp_replace(p, '<label(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_label;

create view covid_pmc.paragraph_staging_filter_01_mml_mspace as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mspace(?: [^>]*)?>([^<]*)</mml:mspace>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_label;

create view covid_pmc.paragraph_staging_filter_01a_mml_mspace as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mspace(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_mspace;

create view covid_pmc.paragraph_staging_filter_01_underline as
select
	pmcid,
	orig,
	regexp_replace(p, '<underline(?: [^>]*)?>([^<]*)</underline>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_mspace;

create view covid_pmc.paragraph_staging_filter_01_hr as
select
	pmcid,
	orig,
	regexp_replace(p, '<hr(?: [^>]*)?>([^<]*)</hr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_underline;

create view covid_pmc.paragraph_staging_filter_01a_hr as
select
	pmcid,
	orig,
	regexp_replace(p, '<hr(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_hr;

create view covid_pmc.paragraph_staging_filter_01_styled_content as
select
	pmcid,
	orig,
	regexp_replace(p, '<styled-content(?: [^>]*)?>([^<]*)</styled-content>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_hr;

create view covid_pmc.paragraph_staging_filter_01_mml_mtext as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtext(?: [^>]*)?>([^<]*)</mml:mtext>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_styled_content;

create view covid_pmc.paragraph_staging_filter_01a_mml_mtext as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtext(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_mtext;

create view covid_pmc.paragraph_staging_filter_01_ext_link as
select
	pmcid,
	orig,
	regexp_replace(p, '<ext-link(?: [^>]*)?>([^<]*)</ext-link>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_mtext;

create view covid_pmc.paragraph_staging_filter_01_alt_text as
select
	pmcid,
	orig,
	regexp_replace(p, '<alt-text(?: [^>]*)?>([^<]*)</alt-text>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_ext_link;

create view covid_pmc.paragraph_staging_filter_01_institution as
select
	pmcid,
	orig,
	regexp_replace(p, '<institution(?: [^>]*)?>([^<]*)</institution>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_alt_text;

create view covid_pmc.paragraph_staging_filter_01_mml_none as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:none(?: [^>]*)?>([^<]*)</mml:none>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_institution;

create view covid_pmc.paragraph_staging_filter_01a_mml_none as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:none(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01_mml_none;

create view covid_pmc.paragraph_staging_filter_02_xref as
select
	pmcid,
	orig,
	regexp_replace(p, '<xref(?: [^>]*)?>([^<]*)</xref>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_01a_mml_none;

create view covid_pmc.paragraph_staging_filter_02a_xref as
select
	pmcid,
	orig,
	regexp_replace(p, '<xref(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_xref;

create view covid_pmc.paragraph_staging_filter_02_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02a_xref;

create view covid_pmc.paragraph_staging_filter_02_th as
select
	pmcid,
	orig,
	regexp_replace(p, '<th(?: [^>]*)?>([^<]*)</th>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_02a_th as
select
	pmcid,
	orig,
	regexp_replace(p, '<th(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_th;

create view covid_pmc.paragraph_staging_filter_02_institution_wrap as
select
	pmcid,
	orig,
	regexp_replace(p, '<institution-wrap(?: [^>]*)?>([^<]*)</institution-wrap>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02a_th;

create view covid_pmc.paragraph_staging_filter_02_bold as
select
	pmcid,
	orig,
	regexp_replace(p, '<bold(?: [^>]*)?>([^<]*)</bold>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_institution_wrap;

create view covid_pmc.paragraph_staging_filter_02_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?>([^<]*)</td>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_bold;

create view covid_pmc.paragraph_staging_filter_02a_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_td;

create view covid_pmc.paragraph_staging_filter_02_mml_mover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mover(?: [^>]*)?>([^<]*)</mml:mover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02a_td;

create view covid_pmc.paragraph_staging_filter_02_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_mover;

create view covid_pmc.paragraph_staging_filter_02_colgroup as
select
	pmcid,
	orig,
	regexp_replace(p, '<colgroup(?: [^>]*)?>([^<]*)</colgroup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_disp_formula;

create view covid_pmc.paragraph_staging_filter_02_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_colgroup;

create view covid_pmc.paragraph_staging_filter_02_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_math;

create view covid_pmc.paragraph_staging_filter_02a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_mtd;

create view covid_pmc.paragraph_staging_filter_02_inline_graphic as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-graphic(?: [^>]*)?>([^<]*)</inline-graphic>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_02a_inline_graphic as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-graphic(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_inline_graphic;

create view covid_pmc.paragraph_staging_filter_02_named_content as
select
	pmcid,
	orig,
	regexp_replace(p, '<named-content(?: [^>]*)?>([^<]*)</named-content>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02a_inline_graphic;

create view covid_pmc.paragraph_staging_filter_02_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_named_content;

create view covid_pmc.paragraph_staging_filter_02_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_munder;

create view covid_pmc.paragraph_staging_filter_02_sup as
select
	pmcid,
	orig,
	regexp_replace(p, '<sup(?: [^>]*)?>([^<]*)</sup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_02_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_sup;

create view covid_pmc.paragraph_staging_filter_02_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_alternatives;

create view covid_pmc.paragraph_staging_filter_02_mml_munderover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munderover(?: [^>]*)?>([^<]*)</mml:munderover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_inline_formula;

create view covid_pmc.paragraph_staging_filter_02_sc as
select
	pmcid,
	orig,
	regexp_replace(p, '<sc(?: [^>]*)?>([^<]*)</sc>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_munderover;

create view covid_pmc.paragraph_staging_filter_02_attrib as
select
	pmcid,
	orig,
	regexp_replace(p, '<attrib(?: [^>]*)?>([^<]*)</attrib>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_sc;

create view covid_pmc.paragraph_staging_filter_02_italic as
select
	pmcid,
	orig,
	regexp_replace(p, '<italic(?: [^>]*)?>([^<]*)</italic>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_attrib;

create view covid_pmc.paragraph_staging_filter_02a_italic as
select
	pmcid,
	orig,
	regexp_replace(p, '<italic(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_italic;

create view covid_pmc.paragraph_staging_filter_02_sub as
select
	pmcid,
	orig,
	regexp_replace(p, '<sub(?: [^>]*)?>([^<]*)</sub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02a_italic;

create view covid_pmc.paragraph_staging_filter_02_statement as
select
	pmcid,
	orig,
	regexp_replace(p, '<statement(?: [^>]*)?>([^<]*)</statement>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_sub;

create view covid_pmc.paragraph_staging_filter_02_license as
select
	pmcid,
	orig,
	regexp_replace(p, '<license(?: [^>]*)?>([^<]*)</license>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_statement;

create view covid_pmc.paragraph_staging_filter_02_fn as
select
	pmcid,
	orig,
	regexp_replace(p, '<fn(?: [^>]*)?>([^<]*)</fn>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_license;

create view covid_pmc.paragraph_staging_filter_02_term as
select
	pmcid,
	orig,
	regexp_replace(p, '<term(?: [^>]*)?>([^<]*)</term>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_fn;

create view covid_pmc.paragraph_staging_filter_02_mml_mmultiscripts as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mmultiscripts(?: [^>]*)?>([^<]*)</mml:mmultiscripts>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_term;

create view covid_pmc.paragraph_staging_filter_02_monospace as
select
	pmcid,
	orig,
	regexp_replace(p, '<monospace(?: [^>]*)?>([^<]*)</monospace>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_mmultiscripts;

create view covid_pmc.paragraph_staging_filter_02_title as
select
	pmcid,
	orig,
	regexp_replace(p, '<title(?: [^>]*)?>([^<]*)</title>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_monospace;

create view covid_pmc.paragraph_staging_filter_02_private_char as
select
	pmcid,
	orig,
	regexp_replace(p, '<private-char(?: [^>]*)?>([^<]*)</private-char>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_title;

create view covid_pmc.paragraph_staging_filter_02_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_private_char;

create view covid_pmc.paragraph_staging_filter_02_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_list_item;

create view covid_pmc.paragraph_staging_filter_02a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_mrow;

create view covid_pmc.paragraph_staging_filter_02_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_02_speaker as
select
	pmcid,
	orig,
	regexp_replace(p, '<speaker(?: [^>]*)?>([^<]*)</speaker>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_msup;

create view covid_pmc.paragraph_staging_filter_02_caption as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption(?: [^>]*)?>([^<]*)</caption>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_speaker;

create view covid_pmc.paragraph_staging_filter_02_mml_mphantom as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mphantom(?: [^>]*)?>([^<]*)</mml:mphantom>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_caption;

create view covid_pmc.paragraph_staging_filter_02_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_mphantom;

create view covid_pmc.paragraph_staging_filter_02_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_02_verse_line as
select
	pmcid,
	orig,
	regexp_replace(p, '<verse-line(?: [^>]*)?>([^<]*)</verse-line>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_02_media as
select
	pmcid,
	orig,
	regexp_replace(p, '<media(?: [^>]*)?>([^<]*)</media>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_verse_line;

create view covid_pmc.paragraph_staging_filter_02_mml_mpadded as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mpadded(?: [^>]*)?>([^<]*)</mml:mpadded>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_media;

create view covid_pmc.paragraph_staging_filter_02_mml_menclose as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:menclose(?: [^>]*)?>([^<]*)</mml:menclose>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_mpadded;

create view covid_pmc.paragraph_staging_filter_02_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_menclose;

create view covid_pmc.paragraph_staging_filter_02_label as
select
	pmcid,
	orig,
	regexp_replace(p, '<label(?: [^>]*)?>([^<]*)</label>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_p;

create view covid_pmc.paragraph_staging_filter_02_underline as
select
	pmcid,
	orig,
	regexp_replace(p, '<underline(?: [^>]*)?>([^<]*)</underline>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_label;

create view covid_pmc.paragraph_staging_filter_02_notes as
select
	pmcid,
	orig,
	regexp_replace(p, '<notes(?: [^>]*)?>([^<]*)</notes>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_underline;

create view covid_pmc.paragraph_staging_filter_02_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_notes;

create view covid_pmc.paragraph_staging_filter_02_styled_content as
select
	pmcid,
	orig,
	regexp_replace(p, '<styled-content(?: [^>]*)?>([^<]*)</styled-content>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_msub;

create view covid_pmc.paragraph_staging_filter_02_ext_link as
select
	pmcid,
	orig,
	regexp_replace(p, '<ext-link(?: [^>]*)?>([^<]*)</ext-link>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_styled_content;

create view covid_pmc.paragraph_staging_filter_02a_ext_link as
select
	pmcid,
	orig,
	regexp_replace(p, '<ext-link(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_ext_link;

create view covid_pmc.paragraph_staging_filter_02_tr as
select
	pmcid,
	orig,
	regexp_replace(p, '<tr(?: [^>]*)?>([^<]*)</tr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02a_ext_link;

create view covid_pmc.paragraph_staging_filter_02_mml_msubsup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup(?: [^>]*)?>([^<]*)</mml:msubsup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_tr;

create view covid_pmc.paragraph_staging_filter_03_xref as
select
	pmcid,
	orig,
	regexp_replace(p, '<xref(?: [^>]*)?>([^<]*)</xref>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_02_mml_msubsup;

create view covid_pmc.paragraph_staging_filter_03a_xref as
select
	pmcid,
	orig,
	regexp_replace(p, '<xref(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_xref;

create view covid_pmc.paragraph_staging_filter_03_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03a_xref;

create view covid_pmc.paragraph_staging_filter_03_th as
select
	pmcid,
	orig,
	regexp_replace(p, '<th(?: [^>]*)?>([^<]*)</th>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_03a_th as
select
	pmcid,
	orig,
	regexp_replace(p, '<th(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_th;

create view covid_pmc.paragraph_staging_filter_03_bold as
select
	pmcid,
	orig,
	regexp_replace(p, '<bold(?: [^>]*)?>([^<]*)</bold>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03a_th;

create view covid_pmc.paragraph_staging_filter_03a_bold as
select
	pmcid,
	orig,
	regexp_replace(p, '<bold(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_bold;

create view covid_pmc.paragraph_staging_filter_03_mml_mover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mover(?: [^>]*)?>([^<]*)</mml:mover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03a_bold;

create view covid_pmc.paragraph_staging_filter_03_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_mover;

create view covid_pmc.paragraph_staging_filter_03_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_mtr;

create view covid_pmc.paragraph_staging_filter_03_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_math;

create view covid_pmc.paragraph_staging_filter_03_permissions as
select
	pmcid,
	orig,
	regexp_replace(p, '<permissions(?: [^>]*)?>([^<]*)</permissions>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_mtd;

create view covid_pmc.paragraph_staging_filter_03_named_content as
select
	pmcid,
	orig,
	regexp_replace(p, '<named-content(?: [^>]*)?>([^<]*)</named-content>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_permissions;

create view covid_pmc.paragraph_staging_filter_03_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_named_content;

create view covid_pmc.paragraph_staging_filter_03_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_munder;

create view covid_pmc.paragraph_staging_filter_03_disp_quote as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-quote(?: [^>]*)?>([^<]*)</disp-quote>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_03_sup as
select
	pmcid,
	orig,
	regexp_replace(p, '<sup(?: [^>]*)?>([^<]*)</sup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_disp_quote;

create view covid_pmc.paragraph_staging_filter_03_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_sup;

create view covid_pmc.paragraph_staging_filter_03_mml_munderover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munderover(?: [^>]*)?>([^<]*)</mml:munderover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_inline_formula;

create view covid_pmc.paragraph_staging_filter_03_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_munderover;

create view covid_pmc.paragraph_staging_filter_03_attrib as
select
	pmcid,
	orig,
	regexp_replace(p, '<attrib(?: [^>]*)?>([^<]*)</attrib>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_list;

create view covid_pmc.paragraph_staging_filter_03_sub as
select
	pmcid,
	orig,
	regexp_replace(p, '<sub(?: [^>]*)?>([^<]*)</sub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_attrib;

create view covid_pmc.paragraph_staging_filter_03_thead as
select
	pmcid,
	orig,
	regexp_replace(p, '<thead(?: [^>]*)?>([^<]*)</thead>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_sub;

create view covid_pmc.paragraph_staging_filter_03_fn as
select
	pmcid,
	orig,
	regexp_replace(p, '<fn(?: [^>]*)?>([^<]*)</fn>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_thead;

create view covid_pmc.paragraph_staging_filter_03_mml_mmultiscripts as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mmultiscripts(?: [^>]*)?>([^<]*)</mml:mmultiscripts>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_fn;

create view covid_pmc.paragraph_staging_filter_03_title as
select
	pmcid,
	orig,
	regexp_replace(p, '<title(?: [^>]*)?>([^<]*)</title>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_mmultiscripts;

create view covid_pmc.paragraph_staging_filter_03_funding_source as
select
	pmcid,
	orig,
	regexp_replace(p, '<funding-source(?: [^>]*)?>([^<]*)</funding-source>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_title;

create view covid_pmc.paragraph_staging_filter_03_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_funding_source;

create view covid_pmc.paragraph_staging_filter_03_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_list_item;

create view covid_pmc.paragraph_staging_filter_03a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_mrow;

create view covid_pmc.paragraph_staging_filter_03_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_03_caption as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption(?: [^>]*)?>([^<]*)</caption>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_msup;

create view covid_pmc.paragraph_staging_filter_03_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_caption;

create view covid_pmc.paragraph_staging_filter_03_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_03_media as
select
	pmcid,
	orig,
	regexp_replace(p, '<media(?: [^>]*)?>([^<]*)</media>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_03a_media as
select
	pmcid,
	orig,
	regexp_replace(p, '<media(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_media;

create view covid_pmc.paragraph_staging_filter_03_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03a_media;

create view covid_pmc.paragraph_staging_filter_03_fig as
select
	pmcid,
	orig,
	regexp_replace(p, '<fig(?: [^>]*)?>([^<]*)</fig>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_p;

create view covid_pmc.paragraph_staging_filter_03_underline as
select
	pmcid,
	orig,
	regexp_replace(p, '<underline(?: [^>]*)?>([^<]*)</underline>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_fig;

create view covid_pmc.paragraph_staging_filter_03_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_underline;

create view covid_pmc.paragraph_staging_filter_03_verse_group as
select
	pmcid,
	orig,
	regexp_replace(p, '<verse-group(?: [^>]*)?>([^<]*)</verse-group>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_msub;

create view covid_pmc.paragraph_staging_filter_03_roman as
select
	pmcid,
	orig,
	regexp_replace(p, '<roman(?: [^>]*)?>([^<]*)</roman>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_verse_group;

create view covid_pmc.paragraph_staging_filter_03_mml_msubsup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup(?: [^>]*)?>([^<]*)</mml:msubsup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_roman;

create view covid_pmc.paragraph_staging_filter_03_tr as
select
	pmcid,
	orig,
	regexp_replace(p, '<tr(?: [^>]*)?>([^<]*)</tr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_mml_msubsup;

create view covid_pmc.paragraph_staging_filter_03_table_wrap_foot as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap-foot(?: [^>]*)?>([^<]*)</table-wrap-foot>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_tr;

create view covid_pmc.paragraph_staging_filter_04_tbody as
select
	pmcid,
	orig,
	regexp_replace(p, '<tbody(?: [^>]*)?>([^<]*)</tbody>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_03_table_wrap_foot;

create view covid_pmc.paragraph_staging_filter_04_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_tbody;

create view covid_pmc.paragraph_staging_filter_04_tr as
select
	pmcid,
	orig,
	regexp_replace(p, '<tr(?: [^>]*)?>([^<]*)</tr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_msub;

create view covid_pmc.paragraph_staging_filter_04_table_wrap_foot as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap-foot(?: [^>]*)?>([^<]*)</table-wrap-foot>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_tr;

create view covid_pmc.paragraph_staging_filter_04_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_table_wrap_foot;

create view covid_pmc.paragraph_staging_filter_04_mml_mroot as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mroot(?: [^>]*)?>([^<]*)</mml:mroot>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_04_monospace as
select
	pmcid,
	orig,
	regexp_replace(p, '<monospace(?: [^>]*)?>([^<]*)</monospace>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mroot;

create view covid_pmc.paragraph_staging_filter_04_disp_quote as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-quote(?: [^>]*)?>([^<]*)</disp-quote>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_monospace;

create view covid_pmc.paragraph_staging_filter_04_mml_mover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mover(?: [^>]*)?>([^<]*)</mml:mover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_disp_quote;

create view covid_pmc.paragraph_staging_filter_04_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mover;

create view covid_pmc.paragraph_staging_filter_04_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?>([^<]*)</td>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_list_item;

create view covid_pmc.paragraph_staging_filter_04a_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_td;

create view covid_pmc.paragraph_staging_filter_04_sub as
select
	pmcid,
	orig,
	regexp_replace(p, '<sub(?: [^>]*)?>([^<]*)</sub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04a_td;

create view covid_pmc.paragraph_staging_filter_04_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_sub;

create view covid_pmc.paragraph_staging_filter_04_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mtr;

create view covid_pmc.paragraph_staging_filter_04_mml_mmultiscripts as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mmultiscripts(?: [^>]*)?>([^<]*)</mml:mmultiscripts>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_04_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mmultiscripts;

create view covid_pmc.paragraph_staging_filter_04_mml_munderover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munderover(?: [^>]*)?>([^<]*)</mml:munderover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_inline_formula;

create view covid_pmc.paragraph_staging_filter_04_mml_mphantom as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mphantom(?: [^>]*)?>([^<]*)</mml:mphantom>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_munderover;

create view covid_pmc.paragraph_staging_filter_04_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mphantom;

create view covid_pmc.paragraph_staging_filter_04_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_math;

create view covid_pmc.paragraph_staging_filter_04_speech as
select
	pmcid,
	orig,
	regexp_replace(p, '<speech(?: [^>]*)?>([^<]*)</speech>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_p;

create view covid_pmc.paragraph_staging_filter_04_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_speech;

create view covid_pmc.paragraph_staging_filter_04_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_04_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_msup;

create view covid_pmc.paragraph_staging_filter_04a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mtd;

create view covid_pmc.paragraph_staging_filter_04_thead as
select
	pmcid,
	orig,
	regexp_replace(p, '<thead(?: [^>]*)?>([^<]*)</thead>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_04_caption as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption(?: [^>]*)?>([^<]*)</caption>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_thead;

create view covid_pmc.paragraph_staging_filter_04_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_caption;

create view covid_pmc.paragraph_staging_filter_04_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_04_mml_msubsup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup(?: [^>]*)?>([^<]*)</mml:msubsup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_list;

create view covid_pmc.paragraph_staging_filter_04_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_msubsup;

create view covid_pmc.paragraph_staging_filter_04_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_munder;

create view covid_pmc.paragraph_staging_filter_04_mml_mpadded as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mpadded(?: [^>]*)?>([^<]*)</mml:mpadded>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mtable;

create view covid_pmc.paragraph_staging_filter_04_fig as
select
	pmcid,
	orig,
	regexp_replace(p, '<fig(?: [^>]*)?>([^<]*)</fig>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mpadded;

create view covid_pmc.paragraph_staging_filter_04_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_fig;

create view covid_pmc.paragraph_staging_filter_04a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04_mml_mrow;

create view covid_pmc.paragraph_staging_filter_05_code as
select
	pmcid,
	orig,
	regexp_replace(p, '<code(?: [^>]*)?>([^<]*)</code>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_04a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_05_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_code;

create view covid_pmc.paragraph_staging_filter_05_tr as
select
	pmcid,
	orig,
	regexp_replace(p, '<tr(?: [^>]*)?>([^<]*)</tr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_msub;

create view covid_pmc.paragraph_staging_filter_05_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_tr;

create view covid_pmc.paragraph_staging_filter_05_mml_mover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mover(?: [^>]*)?>([^<]*)</mml:mover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_05_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mover;

create view covid_pmc.paragraph_staging_filter_05_boxed_text as
select
	pmcid,
	orig,
	regexp_replace(p, '<boxed-text(?: [^>]*)?>([^<]*)</boxed-text>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_list_item;

create view covid_pmc.paragraph_staging_filter_05_fn as
select
	pmcid,
	orig,
	regexp_replace(p, '<fn(?: [^>]*)?>([^<]*)</fn>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_boxed_text;

create view covid_pmc.paragraph_staging_filter_05_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_fn;

create view covid_pmc.paragraph_staging_filter_05a_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mtr;

create view covid_pmc.paragraph_staging_filter_05_mml_mmultiscripts as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mmultiscripts(?: [^>]*)?>([^<]*)</mml:mmultiscripts>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05a_mml_mtr;

create view covid_pmc.paragraph_staging_filter_05_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mmultiscripts;

create view covid_pmc.paragraph_staging_filter_05_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_05_mml_munderover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munderover(?: [^>]*)?>([^<]*)</mml:munderover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_inline_formula;

create view covid_pmc.paragraph_staging_filter_05_mml_mphantom as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mphantom(?: [^>]*)?>([^<]*)</mml:mphantom>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_munderover;

create view covid_pmc.paragraph_staging_filter_05_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mphantom;

create view covid_pmc.paragraph_staging_filter_05_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_alternatives;

create view covid_pmc.paragraph_staging_filter_05_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_p;

create view covid_pmc.paragraph_staging_filter_05_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_math;

create view covid_pmc.paragraph_staging_filter_05_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_05_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_msup;

create view covid_pmc.paragraph_staging_filter_05a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mtd;

create view covid_pmc.paragraph_staging_filter_05_thead as
select
	pmcid,
	orig,
	regexp_replace(p, '<thead(?: [^>]*)?>([^<]*)</thead>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_05_table as
select
	pmcid,
	orig,
	regexp_replace(p, '<table(?: [^>]*)?>([^<]*)</table>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_thead;

create view covid_pmc.paragraph_staging_filter_05_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_table;

create view covid_pmc.paragraph_staging_filter_05_caption as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption(?: [^>]*)?>([^<]*)</caption>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_list;

create view covid_pmc.paragraph_staging_filter_05_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_caption;

create view covid_pmc.paragraph_staging_filter_05_mml_msubsup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup(?: [^>]*)?>([^<]*)</mml:msubsup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_05_sec as
select
	pmcid,
	orig,
	regexp_replace(p, '<sec(?: [^>]*)?>([^<]*)</sec>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_msubsup;

create view covid_pmc.paragraph_staging_filter_05_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_sec;

create view covid_pmc.paragraph_staging_filter_05_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mtable;

create view covid_pmc.paragraph_staging_filter_05_mml_mpadded as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mpadded(?: [^>]*)?>([^<]*)</mml:mpadded>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_munder;

create view covid_pmc.paragraph_staging_filter_05_fig as
select
	pmcid,
	orig,
	regexp_replace(p, '<fig(?: [^>]*)?>([^<]*)</fig>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mpadded;

create view covid_pmc.paragraph_staging_filter_05_title as
select
	pmcid,
	orig,
	regexp_replace(p, '<title(?: [^>]*)?>([^<]*)</title>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_fig;

create view covid_pmc.paragraph_staging_filter_05_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_title;

create view covid_pmc.paragraph_staging_filter_05a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05_mml_mrow;

create view covid_pmc.paragraph_staging_filter_06_tbody as
select
	pmcid,
	orig,
	regexp_replace(p, '<tbody(?: [^>]*)?>([^<]*)</tbody>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_05a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_06_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_tbody;

create view covid_pmc.paragraph_staging_filter_06_table_wrap_foot as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap-foot(?: [^>]*)?>([^<]*)</table-wrap-foot>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_msub;

create view covid_pmc.paragraph_staging_filter_06_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_table_wrap_foot;

create view covid_pmc.paragraph_staging_filter_06_table_wrap as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap(?: [^>]*)?>([^<]*)</table-wrap>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_06_mml_mroot as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mroot(?: [^>]*)?>([^<]*)</mml:mroot>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_table_wrap;

create view covid_pmc.paragraph_staging_filter_06_mml_mover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mover(?: [^>]*)?>([^<]*)</mml:mover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mroot;

create view covid_pmc.paragraph_staging_filter_06_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mover;

create view covid_pmc.paragraph_staging_filter_06_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_disp_formula;

create view covid_pmc.paragraph_staging_filter_06_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?>([^<]*)</td>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_list_item;

create view covid_pmc.paragraph_staging_filter_06a_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_td;

create view covid_pmc.paragraph_staging_filter_06_label as
select
	pmcid,
	orig,
	regexp_replace(p, '<label(?: [^>]*)?>([^<]*)</label>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06a_td;

create view covid_pmc.paragraph_staging_filter_06_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_label;

create view covid_pmc.paragraph_staging_filter_06_mml_mmultiscripts as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mmultiscripts(?: [^>]*)?>([^<]*)</mml:mmultiscripts>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mtr;

create view covid_pmc.paragraph_staging_filter_06_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mmultiscripts;

create view covid_pmc.paragraph_staging_filter_06_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_06_mml_munderover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munderover(?: [^>]*)?>([^<]*)</mml:munderover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_inline_formula;

create view covid_pmc.paragraph_staging_filter_06_mml_mphantom as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mphantom(?: [^>]*)?>([^<]*)</mml:mphantom>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_munderover;

create view covid_pmc.paragraph_staging_filter_06_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mphantom;

create view covid_pmc.paragraph_staging_filter_06_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_alternatives;

create view covid_pmc.paragraph_staging_filter_06_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_p;

create view covid_pmc.paragraph_staging_filter_06_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_math;

create view covid_pmc.paragraph_staging_filter_06_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_06_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_msup;

create view covid_pmc.paragraph_staging_filter_06a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mtd;

create view covid_pmc.paragraph_staging_filter_06_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_06_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_list;

create view covid_pmc.paragraph_staging_filter_06_mml_msubsup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup(?: [^>]*)?>([^<]*)</mml:msubsup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_06_sec as
select
	pmcid,
	orig,
	regexp_replace(p, '<sec(?: [^>]*)?>([^<]*)</sec>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_msubsup;

create view covid_pmc.paragraph_staging_filter_06_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_sec;

create view covid_pmc.paragraph_staging_filter_06_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mtable;

create view covid_pmc.paragraph_staging_filter_06_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_munder;

create view covid_pmc.paragraph_staging_filter_06a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06_mml_mrow;

create view covid_pmc.paragraph_staging_filter_07_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_06a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_07_tr as
select
	pmcid,
	orig,
	regexp_replace(p, '<tr(?: [^>]*)?>([^<]*)</tr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_msub;

create view covid_pmc.paragraph_staging_filter_07_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_tr;

create view covid_pmc.paragraph_staging_filter_07a_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_07_italic as
select
	pmcid,
	orig,
	regexp_replace(p, '<italic(?: [^>]*)?>([^<]*)</italic>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07a_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_07_th as
select
	pmcid,
	orig,
	regexp_replace(p, '<th(?: [^>]*)?>([^<]*)</th>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_italic;

create view covid_pmc.paragraph_staging_filter_07a_th as
select
	pmcid,
	orig,
	regexp_replace(p, '<th(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_th;

create view covid_pmc.paragraph_staging_filter_07_mml_mover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mover(?: [^>]*)?>([^<]*)</mml:mover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07a_th;

create view covid_pmc.paragraph_staging_filter_07_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_mover;

create view covid_pmc.paragraph_staging_filter_07_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?>([^<]*)</td>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_list_item;

create view covid_pmc.paragraph_staging_filter_07a_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_td;

create view covid_pmc.paragraph_staging_filter_07_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07a_td;

create view covid_pmc.paragraph_staging_filter_07_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_mtr;

create view covid_pmc.paragraph_staging_filter_07_mml_munderover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munderover(?: [^>]*)?>([^<]*)</mml:munderover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_07_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_munderover;

create view covid_pmc.paragraph_staging_filter_07_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_inline_formula;

create view covid_pmc.paragraph_staging_filter_07_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_alternatives;

create view covid_pmc.paragraph_staging_filter_07_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_p;

create view covid_pmc.paragraph_staging_filter_07_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_math;

create view covid_pmc.paragraph_staging_filter_07_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_07_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_msup;

create view covid_pmc.paragraph_staging_filter_07_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_mtd;

create view covid_pmc.paragraph_staging_filter_07_caption as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption(?: [^>]*)?>([^<]*)</caption>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_list;

create view covid_pmc.paragraph_staging_filter_07_table as
select
	pmcid,
	orig,
	regexp_replace(p, '<table(?: [^>]*)?>([^<]*)</table>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_caption;

create view covid_pmc.paragraph_staging_filter_07_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_table;

create view covid_pmc.paragraph_staging_filter_07_mml_msubsup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup(?: [^>]*)?>([^<]*)</mml:msubsup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_07_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_msubsup;

create view covid_pmc.paragraph_staging_filter_07_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_mtable;

create view covid_pmc.paragraph_staging_filter_07_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_munder;

create view covid_pmc.paragraph_staging_filter_07a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07_mml_mrow;

create view covid_pmc.paragraph_staging_filter_08_tbody as
select
	pmcid,
	orig,
	regexp_replace(p, '<tbody(?: [^>]*)?>([^<]*)</tbody>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_07a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_08_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_tbody;

create view covid_pmc.paragraph_staging_filter_08_tr as
select
	pmcid,
	orig,
	regexp_replace(p, '<tr(?: [^>]*)?>([^<]*)</tr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_msub;

create view covid_pmc.paragraph_staging_filter_08_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_tr;

create view covid_pmc.paragraph_staging_filter_08_table_wrap as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap(?: [^>]*)?>([^<]*)</table-wrap>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_08_mml_mover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mover(?: [^>]*)?>([^<]*)</mml:mover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_table_wrap;

create view covid_pmc.paragraph_staging_filter_08_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_mover;

create view covid_pmc.paragraph_staging_filter_08_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_disp_formula;

create view covid_pmc.paragraph_staging_filter_08_label as
select
	pmcid,
	orig,
	regexp_replace(p, '<label(?: [^>]*)?>([^<]*)</label>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_list_item;

create view covid_pmc.paragraph_staging_filter_08_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_label;

create view covid_pmc.paragraph_staging_filter_08_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_mtr;

create view covid_pmc.paragraph_staging_filter_08_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_08_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_inline_formula;

create view covid_pmc.paragraph_staging_filter_08_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_alternatives;

create view covid_pmc.paragraph_staging_filter_08_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_math;

create view covid_pmc.paragraph_staging_filter_08_term as
select
	pmcid,
	orig,
	regexp_replace(p, '<term(?: [^>]*)?>([^<]*)</term>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_p;

create view covid_pmc.paragraph_staging_filter_08_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_term;

create view covid_pmc.paragraph_staging_filter_08_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_08_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_msup;

create view covid_pmc.paragraph_staging_filter_08a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_mtd;

create view covid_pmc.paragraph_staging_filter_08_caption as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption(?: [^>]*)?>([^<]*)</caption>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_08_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_caption;

create view covid_pmc.paragraph_staging_filter_08_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_list;

create view covid_pmc.paragraph_staging_filter_08_mml_msubsup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup(?: [^>]*)?>([^<]*)</mml:msubsup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_08_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_msubsup;

create view covid_pmc.paragraph_staging_filter_08_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_mtable;

create view covid_pmc.paragraph_staging_filter_08_fig as
select
	pmcid,
	orig,
	regexp_replace(p, '<fig(?: [^>]*)?>([^<]*)</fig>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_munder;

create view covid_pmc.paragraph_staging_filter_08_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_fig;

create view covid_pmc.paragraph_staging_filter_08a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08_mml_mrow;

create view covid_pmc.paragraph_staging_filter_09_tbody as
select
	pmcid,
	orig,
	regexp_replace(p, '<tbody(?: [^>]*)?>([^<]*)</tbody>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_08a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_09_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_tbody;

create view covid_pmc.paragraph_staging_filter_09_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_msub;

create view covid_pmc.paragraph_staging_filter_09_italic as
select
	pmcid,
	orig,
	regexp_replace(p, '<italic(?: [^>]*)?>([^<]*)</italic>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_09a_italic as
select
	pmcid,
	orig,
	regexp_replace(p, '<italic(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_italic;

create view covid_pmc.paragraph_staging_filter_09_mml_mover as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mover(?: [^>]*)?>([^<]*)</mml:mover>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09a_italic;

create view covid_pmc.paragraph_staging_filter_09_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_mover;

create view covid_pmc.paragraph_staging_filter_09_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?>([^<]*)</td>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_disp_formula;

create view covid_pmc.paragraph_staging_filter_09a_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_td;

create view covid_pmc.paragraph_staging_filter_09_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09a_td;

create view covid_pmc.paragraph_staging_filter_09_disp_formula_group as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula-group(?: [^>]*)?>([^<]*)</disp-formula-group>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_mtr;

create view covid_pmc.paragraph_staging_filter_09_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_disp_formula_group;

create view covid_pmc.paragraph_staging_filter_09_mml_mmultiscripts as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mmultiscripts(?: [^>]*)?>([^<]*)</mml:mmultiscripts>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_09_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_mmultiscripts;

create view covid_pmc.paragraph_staging_filter_09_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_inline_formula;

create view covid_pmc.paragraph_staging_filter_09_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_alternatives;

create view covid_pmc.paragraph_staging_filter_09_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_math;

create view covid_pmc.paragraph_staging_filter_09_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_p;

create view covid_pmc.paragraph_staging_filter_09_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_09_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_msup;

create view covid_pmc.paragraph_staging_filter_09a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_mtd;

create view covid_pmc.paragraph_staging_filter_09_thead as
select
	pmcid,
	orig,
	regexp_replace(p, '<thead(?: [^>]*)?>([^<]*)</thead>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_09_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_thead;

create view covid_pmc.paragraph_staging_filter_09_table as
select
	pmcid,
	orig,
	regexp_replace(p, '<table(?: [^>]*)?>([^<]*)</table>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_list;

create view covid_pmc.paragraph_staging_filter_09_caption as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption(?: [^>]*)?>([^<]*)</caption>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_table;

create view covid_pmc.paragraph_staging_filter_09_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_caption;

create view covid_pmc.paragraph_staging_filter_09_mml_msubsup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup(?: [^>]*)?>([^<]*)</mml:msubsup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_09_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_msubsup;

create view covid_pmc.paragraph_staging_filter_09_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_mtable;

create view covid_pmc.paragraph_staging_filter_09_fig as
select
	pmcid,
	orig,
	regexp_replace(p, '<fig(?: [^>]*)?>([^<]*)</fig>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_munder;

create view covid_pmc.paragraph_staging_filter_09_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_fig;

create view covid_pmc.paragraph_staging_filter_09a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09_mml_mrow;

create view covid_pmc.paragraph_staging_filter_10_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_09a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_10_tr as
select
	pmcid,
	orig,
	regexp_replace(p, '<tr(?: [^>]*)?>([^<]*)</tr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_msub;

create view covid_pmc.paragraph_staging_filter_10_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_tr;

create view covid_pmc.paragraph_staging_filter_10_media as
select
	pmcid,
	orig,
	regexp_replace(p, '<media(?: [^>]*)?>([^<]*)</media>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_10a_media as
select
	pmcid,
	orig,
	regexp_replace(p, '<media(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_media;

create view covid_pmc.paragraph_staging_filter_10_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10a_media;

create view covid_pmc.paragraph_staging_filter_10_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_disp_formula;

create view covid_pmc.paragraph_staging_filter_10_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_list_item;

create view covid_pmc.paragraph_staging_filter_10a_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_mtr;

create view covid_pmc.paragraph_staging_filter_10_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10a_mml_mtr;

create view covid_pmc.paragraph_staging_filter_10_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_10_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_inline_formula;

create view covid_pmc.paragraph_staging_filter_10_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_alternatives;

create view covid_pmc.paragraph_staging_filter_10_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_p;

create view covid_pmc.paragraph_staging_filter_10_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_math;

create view covid_pmc.paragraph_staging_filter_10_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_10_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_msup;

create view covid_pmc.paragraph_staging_filter_10a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_mtd;

create view covid_pmc.paragraph_staging_filter_10_table as
select
	pmcid,
	orig,
	regexp_replace(p, '<table(?: [^>]*)?>([^<]*)</table>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_10_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_table;

create view covid_pmc.paragraph_staging_filter_10_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_10_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_mtable;

create view covid_pmc.paragraph_staging_filter_10_fig as
select
	pmcid,
	orig,
	regexp_replace(p, '<fig(?: [^>]*)?>([^<]*)</fig>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_munder;

create view covid_pmc.paragraph_staging_filter_10_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_fig;

create view covid_pmc.paragraph_staging_filter_10a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10_mml_mrow;

create view covid_pmc.paragraph_staging_filter_11_tbody as
select
	pmcid,
	orig,
	regexp_replace(p, '<tbody(?: [^>]*)?>([^<]*)</tbody>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_10a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_11_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_tbody;

create view covid_pmc.paragraph_staging_filter_11_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_msub;

create view covid_pmc.paragraph_staging_filter_11_table_wrap as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap(?: [^>]*)?>([^<]*)</table-wrap>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_11_supplementary_material as
select
	pmcid,
	orig,
	regexp_replace(p, '<supplementary-material(?: [^>]*)?>([^<]*)</supplementary-material>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_table_wrap;

create view covid_pmc.paragraph_staging_filter_11_th as
select
	pmcid,
	orig,
	regexp_replace(p, '<th(?: [^>]*)?>([^<]*)</th>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_supplementary_material;

create view covid_pmc.paragraph_staging_filter_11a_th as
select
	pmcid,
	orig,
	regexp_replace(p, '<th(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_th;

create view covid_pmc.paragraph_staging_filter_11_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11a_th;

create view covid_pmc.paragraph_staging_filter_11_fn as
select
	pmcid,
	orig,
	regexp_replace(p, '<fn(?: [^>]*)?>([^<]*)</fn>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_disp_formula;

create view covid_pmc.paragraph_staging_filter_11_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_fn;

create view covid_pmc.paragraph_staging_filter_11_disp_formula_group as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula-group(?: [^>]*)?>([^<]*)</disp-formula-group>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_mtr;

create view covid_pmc.paragraph_staging_filter_11_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_disp_formula_group;

create view covid_pmc.paragraph_staging_filter_11_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_11_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_inline_formula;

create view covid_pmc.paragraph_staging_filter_11_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_alternatives;

create view covid_pmc.paragraph_staging_filter_11_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_p;

create view covid_pmc.paragraph_staging_filter_11_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_math;

create view covid_pmc.paragraph_staging_filter_11_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_11_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_msup;

create view covid_pmc.paragraph_staging_filter_11a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_mtd;

create view covid_pmc.paragraph_staging_filter_11_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_11_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_list;

create view covid_pmc.paragraph_staging_filter_11_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_11_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_mtable;

create view covid_pmc.paragraph_staging_filter_11a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11_mml_mrow;

create view covid_pmc.paragraph_staging_filter_12_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_11a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_12_tr as
select
	pmcid,
	orig,
	regexp_replace(p, '<tr(?: [^>]*)?>([^<]*)</tr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_msub;

create view covid_pmc.paragraph_staging_filter_12_table_wrap_foot as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap-foot(?: [^>]*)?>([^<]*)</table-wrap-foot>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_tr;

create view covid_pmc.paragraph_staging_filter_12_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_table_wrap_foot;

create view covid_pmc.paragraph_staging_filter_12a_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_12_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12a_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_12_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_disp_formula;

create view covid_pmc.paragraph_staging_filter_12_array as
select
	pmcid,
	orig,
	regexp_replace(p, '<array(?: [^>]*)?>([^<]*)</array>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_list_item;

create view covid_pmc.paragraph_staging_filter_12_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_array;

create view covid_pmc.paragraph_staging_filter_12_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_mtr;

create view covid_pmc.paragraph_staging_filter_12_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_12_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_inline_formula;

create view covid_pmc.paragraph_staging_filter_12_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_math;

create view covid_pmc.paragraph_staging_filter_12_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_p;

create view covid_pmc.paragraph_staging_filter_12_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_12_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_msup;

create view covid_pmc.paragraph_staging_filter_12a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_mtd;

create view covid_pmc.paragraph_staging_filter_12_caption as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption(?: [^>]*)?>([^<]*)</caption>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_12_table as
select
	pmcid,
	orig,
	regexp_replace(p, '<table(?: [^>]*)?>([^<]*)</table>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_caption;

create view covid_pmc.paragraph_staging_filter_12_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_table;

create view covid_pmc.paragraph_staging_filter_12_mml_msubsup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msubsup(?: [^>]*)?>([^<]*)</mml:msubsup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_12_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_msubsup;

create view covid_pmc.paragraph_staging_filter_12_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_mtable;

create view covid_pmc.paragraph_staging_filter_12a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12_mml_mrow;

create view covid_pmc.paragraph_staging_filter_13_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_12a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_13_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_alternatives;

create view covid_pmc.paragraph_staging_filter_13a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_mtd;

create view covid_pmc.paragraph_staging_filter_13_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_13a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_mrow;

create view covid_pmc.paragraph_staging_filter_13_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_13_mml_munder as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:munder(?: [^>]*)?>([^<]*)</mml:munder>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_p;

create view covid_pmc.paragraph_staging_filter_13_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_munder;

create view covid_pmc.paragraph_staging_filter_13_boxed_text as
select
	pmcid,
	orig,
	regexp_replace(p, '<boxed-text(?: [^>]*)?>([^<]*)</boxed-text>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_math;

create view covid_pmc.paragraph_staging_filter_13_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_boxed_text;

create view covid_pmc.paragraph_staging_filter_13_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_13_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_mtr;

create view covid_pmc.paragraph_staging_filter_13_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_mtable;

create view covid_pmc.paragraph_staging_filter_13_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_13_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_13_thead as
select
	pmcid,
	orig,
	regexp_replace(p, '<thead(?: [^>]*)?>([^<]*)</thead>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_list;

create view covid_pmc.paragraph_staging_filter_13_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_thead;

create view covid_pmc.paragraph_staging_filter_13_table_wrap as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap(?: [^>]*)?>([^<]*)</table-wrap>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_14_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_13_table_wrap;

create view covid_pmc.paragraph_staging_filter_14_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_alternatives;

create view covid_pmc.paragraph_staging_filter_14a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_mtd;

create view covid_pmc.paragraph_staging_filter_14_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_14a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_mrow;

create view covid_pmc.paragraph_staging_filter_14_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_14_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_p;

create view covid_pmc.paragraph_staging_filter_14_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_msub;

create view covid_pmc.paragraph_staging_filter_14_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_inline_formula;

create view covid_pmc.paragraph_staging_filter_14_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_math;

create view covid_pmc.paragraph_staging_filter_14_disp_quote as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-quote(?: [^>]*)?>([^<]*)</disp-quote>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_disp_formula;

create view covid_pmc.paragraph_staging_filter_14_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_disp_quote;

create view covid_pmc.paragraph_staging_filter_14_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_14a_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_mtr;

create view covid_pmc.paragraph_staging_filter_14_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14a_mml_mtr;

create view covid_pmc.paragraph_staging_filter_14_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_mtable;

create view covid_pmc.paragraph_staging_filter_14_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_msup;

create view covid_pmc.paragraph_staging_filter_14_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_14_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_14_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_15_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_14_list_item;

create view covid_pmc.paragraph_staging_filter_15a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_mtd;

create view covid_pmc.paragraph_staging_filter_15_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_15a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_mrow;

create view covid_pmc.paragraph_staging_filter_15_mml_msub as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msub(?: [^>]*)?>([^<]*)</mml:msub>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_15_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_msub;

create view covid_pmc.paragraph_staging_filter_15_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_math;

create view covid_pmc.paragraph_staging_filter_15_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_disp_formula;

create view covid_pmc.paragraph_staging_filter_15_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_15_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_mtr;

create view covid_pmc.paragraph_staging_filter_15_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_mtable;

create view covid_pmc.paragraph_staging_filter_15_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_msup;

create view covid_pmc.paragraph_staging_filter_15_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_15_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_15_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_list;

create view covid_pmc.paragraph_staging_filter_15_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_16_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_15_list_item;

create view covid_pmc.paragraph_staging_filter_16a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_mml_mtd;

create view covid_pmc.paragraph_staging_filter_16_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_16a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_mml_mrow;

create view covid_pmc.paragraph_staging_filter_16_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_16_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_p;

create view covid_pmc.paragraph_staging_filter_16_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_mml_math;

create view covid_pmc.paragraph_staging_filter_16_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_disp_formula;

create view covid_pmc.paragraph_staging_filter_16_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_mml_mtr;

create view covid_pmc.paragraph_staging_filter_16_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_mml_mtable;

create view covid_pmc.paragraph_staging_filter_16_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_mml_msup;

create view covid_pmc.paragraph_staging_filter_16_mml_mstyle as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mstyle(?: [^>]*)?>([^<]*)</mml:mstyle>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_16_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_mml_mstyle;

create view covid_pmc.paragraph_staging_filter_16_disp_formula_group as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula-group(?: [^>]*)?>([^<]*)</disp-formula-group>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_list;

create view covid_pmc.paragraph_staging_filter_17_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_16_disp_formula_group;

create view covid_pmc.paragraph_staging_filter_17a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_mml_mtd;

create view covid_pmc.paragraph_staging_filter_17_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_17a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_mml_mrow;

create view covid_pmc.paragraph_staging_filter_17_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_17_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_inline_formula;

create view covid_pmc.paragraph_staging_filter_17_mml_msqrt as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msqrt(?: [^>]*)?>([^<]*)</mml:msqrt>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_mml_math;

create view covid_pmc.paragraph_staging_filter_17_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_mml_msqrt;

create view covid_pmc.paragraph_staging_filter_17_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_mml_mtr;

create view covid_pmc.paragraph_staging_filter_17_disp_formula_group as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula-group(?: [^>]*)?>([^<]*)</disp-formula-group>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_mml_mtable;

create view covid_pmc.paragraph_staging_filter_17_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_disp_formula_group;

create view covid_pmc.paragraph_staging_filter_17_statement as
select
	pmcid,
	orig,
	regexp_replace(p, '<statement(?: [^>]*)?>([^<]*)</statement>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_17_caption as
select
	pmcid,
	orig,
	regexp_replace(p, '<caption(?: [^>]*)?>([^<]*)</caption>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_statement;

create view covid_pmc.paragraph_staging_filter_18_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_17_caption;

create view covid_pmc.paragraph_staging_filter_18a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18_mml_mrow;

create view covid_pmc.paragraph_staging_filter_18_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_18_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18_inline_formula;

create view covid_pmc.paragraph_staging_filter_18_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18_mml_mtr;

create view covid_pmc.paragraph_staging_filter_18_fig as
select
	pmcid,
	orig,
	regexp_replace(p, '<fig(?: [^>]*)?>([^<]*)</fig>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18_mml_mtable;

create view covid_pmc.paragraph_staging_filter_18_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18_fig;

create view covid_pmc.paragraph_staging_filter_18_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_18a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18_mml_mtd;

create view covid_pmc.paragraph_staging_filter_18_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_19_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_18_mml_math;

create view covid_pmc.paragraph_staging_filter_19a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19_mml_mrow;

create view covid_pmc.paragraph_staging_filter_19_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?>([^<]*)</td>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_19a_td as
select
	pmcid,
	orig,
	regexp_replace(p, '<td(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19_td;

create view covid_pmc.paragraph_staging_filter_19_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19a_td;

create view covid_pmc.paragraph_staging_filter_19_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19_mml_math;

create view covid_pmc.paragraph_staging_filter_19_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19_disp_formula;

create view covid_pmc.paragraph_staging_filter_19_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19_mml_mtable;

create view covid_pmc.paragraph_staging_filter_19_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19_mml_mtr;

create view covid_pmc.paragraph_staging_filter_19_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19_mml_msup;

create view covid_pmc.paragraph_staging_filter_19_mml_mfrac as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfrac(?: [^>]*)?>([^<]*)</mml:mfrac>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_20_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_19_mml_mfrac;

create view covid_pmc.paragraph_staging_filter_20a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20_mml_mrow;

create view covid_pmc.paragraph_staging_filter_20_inline_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<inline-formula(?: [^>]*)?>([^<]*)</inline-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_20_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20_inline_formula;

create view covid_pmc.paragraph_staging_filter_20_tr as
select
	pmcid,
	orig,
	regexp_replace(p, '<tr(?: [^>]*)?>([^<]*)</tr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20_mml_mtable;

create view covid_pmc.paragraph_staging_filter_20_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20_tr;

create view covid_pmc.paragraph_staging_filter_20_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20_alternatives;

create view covid_pmc.paragraph_staging_filter_20a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20_mml_mtd;

create view covid_pmc.paragraph_staging_filter_20_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_20_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20_p;

create view covid_pmc.paragraph_staging_filter_21_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_20_mml_math;

create view covid_pmc.paragraph_staging_filter_21_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_21_list_item;

create view covid_pmc.paragraph_staging_filter_21_tbody as
select
	pmcid,
	orig,
	regexp_replace(p, '<tbody(?: [^>]*)?>([^<]*)</tbody>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_21_disp_formula;

create view covid_pmc.paragraph_staging_filter_21_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_21_tbody;

create view covid_pmc.paragraph_staging_filter_21_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_21_mml_mrow;

create view covid_pmc.paragraph_staging_filter_21_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_21_mml_mtr;

create view covid_pmc.paragraph_staging_filter_21_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_21_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_21_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_21_alternatives;

create view covid_pmc.paragraph_staging_filter_21a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_21_mml_mtd;

create view covid_pmc.paragraph_staging_filter_22_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_21a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_22_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22_disp_formula;

create view covid_pmc.paragraph_staging_filter_22a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22_mml_mrow;

create view covid_pmc.paragraph_staging_filter_22_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_22_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22_mml_mtable;

create view covid_pmc.paragraph_staging_filter_22a_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22_mml_mtr;

create view covid_pmc.paragraph_staging_filter_22_table as
select
	pmcid,
	orig,
	regexp_replace(p, '<table(?: [^>]*)?>([^<]*)</table>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22a_mml_mtr;

create view covid_pmc.paragraph_staging_filter_22_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22_table;

create view covid_pmc.paragraph_staging_filter_22_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_22_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22_list;

create view covid_pmc.paragraph_staging_filter_23_table_wrap as
select
	pmcid,
	orig,
	regexp_replace(p, '<table-wrap(?: [^>]*)?>([^<]*)</table-wrap>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_22_p;

create view covid_pmc.paragraph_staging_filter_23_list_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<list-item(?: [^>]*)?>([^<]*)</list-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_23_table_wrap;

create view covid_pmc.paragraph_staging_filter_23_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_23_list_item;

create view covid_pmc.paragraph_staging_filter_23a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_23_mml_mrow;

create view covid_pmc.paragraph_staging_filter_23_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_23a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_23_mml_mfenced as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mfenced(?: [^>]*)?>([^<]*)</mml:mfenced>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_23_mml_mtable;

create view covid_pmc.paragraph_staging_filter_23_mml_msup as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:msup(?: [^>]*)?>([^<]*)</mml:msup>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_23_mml_mfenced;

create view covid_pmc.paragraph_staging_filter_23_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_23_mml_msup;

create view covid_pmc.paragraph_staging_filter_24_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_23_p;

create view covid_pmc.paragraph_staging_filter_24a_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_24_mml_mrow;

create view covid_pmc.paragraph_staging_filter_24_statement as
select
	pmcid,
	orig,
	regexp_replace(p, '<statement(?: [^>]*)?>([^<]*)</statement>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_24a_mml_mrow;

create view covid_pmc.paragraph_staging_filter_24_def as
select
	pmcid,
	orig,
	regexp_replace(p, '<def(?: [^>]*)?>([^<]*)</def>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_24_statement;

create view covid_pmc.paragraph_staging_filter_24_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<list(?: [^>]*)?>([^<]*)</list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_24_def;

create view covid_pmc.paragraph_staging_filter_24_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?>([^<]*)</mml:mtd>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_24_list;

create view covid_pmc.paragraph_staging_filter_24a_mml_mtd as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtd(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_24_mml_mtd;

create view covid_pmc.paragraph_staging_filter_24_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_24a_mml_mtd;

create view covid_pmc.paragraph_staging_filter_25_mml_mrow as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mrow(?: [^>]*)?>([^<]*)</mml:mrow>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_24_mml_math;

create view covid_pmc.paragraph_staging_filter_25_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?>([^<]*)</mml:mtr>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_25_mml_mrow;

create view covid_pmc.paragraph_staging_filter_25a_mml_mtr as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtr(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_25_mml_mtr;

create view covid_pmc.paragraph_staging_filter_25_def_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<def-item(?: [^>]*)?>([^<]*)</def-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_25a_mml_mtr;

create view covid_pmc.paragraph_staging_filter_25_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_25_def_item;

create view covid_pmc.paragraph_staging_filter_25_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_25_alternatives;

create view covid_pmc.paragraph_staging_filter_26_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_25_mml_math;

create view covid_pmc.paragraph_staging_filter_26_mml_mtable as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:mtable(?: [^>]*)?>([^<]*)</mml:mtable>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_26_disp_formula;

create view covid_pmc.paragraph_staging_filter_26_def_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<def-list(?: [^>]*)?>([^<]*)</def-list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_26_mml_mtable;

create view covid_pmc.paragraph_staging_filter_26_alternatives as
select
	pmcid,
	orig,
	regexp_replace(p, '<alternatives(?: [^>]*)?>([^<]*)</alternatives>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_26_def_list;

create view covid_pmc.paragraph_staging_filter_27_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_26_alternatives;

create view covid_pmc.paragraph_staging_filter_27_mml_math as
select
	pmcid,
	orig,
	regexp_replace(p, '<mml:math(?: [^>]*)?>([^<]*)</mml:math>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_27_p;

create view covid_pmc.paragraph_staging_filter_28_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?>([^<]*)</disp-formula>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_27_mml_math;

create view covid_pmc.paragraph_staging_filter_28a_disp_formula as
select
	pmcid,
	orig,
	regexp_replace(p, '<disp-formula(?: [^>]*)?/>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_28_disp_formula;

create view covid_pmc.paragraph_staging_filter_28_def as
select
	pmcid,
	orig,
	regexp_replace(p, '<def(?: [^>]*)?>([^<]*)</def>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_28a_disp_formula;

create view covid_pmc.paragraph_staging_filter_29_def_item as
select
	pmcid,
	orig,
	regexp_replace(p, '<def-item(?: [^>]*)?>([^<]*)</def-item>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_28_def;

create view covid_pmc.paragraph_staging_filter_30_def_list as
select
	pmcid,
	orig,
	regexp_replace(p, '<def-list(?: [^>]*)?>([^<]*)</def-list>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_29_def_item;

create view covid_pmc.paragraph_staging_filter_31_p as
select
	pmcid,
	orig,
	regexp_replace(p, '<p(?: [^>]*)?>([^<]*)</p>', '\1', 'g') as p
from covid_pmc.paragraph_staging_filter_30_def_list;

create view covid_pmc.sec_para_final as
select
	pmcid,
	orig,
	p
from covid_pmc.paragraph_staging_filter_31_p;

select * from covid_pmc.sec_para_final where p ~ '<' limit 10;

