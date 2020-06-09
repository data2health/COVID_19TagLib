
CREATE SCHEMA n3c_admin;

CREATE TABLE n3c_admin.n3c (
    site text,
    ctsa_community text,
    group_consortium text,
    contacting_person text,
    contact_made boolean,
    pi_contact text,
    pi_email text,
    data_ingest text,
    data_ingest_date date,
    date_script_sent date,
    initial_data_received date,
    last_data_received date,
    update_frequency interval,
    notes text
);


CREATE TABLE n3c_admin.ncats (
    site_name text,
    pi_poc_name text,
    dta_sent date,
    dta_executed date,
    dua_sent date,
    dua_executed date,
    ctsa_non_ctsa text
);


CREATE TABLE n3c_admin.irb (
    institution text,
    initial_contact date,
    submitted date,
    approved date,
    local_approval date,
    local_receipt date,
    cleared date
);


CREATE TABLE n3c_admin.acquisition (
    site_name text,
    first_meeting date,
    primary_technical_contact text,
    data_model text,
    passing_data text
);


CREATE TABLE n3c_admin.mapping_ncats (
    site text,
    city text,
    state text,
    n3c text,
    ncats text
);


CREATE TABLE n3c_admin.mapping_irb (
    site text,
    city text,
    state text,
    n3c text,
    irb text
);


CREATE TABLE n3c_admin.mapping_acquisition (
    site text,
    city text,
    state text,
    n3c text,
    acquisition text
);


CREATE VIEW n3c_admin.dashboard1 AS
 SELECT mapping_ncats.site,
    mapping_ncats.city,
    mapping_ncats.state,
    ncats.dta_sent,
    ncats.dta_executed,
    ncats.dua_sent,
    ncats.dua_executed,
    ncats.ctsa_non_ctsa,
    n3c.ctsa_community,
    n3c.group_consortium,
    n3c.contacting_person,
    n3c.contact_made,
    n3c.pi_contact,
    n3c.pi_email,
    n3c.data_ingest,
    n3c.data_ingest_date,
    n3c.date_script_sent,
    n3c.initial_data_received,
    n3c.last_data_received,
    n3c.update_frequency,
    n3c.notes
   FROM n3c_admin.mapping_ncats,
    n3c_admin.ncats,
    n3c_admin.n3c
  WHERE ((mapping_ncats.n3c = n3c.site) AND (mapping_ncats.ncats = ncats.site_name));


ALTER TABLE n3c_admin.dashboard1 OWNER TO eichmann;

CREATE VIEW n3c_admin.dashboard2 AS
 SELECT mapping_irb.site,
    mapping_irb.city,
    mapping_irb.state,
    irb.submitted,
    irb.approved,
    irb.local_approval,
    irb.local_receipt,
    irb.cleared
   FROM mapping_irb,
    irb,
    n3c
  WHERE mapping_irb.n3c = n3c.site AND mapping_irb.irb = irb.institution;

CREATE VIEW n3c_admin.dashboard3 AS
 SELECT mapping_acquisition.site,
    mapping_acquisition.city,
    mapping_acquisition.state,
    acquisition.first_meeting,
    acquisition.data_model,
    acquisition.passing_data
   FROM n3c_admin.mapping_acquisition,
    n3c_admin.n3c,
    n3c_admin.acquisition
  WHERE ((mapping_acquisition.n3c = n3c.site) AND (mapping_acquisition.acquisition = acquisition.site_name));


CREATE VIEW dashboard AS
select * from dashboard1 natural left join dashboard2 natural left join dashboard3;


--
-- personnel
--

create materialized view personnel as
select
	timestamp::timestamp as onboarded,
	email_address,
	first_name,
	last_name,
    google__g_suite_enabled_email__if_different_from_primary__pr as gmail,
    slack_email_if_different_from_primary__previously_entered__a as slack,
    github_handle as github,
    case when substring(orcid_id from '[0-9]+-[0-9]+-[0-9]+-[0-9X]+') = '0000-0000-0000-0000' then null else substring(orcid_id from '[0-9]+-[0-9]+-[0-9]+-[0-9X]+') end as orcid,
    assistant_email,
    institutional_affiliation__ctsa__or_employer__url_,
    root_domain__calculated_,
    institution_lookup,
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
from newformmaster
where email_address is not null
;

--
-- old stuff
--


CREATE VIEW n3c_admin.irb_temp AS
 SELECT irb_status.institution,
    (irb_status.initial_contact_with_jhm)::date AS initial_contact,
    ("substring"(irb_status.lcq_finalized_submitted_to_eirb_for_review, '[0-9]+/[0-9]+/[0-9]+'::text))::date AS submitted,
    (irb_status.sirb_onboarding_approved)::date AS approved,
    (irb_status.date_of_local_irb_approval)::date AS local_approval,
    (irb_status.receipt_of_local_irb_approval_letter)::date AS local_receipt,
    (COALESCE(irb_status.sirb_onboarding_approved, irb_status.receipt_of_local_irb_approval_letter))::date AS cleared
   FROM n3c_admin.irb_status
  WHERE (irb_status.institution IS NOT NULL);


CREATE TABLE n3c_admin.n3c_v1 (
    site text,
    case_count integer,
    data_network text,
    target_reason text,
    wave text
);

