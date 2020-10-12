--
-- PostgreSQL database dump
--

--
-- Name: n3c_web; Type: SCHEMA; Schema: -; Owner: eichmann
--

CREATE SCHEMA n3c_web;

--
-- Name: bio; Type: TABLE; Schema: n3c_web; Owner: eichmann
--

CREATE TABLE n3c_web.bio (
    nid integer,
    vid integer,
    title text,
    body_value text,
    body_summary text,
    filename text,
    headshot_url text,
    headshot_alt text,
    headshot_title text,
    headshot_width integer,
    headshot_height integer,
    detailed_role text,
    webpage_url text,
    workstream_role text,
    created timestamp with time zone,
    changed timestamp with time zone
);


--
-- Name: bio_role; Type: TABLE; Schema: n3c_web; Owner: eichmann
--

CREATE TABLE n3c_web.bio_role (
    nid integer,
    vid integer,
    delta integer,
    role text
);


--
-- Name: bio_substream; Type: TABLE; Schema: n3c_web; Owner: eichmann
--

CREATE TABLE n3c_web.bio_substream (
    nid integer,
    vid integer,
    delta integer,
    substream text
);


--
-- Name: bio_workstream; Type: TABLE; Schema: n3c_web; Owner: eichmann
--

CREATE TABLE n3c_web.bio_workstream (
    nid integer,
    vid integer,
    delta integer,
    workstream text
);


--
-- Name: domain_team; Type: TABLE; Schema: n3c_web; Owner: eichmann
--

CREATE TABLE n3c_web.domain_team (
    nid integer,
    vid integer,
    title text,
    cross_cutting integer,
    description text,
    summary text,
    mailing_list_address text,
    mailing_list_url text,
    mailing_list_anchor text,
    gdrive_url text,
    gdrive_anchor text,
    slack_url text,
    slack_anchor text,
    created timestamp with time zone,
    changed timestamp with time zone
);


--
-- Name: domain_team_lead; Type: TABLE; Schema: n3c_web; Owner: eichmann
--

CREATE TABLE n3c_web.domain_team_lead (
    nid integer,
    vid integer,
    delta integer,
    lead_nid integer
);


--
-- Name: subgroup; Type: TABLE; Schema: n3c_web; Owner: eichmann
--

CREATE TABLE n3c_web.subgroup (
    nid integer,
    vid integer,
    title text,
    mission text,
    meeting_time text,
    leads text,
    join_docs text,
    gdrive_url text,
    gdrive_anchor text,
    workstream_nid integer,
    created timestamp with time zone,
    changed timestamp with time zone
);


--
-- Name: workstream; Type: TABLE; Schema: n3c_web; Owner: eichmann
--

CREATE TABLE n3c_web.workstream (
    nid integer,
    vid integer,
    title text,
    body_value text,
    body_summary text,
    mission text,
    github_url text,
    github_anchor text,
    onboard_url text,
    onboard_anchor text,
    ggroup_url text,
    ggroup_anchor text,
    slack_url text,
    slack_anchor text,
    zoom_url text,
    zoom_anchor text,
    video_url text,
    icon_url text,
    icon_alt_text text,
    icon_title text,
    icon_width integer,
    icon_height integer,
    created timestamp with time zone,
    changed timestamp with time zone
);


--
-- Name: workstream_person; Type: TABLE; Schema: n3c_web; Owner: eichmann
--

CREATE TABLE n3c_web.workstream_person (
    nid integer,
    vid integer,
    delta integer,
    person_nid integer
);


--
-- PostgreSQL database dump complete
--

select
    jsonb_pretty(json_agg(team)::jsonb)
from
    (select
        'https://covid.cd2h.org/node/'||nid as url,
        cross_cutting::boolean,
        title,
        substring(summary from '<p>(.*)</p.*') as summary,
        substring(description from '<h2>Mission</h2>[^<]*(.*)') as description,
        ( select jsonb_agg(leads)
        	from (select
        				delta,
        				'https://covid.cd2h.org/node/'||lead_nid as url,
        				bio.title as name,
        				substring(bio.body_value from '<p>(.*)</p.*') as institution
        			from domain_team_lead, bio
        			where domain_team_lead.nid = domain_team.nid
        			  and lead_nid = bio.nid
        			order by delta
        			) as leads
        	) as leads
     from domain_team
    ) as team
;
