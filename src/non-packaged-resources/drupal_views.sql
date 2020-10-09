-- content type: Domain Team
--	Title (string)
--	description (full HTML)
--	short_summary (basic HTML)
--	Leads (content type reference)
--	Google Drive (URL, link text)
--	Mailing List Address (string)
--	Join Us! (URL, link text)
--	SLACK (URL, link text)
--	cross-cutting (checkbox)

SELECT
	nid,
	vid,
	title,
	field_cross_cutting_value as cross_cutting,
	to_timestamp(created::numeric) as created,
	to_timestamp(changed::numeric) as changed
FROM
	node_field_data
LEFT JOIN
	node__field_cross_cutting
ON  node_field_data.nid = node__field_cross_cutting.entity_id
AND node__field_cross_cutting.deleted = '0'
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'domain_team'
ORDER BY title ASC NULLS FIRST;
