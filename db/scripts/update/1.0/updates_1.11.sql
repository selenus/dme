ALTER TABLE schema ADD COLUMN uuid character(36);


ALTER TABLE attribute
  ADD COLUMN ns_uri character varying(1024);

ALTER TABLE containment
  ADD COLUMN ns_uri character varying(1024);
  
CREATE SEQUENCE namespace_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE TABLE namespace (
  id bigint NOT NULL DEFAULT nextval('namespace_id_seq'::regclass),
  prefix character varying(25),
  uri character varying(1024),
  schema_id bigint NOT NULL,
  CONSTRAINT pkey_namespace PRIMARY KEY (id)
);

ALTER TABLE namespace
  ADD CONSTRAINT fkey_namespace_schema FOREIGN KEY (schema_id)
      REFERENCES schema (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;
      
INSERT INTO db_version_history(version, updated) VALUES ('1.11', NOW());