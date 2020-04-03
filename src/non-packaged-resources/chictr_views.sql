create view article as
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
