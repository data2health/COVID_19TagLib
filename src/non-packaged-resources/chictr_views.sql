create materialized view study as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/main'
    passing raw
    columns
        utrn text path 'UTRN/text()',
        reg_name text path 'reg_name/text()',
        date_registration text path 'date_registration/text()',
        primary_sponsor text path 'primary_sponsor/text()',
        public_title text path 'public_title/text()',
        acronym text path 'acronym/text()',
        scientific_title text path 'scientific_title/text()',
        Scientific_acronym text path 'Scientific_acronym/text()',
        date_enrolment text path 'date_enrolment/text()',
        type_enrolment text path 'type_enrolment/text()',
        target_size text path 'target_size/text()',
        recruitment_status text path 'recruitment_status/text()',
        url text path 'url/text()',
        study_type text path 'study_type/text()',
        study_design text path 'study_design/text()',
        phase text path 'phase/text()',
        hc_freetext text path 'hc_freetext/text()',
        i_freetext text path 'i_freetext/text()',
        results_actual_enrolment text path 'results_actual_enrolment/text()',
        results_date_completed text path 'results_date_completed/text()',
        results_url_link text path 'results_url_link/text()',
        results_summary text path 'results_summary/text()',
        results_date_posted text path 'results_date_posted/text()',
        results_date_first_publication text path 'results_date_first_publication/text()',
        results_baseline_char text path 'results_baseline_char/text()',
        results_participant_flow text path 'results_participant_flow/text()',
        results_adverse_events text path 'results_adverse_events/text()',
        results_outcome_measures text path 'results_outcome_measures/text()',
        results_url_protocol text path 'results_url_protocol/text()',
        results_IPD_plan text path 'results_IPD_plan/text()',
        results_IPD_description text path 'results_IPD_description/text()'
        );

create materialized view contact as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/contacts/contact'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	type text path 'type/text()',
    	firstname text path 'firstname/text()',
    	middlename text path 'middlename/text()',
    	lastname text path 'lastname/text()',
    	address text path 'address/text()',
    	city text path 'city/text()',
    	country1 text path 'country1/text()',
    	zip text path 'zip/text()',
    	telephone text path 'telephone/text()',
    	email text path 'email/text()',
    	affiliation text path 'affiliation/text()'
    	)
where type is not null;


create materialized view country as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/countries/country2'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	country text path 'text()'
    	)
where country is not null;

create materialized view criteria as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/criteria'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	inclusion_criteria text path 'inclusion_criteria/text()',
    	agemin text path 'agemin/text()',
    	agemax text path 'agemax/text()',
    	gender text path 'gender/text()',
    	exclusion_criteria text path 'exclusion_criteria/text()'
    	)
where inclusion_criteria is not null;

create materialized view health_condition_code as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/health_condition_code/hc_code'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	hc_code text path 'text()'
    	)
where hc_code is not null;

create materialized view health_condition_keyword as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/health_condition_keyword/hc_keyword'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	hc_keyword text path 'text()'
    	)
where hc_keyword is not null;

create materialized view intervention_code as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/intervention_code/i_code'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	i_code text path 'text()'
    	)
where i_code is not null;

create materialized view intervention_keyword as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/intervention_keyword/i_keyword'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	i_keyword text path 'text()'
    	)
where i_keyword is not null;

create materialized view primary_outcome as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/primary_outcome/prim_outcome'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	prim_outcome text path 'text()'
    	)
where prim_outcome is not null;

create materialized view secondary_outcome as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/secondary_outcome/sec_outcome'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	sec_outcome text path 'text()'
    	)
where sec_outcome is not null;

create materialized view secondary_sponsor as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/secondary_sponsor/sponsor_name'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	sponsor_name text path 'text()'
    	)
where sponsor_name is not null;

create materialized view secondary_id as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/secondary_ids/secondary_id'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	sec_id text path 'sec_id/text()',
    	issuing_authority text path 'issuing_authority/text()'
    	)
where sec_id is not null;

create materialized view source_support as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/source_support/source_name'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	source_support text path 'text()'
    	)
where source_support is not null;

create materialized view ethics_review as
select id,xmltable.* from 
raw,
xmltable(
    '/trials/Triall/ethics_reviews/ethics_review'
    passing raw
    columns
        seqnum FOR ORDINALITY,
    	status text path 'status/text()',
    	approval_date text path 'approval_date/text()',
    	contact_name text path 'contact_name/text()',
    	contact_address text path 'contact_address/text()',
    	contact_phone text path 'contact_phone/text()',
    	contact_email text path 'contact_email/text()'
    	)
where status is not null;
