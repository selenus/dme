-- Version 1.9
-- User-Action Log on BaseEntity replaces old author or creator fields
ALTER TABLE "schema" DROP COLUMN author;
ALTER TABLE files DROP COLUMN creator;
ALTER TABLE "mapping" DROP COLUMN author;

CREATE SEQUENCE user_annotation_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17315
  CACHE 1;

CREATE TABLE user_annotation (
  id bigint NOT NULL DEFAULT nextval('user_annotation_id_seq'::regclass),
  parent_id bigint,
  user_id bigint NOT NULL,
  annotated_object_type character varying(1024) NOT NULL,
  annotated_object_id bigint NOT NULL,
  aggregator_object_type character varying(1024) NOT NULL,
  aggregator_object_id bigint NOT NULL,
  action_type smallint NOT NULL,
  object_snapshot bytea,
  "comment" text,
  created timestamp with time zone NOT NULL,
  CONSTRAINT pkey_user_annotation PRIMARY KEY (id),
  CONSTRAINT fkey_comment_user FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE RESTRICT
);

INSERT INTO db_version_history(id, version, updated) VALUES (10, '1.9', NOW());
-- End Update (Version 1.9)