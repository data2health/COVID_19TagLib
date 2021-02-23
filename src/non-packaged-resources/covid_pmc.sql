create or replace function xml_pretty(xml)
returns xml as $$
  -- https://gist.github.com/LeKovr/e7b365d2dca58e4bc8c8f4695e0ca435
  -- requires xml2 pg extension
  -- https://postgres.cz/wiki/PostgreSQL_SQL_Tricks#Pretty_xml_formating
  select xslt_process($1::text,'
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:strip-space elements="*" />
<xsl:output method="xml" indent="yes" />
<xsl:template match="node() | @*">
<xsl:copy>
<xsl:apply-templates select="node() | @*" />
</xsl:copy>
</xsl:template>
</xsl:stylesheet>')::xml
$$ language sql immutable strict;

create materialized view section as
select
	pmcid,
	pmid,
	xmltable.seqnum,
	xmltable.id,
	xmltable.label,
	xmltable.title,
	xmltable.sec
from
	xml,
	lateral xmltable(('//article/body/sec') passing (xml.xml) columns
		seqnum for ordinality,
		id text path('@id'),
		label text path('label'),
		title text path('title'),
		sec xml path('.')
	)
;

create materialized view section_paragraph as
select
	pmcid,
	pmid,
	seqnum,
	xmltable.seqnum2,
	xmltable.id,
	xmltable.p
from
	section,
	lateral xmltable(('//sec/p') passing (section.sec) columns
		seqnum2 for ordinality,
		id text path('@id'),
		p xml path('.')
	)
;

create materialized view section_paragraph_xref as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	xmltable.seqnum3,
	xmltable.rid,
	xmltable.ref_type,
	xmltable.xref
from
	section_paragraph,
	lateral xmltable(('//p/xref') passing (section_paragraph.p) columns
		seqnum3 for ordinality,
		rid text path('@rid'),
		ref_type text path('@ref-type'),
		xref text path('.')
	)
;

create materialized view section_paragraph_fig as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	xmltable.seqnum3,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	section_paragraph,
	lateral xmltable(('//p/fig') passing (section_paragraph.p) columns
		seqnum3 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view section_paragraph_table as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	xmltable.seqnum3,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	section_paragraph,
	lateral xmltable(('//p/table-wrap') passing (section_paragraph.p) columns
		seqnum3 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view sub1section as
select
	pmcid,
	pmid,
	seqnum,
	xmltable.seqnum2,
	xmltable.id,
	xmltable.label,
	xmltable.title,
	xmltable.sec
from
	section,
	lateral xmltable(('//sec/sec') passing (section.sec) columns
		seqnum2 for ordinality,
		id text path('@id'),
		label text path('label'),
		title text path('title'),
		sec xml path('.')
	)
;

create materialized view sub1section_paragraph as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	xmltable.seqnum3,
	xmltable.id,
	xmltable.p
from
	sub1section,
	lateral xmltable(('//sec/p') passing (sub1section.sec) columns
		seqnum3 for ordinality,
		id text path('@id'),
		p xml path('.')
	)
;

create materialized view sub1section_paragraph_xref as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	xmltable.seqnum4,
	xmltable.rid,
	xmltable.ref_type,
	xmltable.xref
from
	sub1section_paragraph,
	lateral xmltable(('//p/xref') passing (sub1section_paragraph.p) columns
		seqnum4 for ordinality,
		rid text path('@rid'),
		ref_type text path('@ref-type'),
		xref text path('.')
	)
;

create materialized view sub1section_paragraph_fig as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	xmltable.seqnum4,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	sub1section_paragraph,
	lateral xmltable(('//p/fig') passing (sub1section_paragraph.p) columns
		seqnum4 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view sub1section_paragraph_table as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	xmltable.seqnum4,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	sub1section_paragraph,
	lateral xmltable(('//p/table-wrap') passing (sub1section_paragraph.p) columns
		seqnum4 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view sub2section as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	xmltable.seqnum3,
	xmltable.id,
	xmltable.label,
	xmltable.title,
	xmltable.sec
from
	sub1section,
	lateral xmltable(('//sec/sec') passing (sub1section.sec) columns
		seqnum3 for ordinality,
		id text path('@id'),
		label text path('label'),
		title text path('title'),
		sec xml path('.')
	)
;

create materialized view sub2section_paragraph as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	xmltable.seqnum4,
	xmltable.id,
	xmltable.p
from
	sub2section,
	lateral xmltable(('//sec/p') passing (sub2section.sec) columns
		seqnum4 for ordinality,
		id text path('@id'),
		p xml path('.')
	)
;

create materialized view sub2section_paragraph_xref as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	xmltable.seqnum5,
	xmltable.rid,
	xmltable.ref_type,
	xmltable.xref
from
	sub2section_paragraph,
	lateral xmltable(('//p/xref') passing (sub2section_paragraph.p) columns
		seqnum5 for ordinality,
		rid text path('@rid'),
		ref_type text path('@ref-type'),
		xref text path('.')
	)
;

create materialized view sub2section_paragraph_fig as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	xmltable.seqnum5,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	sub2section_paragraph,
	lateral xmltable(('//p/fig') passing (sub2section_paragraph.p) columns
		seqnum5 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view sub2section_paragraph_table as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	xmltable.seqnum5,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	sub2section_paragraph,
	lateral xmltable(('//p/table-wrap') passing (sub2section_paragraph.p) columns
		seqnum5 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view sub3section as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	xmltable.seqnum4,
	xmltable.id,
	xmltable.label,
	xmltable.title,
	xmltable.sec
from
	sub2section,
	lateral xmltable(('//sec/sec') passing (sub2section.sec) columns
		seqnum4 for ordinality,
		id text path('@id'),
		label text path('label'),
		title text path('title'),
		sec xml path('.')
	)
;

create materialized view sub3section_paragraph as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	xmltable.seqnum5,
	xmltable.id,
	xmltable.p
from
	sub3section,
	lateral xmltable(('//sec/p') passing (sub3section.sec) columns
		seqnum5 for ordinality,
		id text path('@id'),
		p xml path('.')
	)
;

create materialized view sub3section_paragraph_xref as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	seqnum5,
	xmltable.seqnum6,
	xmltable.rid,
	xmltable.ref_type,
	xmltable.xref
from
	sub3section_paragraph,
	lateral xmltable(('//p/xref') passing (sub3section_paragraph.p) columns
		seqnum6 for ordinality,
		rid text path('@rid'),
		ref_type text path('@ref-type'),
		xref text path('.')
	)
;

create materialized view sub3section_paragraph_fig as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	seqnum5,
	xmltable.seqnum6,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	sub3section_paragraph,
	lateral xmltable(('//p/fig') passing (sub3section_paragraph.p) columns
		seqnum6 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view sub3section_paragraph_table as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	seqnum5,
	xmltable.seqnum6,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	sub3section_paragraph,
	lateral xmltable(('//p/table-wrap') passing (sub3section_paragraph.p) columns
		seqnum6 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view sub4section as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	xmltable.seqnum5,
	xmltable.id,
	xmltable.label,
	xmltable.title,
	xmltable.sec
from
	sub3section,
	lateral xmltable(('//sec/sec') passing (sub3section.sec) columns
		seqnum5 for ordinality,
		id text path('@id'),
		label text path('label'),
		title text path('title'),
		sec xml path('.')
	)
;

create materialized view sub4section_paragraph as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	seqnum5,
	xmltable.seqnum6,
	xmltable.id,
	xmltable.p
from
	sub4section,
	lateral xmltable(('//sec/p') passing (sub4section.sec) columns
		seqnum6 for ordinality,
		id text path('@id'),
		p xml path('.')
	)
;

create materialized view sub4section_paragraph_xref as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	seqnum5,
	seqnum6,
	xmltable.seqnum7,
	xmltable.rid,
	xmltable.ref_type,
	xmltable.xref
from
	sub4section_paragraph,
	lateral xmltable(('//p/xref') passing (sub4section_paragraph.p) columns
		seqnum7 for ordinality,
		rid text path('@rid'),
		ref_type text path('@ref-type'),
		xref text path('.')
	)
;

create materialized view sub4section_paragraph_fig as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	seqnum5,
	seqnum6,
	xmltable.seqnum7,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	sub4section_paragraph,
	lateral xmltable(('//p/fig') passing (sub4section_paragraph.p) columns
		seqnum7 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view sub4section_paragraph_table as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	seqnum5,
	seqnum6,
	xmltable.seqnum7,
	xmltable.id,
	xmltable.position,
	xmltable.orientation,
	xmltable.label,
	xmltable.caption,
	xmltable.fig
from
	sub4section_paragraph,
	lateral xmltable(('//p/table-wrap') passing (sub4section_paragraph.p) columns
		seqnum7 for ordinality,
		id text path('@id'),
		position text path('@position'),
		orientation text path('@orientation'),
		label text path('label'),
		caption xml path('caption'),
		fig xml path('.')
	)
;

create materialized view sub5section as
select
	pmcid,
	pmid,
	seqnum,
	seqnum2,
	seqnum3,
	seqnum4,
	seqnum5,
	xmltable.seqnum6,
	xmltable.id,
	xmltable.label,
	xmltable.title,
	xmltable.sec
from
	sub4section,
	lateral xmltable(('//sec/sec') passing (sub4section.sec) columns
		seqnum6 for ordinality,
		id text path('@id'),
		label text path('label'),
		title text path('title'),
		sec xml path('.')
	)
;

--------------------------

create view sec_para_filter as
select regexp_replace(p::text, '<p[^>]*>(.*)</p>', '\1') as p
from section_paragraph;

create view sec_para_bib_filter as
select
	regexp_replace(p, '([\[(]<xref [^>]*bibr[^>]*>[^<]*</xref>( *[,;] *<xref [^>]*bibr[^>]*>[^<]*</xref>)*[\])])', '', 'g') as p
from sec_para_filter;

create view sec_para_xref_filter as
select
	regexp_replace(p, '<xref[^>]*>([^<*]*)</xref>', '\1', 'g') as p
from sec_para_bib_filter;

create view sec_para_fig_filter as
select
	regexp_replace(p, '<fig [^>]*>.*</fig>', '', 'g') as p
from sec_para_xref_filter;

create view sec_para_table_filter as
select
	regexp_replace(p, '<table-wrap [^>]*>.*</table-wrap>', '', 'g') as p
from sec_para_fig_filter;

create view sec_para_italic_filter as
select
	regexp_replace(p, '<italic>([^<]*)</italic>', '\1', 'g') as p
from sec_para_table_filter;

create view sec_para_sub_filter as
select
	regexp_replace(p, '<sub>([^<]*)</sub>', '\1', 'g') as p
from sec_para_italic_filter;

create view sec_para_sup_filter as
select
	regexp_replace(p, '<sup>([^<]*)</sup>', '\1', 'g') as p
from sec_para_sub_filter;

create view sec_para_link_filter as
select
	regexp_replace(p, '\(<ext-link ([^<]*)</ext-link>\)', '', 'g') as p
from sec_para_sup_filter;

create view sec_para_math_space_filter as
select
	regexp_replace(p, '<mml:mspace[^>]*/>', ' ', 'g') as p
from sec_para_link_filter;

create view sec_para_math_text_filter as
select
	regexp_replace(p, '<mml:mtext[^>]*>([^<]*)</mml:mtext>', '\1', 'g') as p
from sec_para_math_space_filter;

create view sec_para_math_element_filter as
select
	regexp_replace(p, '<mml:m[ion][^>]*>([^<]*)</mml:m[ion]>', '\1', 'g') as p
from sec_para_math_text_filter;

create view sec_para_math_sup_filter as
select
	regexp_replace(p, '<mml:msup[^>]*>([^<]*)</mml:msup>', '\1', 'g') as p
from sec_para_math_element_filter;

create view sec_para_math_sub_filter as
select
	regexp_replace(p, '<mml:msub[^>]*>([^<]*)</mml:msub>', '\1', 'g') as p
from sec_para_math_sup_filter;

create view sec_para_math_subsup_filter as
select
	regexp_replace(p, '<mml:msubsup[^>]*>([^<]*)</mml:msubsup>', '\1', 'g') as p
from sec_para_math_sub_filter;

create view sec_para_math_frac_filter as
select
	regexp_replace(p, '<mml:mfrac[^>]*>([^<]*)</mml:mfrac>', '\1', 'g') as p
from sec_para_math_subsup_filter;

create view sec_para_math_row_filter as
select
	regexp_replace(p, '<mml:mrow[^>]*>([^<]*)</mml:mrow>', '\1', 'g') as p
from sec_para_math_frac_filter;

create view sec_para_math_math_filter as
select
	regexp_replace(p, '<mml:math[^>]*>([^<]*)</mml:math>', '\1', 'g') as p
from sec_para_math_row_filter;

create view sec_para_math_formula_filter as
select
	regexp_replace(p, '<inline-formula[^>]*>([^<]*)</inline-formula>', '\1', 'g') as p
from sec_para_math_math_filter
limit 1000;
