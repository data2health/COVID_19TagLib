truncate n3c_admin.ncats;
insert into n3c_admin.ncats 
select
	site_name,
	pi_poc_name,
	substring(dta_sent,'[0-9]+/[0-9]+/[0-9]+')::date,
	substring(dta_executed,'[0-9]+/[0-9]+/[0-9]+')::date,
	dua_sent::date,
	dua_executed::date,
	ctsa_non_ctsa
from n3c_admin.sheet1
where site_name is not null and site_name != 'Sent batch email to all the below' and site_name != 'Counts';

truncate n3c_admin.n3c;
insert into n3c_admin.n3c
select site,ctsa_community,groups_consortium___e_g___ny__ca__petal__etc__,person_to_make_contact,contact_made___type_yes_no__or_use_dropdown_menu_::boolean,pi_contact_at_site,pi_email,status_of_data_ingest__use_dropdown_menu_,status_of_data_ingest_date::date,date_script_sent_to_site::date,date_initial_data_received::date,null,null,notes__actions_issues_
from n3c_admin.hub___site_status;

truncate n3c_admin.acquisition;
insert into n3c_admin.acquisition(site_name,first_meeting,primary_technical_contact,data_model,passing_data)
select site_name,substring(date_of_first_meeting,'[0-9]+/[0-9]+/[0-9]+')::date,primary_technical_contact,data_model,passing_data_ 
from n3c_admin.site_status;

truncate n3c_admin.irb;
insert into n3c_admin.irb
 SELECT irb_status.institution,
    (irb_status.initial_contact_with_jhm)::date AS initial_contact,
    ("substring"(irb_status.lcq_finalized_submitted_to_eirb_for_review, '[0-9]+/[0-9]+/[0-9]+'::text))::date AS submitted,
    (irb_status.sirb_onboarding_approved)::date AS approved,
    (irb_status.date_of_local_irb_approval)::date AS local_approval,
    (irb_status.receipt_of_local_irb_approval_letter)::date AS local_receipt,
    (COALESCE(irb_status.sirb_onboarding_approved, irb_status.receipt_of_local_irb_approval_letter))::date AS cleared
   FROM n3c_admin.irb_status
  WHERE (irb_status.institution IS NOT NULL);

delete from n3c_admin.mapping_ncats
where n3c not in (select site from n3c_admin.n3c) or ncats not in (select site_name from n3c_admin.ncats);
