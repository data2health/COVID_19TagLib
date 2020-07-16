CREATE SCHEMA translator_kg;

CREATE TABLE translator_kg.edge (
    subject text,
    object text,
    relation text,
    edge_label text,
    provided_by text,
    combined_score text,
    neighborhood text,
    neighborhood_transferred text,
    fusion text,
    cooccurence text,
    homology text,
    coexpression text,
    coexpression_transferred text,
    experiments text,
    experiments_transferred text,
    database text,
    database_transferred text,
    textmining text,
    textmining_transferred text,
    comment text,
    publication text,
    num_participants text,
    association_type text,
    detection_method text,
    subj_exp_role text,
    obj_exp_role text,
    evidence text,
    target_type text,
    db_references text,
    eco_code text,
    kg_with text,
    kg_date text,
    assigned_by text,
    annotation_properties text
);

update edge set subject = null where trim(subject) = '';
update edge set object = null where trim(object) = '';
update edge set relation = null where trim(relation) = '';
update edge set edge_label = null where trim(edge_label) = '';
update edge set provided_by = null where trim(provided_by) = '';
update edge set combined_score = null where trim(combined_score) = '';
update edge set neighborhood = null where trim(neighborhood) = '';
update edge set neighborhood_transferred = null where trim(neighborhood_transferred) = '';
update edge set fusion = null where trim(fusion) = '';
update edge set cooccurence = null where trim(cooccurence) = '';
update edge set homology = null where trim(homology) = '';
update edge set coexpression = null where trim(coexpression) = '';
update edge set coexpression_transferred = null where trim(coexpression_transferred) = '';
update edge set experiments = null where trim(experiments) = '';
update edge set experiments_transferred = null where trim(experiments_transferred) = '';
update edge set database = null where trim(database) = '';
update edge set database_transferred = null where trim(database_transferred) = '';
update edge set textmining = null where trim(textmining) = '';
update edge set textmining_transferred = null where trim(textmining_transferred) = '';
update edge set comment = null where trim(comment) = '';
update edge set publication = null where trim(publication) = '';
update edge set num_participants = null where trim(num_participants) = '';
update edge set association_type = null where trim(association_type) = '';
update edge set detection_method = null where trim(detection_method) = '';
update edge set subj_exp_role = null where trim(subj_exp_role) = '';
update edge set obj_exp_role = null where trim(obj_exp_role) = '';
update edge set evidence = null where trim(evidence) = '';
update edge set target_type = null where trim(target_type) = '';
update edge set db_references = null where trim(db_references) = '';
update edge set eco_code = null where trim(eco_code) = '';
update edge set kg_with = null where trim(kg_with) = '';
update edge set kg_date = null where trim(kg_date) = '';
update edge set assigned_by = null where trim(assigned_by) = '';
update edge set annotation_properties = null where trim(annotation_properties) = '';

alter table edge alter combined_score type int using combined_score::int;
alter table edge alter neighborhood type int using neighborhood::int;
alter table edge alter neighborhood_transferred type int using neighborhood_transferred::int;
alter table edge alter fusion type int using fusion::int;
alter table edge alter cooccurence type int using cooccurence::int;
alter table edge alter homology type int using homology::int;
alter table edge alter coexpression type int using coexpression::int;
alter table edge alter coexpression_transferred type int using coexpression_transferred::int;
alter table edge alter experiments type int using experiments::int;
alter table edge alter experiments_transferred type int using experiments_transferred::int;
alter table edge alter database type int using database::int;
alter table edge alter database_transferred type int using database_transferred::int;
alter table edge alter textmining type int using textmining::int;
alter table edge alter textmining_transferred type int using textmining_transferred::int;
alter table edge alter num_participants type int using num_participants::int;

CREATE TABLE translator_kg.node (
    id text,
    name text,
    category text,
    description text,
    alias text,
    provided_by text,
    xrefs text,
    tdl text,
    ncbi_taxid text,
    ttd_id text,
    iri text,
    synonym text,
    full_name text,
    in_taxon text
);

update node set name = null where trim(name) = '';
update node set category = null where trim(category) = '';
update node set description = null where trim(description) = '';
update node set alias = null where trim(alias) = '';
update node set provided_by = null where trim(provided_by) = '';
update node set xrefs = null where trim(xrefs) = '';
update node set tdl = null where trim(tdl) = '';
update node set ncbi_taxid = null where trim(ncbi_taxid) = '';
update node set ttd_id = null where trim(ttd_id) = '';
update node set iri = null where trim(iri) = '';
update node set synonym = null where trim(synonym) = '';
update node set full_name = null where trim(full_name) = '';
update node set in_taxon = null where trim(in_taxon) = '';
