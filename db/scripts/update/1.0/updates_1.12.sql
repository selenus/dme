CREATE SEQUENCE schema_element_analyzer_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE TABLE schema_element_analyzer (
   id bigint NOT NULL DEFAULT nextval('schema_element_analyzer_id_seq'::regclass), 
   schema_element_id bigint,
   analyzer character varying(255), 
   CONSTRAINT pkey_schema_element_analyzer PRIMARY KEY (id)
);

ALTER TABLE attribute
   ADD COLUMN process_source_links boolean DEFAULT false;
   
ALTER TABLE attribute
   ADD COLUMN process_geo_entities boolean DEFAULT false;

ALTER TABLE attribute
   ADD COLUMN use_for_title boolean DEFAULT false;
   
ALTER TABLE attribute
   ADD COLUMN use_for_topic_modelling boolean DEFAULT false;
   
   
ALTER TABLE containment
   ADD COLUMN process_source_links boolean DEFAULT false;

ALTER TABLE containment
   ADD COLUMN process_geo_entities boolean DEFAULT false;
   
ALTER TABLE containment
   ADD COLUMN use_for_title boolean DEFAULT false;
   
ALTER TABLE containment
   ADD COLUMN use_for_topic_modelling boolean DEFAULT false;
   
   
CREATE SEQUENCE roles_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE SEQUENCE roles_role_mapping_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE SEQUENCE users_id_seq INCREMENT 1 MINVALUE 1 START 1;
   
INSERT INTO db_version_history(version, updated) VALUES ('1.12', NOW());