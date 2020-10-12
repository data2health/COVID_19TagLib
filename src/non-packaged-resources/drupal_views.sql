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

CREATE SCHEMA n3c_admin;

CREATE VIEW n3c_admin.subgroup AS
SELECT
    nid,
    vid,
    title,
    field_subgroup_mission_value as mission,
    field_meeting_time_value as meeting_time,
    field_subgroup_leads_value as leads,
    field_join_docs_value as join_docs,
    field_google_drive_uri as gdrive_url,
    field_google_drive_title as gdrive_anchor,
    field_under_workstream_target_id as workstream_nid,
    to_timestamp(node_field_data.created::numeric) as created,
    to_timestamp(node_field_data.changed::numeric) as changed
FROM
    node_field_data
LEFT JOIN
    node__body
ON  node_field_data.nid = node__body.entity_id
AND node__body.deleted = '0'
LEFT JOIN
    node__field_subgroup_mission
ON  node_field_data.nid = node__field_subgroup_mission.entity_id
AND node__field_subgroup_mission.deleted = '0'
LEFT JOIN
    node__field_meeting_time
ON  node_field_data.nid = node__field_meeting_time.entity_id
AND node__field_meeting_time.deleted = '0'
LEFT JOIN
    node__field_subgroup_leads
ON  node_field_data.nid = node__field_subgroup_leads.entity_id
AND node__field_subgroup_leads.deleted = '0'
LEFT JOIN
    node__field_join_docs
ON  node_field_data.nid = node__field_join_docs.entity_id
AND node__field_join_docs.deleted = '0'
LEFT JOIN
    node__field_google_drive
ON  node_field_data.nid = node__field_google_drive.entity_id
AND node__field_google_drive.deleted = '0'
LEFT JOIN
    node__field_under_workstream
ON  node_field_data.nid = node__field_under_workstream.entity_id
AND node__field_under_workstream.deleted = '0'
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'subgroup'
;

CREATE VIEW n3c_admin.workstream AS
SELECT
    nid,
    vid,
    title,
    body_value,
    body_summary,
    field_mission_value as mission,
    field_github_link_uri as github_url,
    field_github_link_title as github_anchor,
    field_onboard_link_uri as onboard_url,
    field_onboard_link_title as onboard_anchor,
    field_public_goo_uri as ggroup_url,
    field_public_goo_title as ggroup_anchor,
    field_slack_link_uri as slack_url,
    field_slack_link_title as slack_anchor,
    field_zoom_link_uri as zoom_url,
    field_zoom_link_title as zoom_anchor,
    field_video_value as video_url,
    uri as icon_url,
    field_workstream_icon_alt as icon_alt_text,
    field_workstream_icon_title as icon_title,
    field_workstream_icon_width as icon_width,
    field_workstream_icon_height as icon_height,
    to_timestamp(node_field_data.created::numeric) as created,
    to_timestamp(node_field_data.changed::numeric) as changed
FROM
    node_field_data
LEFT JOIN
    node__body
ON  node_field_data.nid = node__body.entity_id
AND node__body.deleted = '0'
LEFT JOIN
    node__field_mission
ON  node_field_data.nid = node__field_mission.entity_id
AND node__field_mission.deleted = '0'
LEFT JOIN
    node__field_github_link
ON  node_field_data.nid = node__field_github_link.entity_id
AND node__field_github_link.deleted = '0'
LEFT JOIN
    node__field_onboard_link
ON  node_field_data.nid = node__field_onboard_link.entity_id
AND node__field_onboard_link.deleted = '0'
LEFT JOIN
    node__field_public_goo
ON  node_field_data.nid = node__field_public_goo.entity_id
AND node__field_public_goo.deleted = '0'
LEFT JOIN
    node__field_slack_link
ON  node_field_data.nid = node__field_slack_link.entity_id
AND node__field_slack_link.deleted = '0'
LEFT JOIN
    node__field_zoom_link
ON  node_field_data.nid = node__field_zoom_link.entity_id
AND node__field_zoom_link.deleted = '0'
LEFT JOIN
    node__field_video
ON  node_field_data.nid = node__field_video.entity_id
AND node__field_video.deleted = '0'
LEFT JOIN
    node__field_workstream_icon
ON  node_field_data.nid = node__field_workstream_icon.entity_id
AND node__field_workstream_icon.deleted = '0'
LEFT JOIN
    file_managed
ON  node__field_workstream_icon.field_workstream_icon_target_id = file_managed.fid
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'workstream'
;

CREATE VIEW n3c_admin.workstream_person AS
SELECT
    nid,
    vid,
    delta,
    field_workstream_people_target_id as person_nid
FROM
    node_field_data
JOIN
    node__field_workstream_people
ON  node_field_data.nid = node__field_workstream_people.entity_id
AND node__field_workstream_people.deleted = '0'
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'workstream'
;

