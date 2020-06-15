truncate n3c_admin.mapping_ncats_backup;
insert into n3c_admin.mapping_ncats_backup select * from n3c_admin.mapping_ncats;

truncate n3c_admin.mapping_irb_backup;
insert into n3c_admin.mapping_irb_backup select * from n3c_admin.mapping_irb;

truncate n3c_admin.mapping_acquisition_backup;
insert into n3c_admin.mapping_acquisition_backup select * from n3c_admin.mapping_acquisition;

truncate n3c_admin.ncats;
update n3c_admin.ncats_master set dta_sent = dta_executed where dta_sent~'XXXXX';
insert into n3c_admin.ncats 
select
	site_name,
	batch_email_,
	email,
	first,
	last,
	role__pi_contact_,
	dta_sent::date,
	dta_executed::date,
	dua_sent::date,
	dua_executed::date,
	ctsa_non_ctsa
from n3c_admin.ncats_master
where site_name is not null and site_name != 'Sent batch email to all the below' and site_name != 'Counts';

truncate n3c_admin.n3c;
insert into n3c_admin.n3c
select site,ctsa_community,groups_consortium___e_g___ny__ca__petal__etc__,person_to_make_contact,contact_made___type_yes_no__or_use_dropdown_menu_::boolean,pi_contact_at_site,pi_email,status_of_data_ingest__use_dropdown_menu_,status_of_data_ingest_date::date,date_script_sent_to_site::date,date_initial_data_received::date,null,null,notes__actions_issues_
from n3c_admin.status;

truncate n3c_admin.acquisition;
insert into n3c_admin.acquisition(site_name,first_meeting,primary_technical_contact,data_model,passing_data)
select site_name,substring(date_of_first_meeting,'[0-9]+/[0-9]+/[0-9]+')::date,primary_technical_contact,data_model,passing_data_ 
from n3c_admin.site_status;

truncate n3c_admin.irb;
insert into n3c_admin.irb
 SELECT irbstatus.institution,
    (irbstatus.initial_contact_with_jhm)::date AS initial_contact,
    ("substring"(irbstatus.lcq_finalized_submitted_to_eirb_for_review, '[0-9]+/[0-9]+/[0-9]+'::text))::date AS submitted,
    (irbstatus.sirb_onboarding_approved)::date AS approved,
    (irbstatus.date_of_local_irb_approval)::date AS local_approval,
    (irbstatus.receipt_of_local_irb_approval_letter)::date AS local_receipt,
    (COALESCE(irbstatus.sirb_onboarding_approved, irbstatus.receipt_of_local_irb_approval_letter))::date AS cleared
   FROM n3c_admin.irbstatus
  WHERE (initial_contact_with_jhm is not null);

delete from n3c_admin.mapping_ncats
where n3c not in (select site from n3c_admin.n3c) or ncats not in (select site_name from n3c_admin.ncats);

delete from n3c_admin.mapping_irb
where n3c not in (select site from n3c_admin.n3c) or irb not in (select institution from n3c_admin.irb);

delete from n3c_admin.mapping_acquisition
where n3c not in (select site from n3c_admin.n3c) or acquisition not in (select site_name from n3c_admin.acquisition);

create materialized view n3c_admin.personnel as
select
	timestamp::timestamp as onboarded,
	email_address,
	first_name,
	last_name,
    google__g_suite_enabled_email__if_different_from_primary_add as gmail,
    slack_email_if_different_from_primary_address as slack,
    github_handle as github,
    case when substring(orcid_id from '[0-9]+-[0-9]+-[0-9]+-[0-9X]+') = '0000-0000-0000-0000' then null else substring(orcid_id from '[0-9]+-[0-9]+-[0-9]+-[0-9X]+') end as orcid,
    assistant_email,
    institutional_affiliation__ctsa__or_employer__url_ as institution_url,
    root_domain__ as root_domain,
    institution__ as institution,
	would_you_like_to_onboard_to_n3c_::boolean as n3c_onboard,
	would_you_like_to_onboard_to_cd2h_::boolean as cd2h_onboard,
    substring(n3c_all_hands from '^[^ ]+')::boolean as n3c_all_hands,
    substring(data_partnership___governance from '^[^ ]+')::boolean as governance,
    substring(phenotype___data_acquisition from '^[^ ]+')::boolean as data_acquisition,
    substring(data_ingestion___harmonization from '^[^ ]+')::boolean as harmonization,
    substring(collaborative_analytics from '^[^ ]+')::boolean as collaborative_analytics,
    substring(synthetic_data from '^[^ ]+')::boolean as synthetic_data,
    substring(portal___dashboards__collaborative_analytics__subgroup from '^[^ ]+')::boolean as portal_dashboards,
    substring(tools___resources__collaborative_analytics__subgroup from '^[^ ]+')::boolean as tools_resources,
    substring(clinical_scenarios___data_analytics__collaborative_analytics from '^[^ ]+')::boolean as clinical_scenarios,
    substring(nlp__collaborative_analytics_ from '^[^ ]+')::boolean as nlp,
    would_you_like_to_onboard_to_cd2h__1::boolean as cd2h_onboard2,
    substring(cd2h_all_hands from '^[^ ]+')::boolean as cd2h_all_hands,
    substring(tools___cloud_infrastructure from '^[^ ]+')::boolean as tools_cloud,
    substring(next_generation_data_sharing from '^[^ ]+')::boolean as next_gen_data,
    substring(informatics_maturity_and_best_practices from '^[^ ]+')::boolean as maturity,
    substring(resource_discovery from '^[^ ]+')::boolean as resource_discovery
from n3c_admin.onboard_master
where email_address is not null
;
