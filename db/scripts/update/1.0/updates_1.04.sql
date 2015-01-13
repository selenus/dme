-- Version 1.4
-- Tables for file handling introduced
CREATE TABLE file_types
(
  id bigint NOT NULL,
  name character varying(255),
  description text,
  modified timestamp with time zone,
  created timestamp with time zone,
  CONSTRAINT file_types_pkey PRIMARY KEY (id)
);
CREATE TABLE files
(
  id bigint NOT NULL,
  filename character varying(255),
  filestream bytea,
  filetype bigint,
  validated boolean,
  creator character varying(255),
  modified timestamp with time zone,
  created timestamp with time zone,
  CONSTRAINT files_pkey PRIMARY KEY (id),
  CONSTRAINT files_file_types_fkey FOREIGN KEY (filetype)
      REFERENCES file_types (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
INSERT INTO file_types(id, name, description) VALUES (1, 'XML Schema', 'XML Schema');
INSERT INTO db_version_history(id, version, updated) VALUES (5, '1.4', NOW());
-- End Update (Version 1.4)