CREATE VIEW n3c_admin.bio AS
SELECT
    nid,
    vid,
    title,
    body_value,
    body_summary,
    filename,
    uri as headshot_url,
    field_headshot_alt as headshot_alt,
    field_headshot_title as headshot_title,
    field_headshot_width as headshot_width,
    field_headshot_height as headshot_height,
    field_role_detailed_value as detailed_role,
    field_webpage_uri as webpage_url,
    field_workstream_role_value as workstream_role,
    to_timestamp(node_field_data.created::numeric) as created,
    to_timestamp(node_field_data.changed::numeric) as changed
FROM
    node_field_data
LEFT JOIN
    node__body
ON  node_field_data.nid = node__body.entity_id
AND node__body.deleted = '0'
LEFT JOIN
    node__field_role_detailed
ON  node_field_data.nid = node__field_role_detailed.entity_id
AND node__field_role_detailed.deleted = '0'
LEFT JOIN
    node__field_headshot
ON  node_field_data.nid = node__field_headshot.entity_id
AND node__field_headshot.deleted = '0'
LEFT JOIN
    file_managed
ON  node__field_headshot.field_headshot_target_id = file_managed.fid
LEFT JOIN
    node__field_webpage
ON  node_field_data.nid = node__field_webpage.entity_id
AND node__field_webpage.deleted = '0'
LEFT JOIN
    node__field_workstream_role
ON  node_field_data.nid = node__field_workstream_role.entity_id
AND node__field_workstream_role.deleted = '0'
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'bio'
;

CREATE VIEW n3c_admin.bio_role AS
SELECT
    nid,
    vid,
    delta,
    field_role_value as role
FROM
    node_field_data
JOIN
    node__field_role
ON  node_field_data.nid = node__field_role.entity_id
AND node__field_role.deleted = '0'
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'bio'
;

CREATE VIEW n3c_admin.bio_substream AS
SELECT
    nid,
    vid,
    delta,
    field_substream_value as substream
FROM
    node_field_data
JOIN
    node__field_substream
ON  node_field_data.nid = node__field_substream.entity_id
AND node__field_substream.deleted = '0'
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'bio'
;

CREATE VIEW n3c_admin.bio_workstream AS
SELECT
    nid,
    vid,
    delta,
    field_workstream_value as workstream
FROM
    node_field_data
JOIN
    node__field_workstream
ON  node_field_data.nid = node__field_workstream.entity_id
AND node__field_workstream.deleted = '0'
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'bio'
;

CREATE VIEW n3c_admin.domain_team AS
SELECT
	nid,
	vid,
	title,
	field_cross_cutting_value as cross_cutting,
	field_team_description_value as description,
    field_short_summary_value as summary,
    field_mailing_list_address_value as mailing_list_address,
    field_mailing_list_join_link_uri as mailing_list_url,
    field_mailing_list_join_link_title as mailing_list_anchor,
    field_team_google_drive_uri as gdrive_url,
    field_team_google_drive_title as gdrive_anchor,
    field_slack_uri as slack_url,
    field_slack_title as slack_anchor,
	to_timestamp(created::numeric) as created,
	to_timestamp(changed::numeric) as changed
FROM
	node_field_data
LEFT JOIN
	node__field_cross_cutting
ON  node_field_data.nid = node__field_cross_cutting.entity_id
AND node__field_cross_cutting.deleted = '0'
LEFT JOIN
    node__field_team_description
ON  node_field_data.nid = node__field_team_description.entity_id
AND node__field_team_description.deleted = '0'
LEFT JOIN
    node__field_short_summary
ON  node_field_data.nid = node__field_short_summary.entity_id
AND node__field_short_summary.deleted = '0'
LEFT JOIN
    node__field_mailing_list_address
ON  node_field_data.nid = node__field_mailing_list_address.entity_id
AND node__field_mailing_list_address.deleted = '0'
LEFT JOIN
    node__field_mailing_list_join_link
ON  node_field_data.nid = node__field_mailing_list_join_link.entity_id
AND node__field_mailing_list_join_link.deleted = '0'
LEFT JOIN
    node__field_team_google_drive
ON  node_field_data.nid = node__field_team_google_drive.entity_id
AND node__field_team_google_drive.deleted = '0'
LEFT JOIN
    node__field_slack
ON  node_field_data.nid = node__field_slack.entity_id
AND node__field_slack.deleted = '0'
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'domain_team'
ORDER BY title ASC NULLS FIRST;

CREATE VIEW n3c_admin.domain_team_lead AS
SELECT
    nid,
    vid,
    delta,
    field_leads_target_id as lead_nid
FROM
    node_field_data
JOIN
    node__field_leads
ON  node_field_data.nid = node__field_leads.entity_id
AND node__field_leads.deleted = '0'
WHERE node_field_data.status = '1'
  AND node_field_data.type = 'domain_team'
;
