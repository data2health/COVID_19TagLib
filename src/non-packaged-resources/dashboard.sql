
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
    n3c.data_ingest_date,
    n3c.initial_data_received
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

