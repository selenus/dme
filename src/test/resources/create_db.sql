--
-- Initial database setup script for the DARIAH-DE federation metamodel
--
-- 20150313 - TGR: Created script
-- 20150316 - TGR: Added element

--
-- Grammars and functions
--
CREATE SEQUENCE description_grammar_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE TABLE description_grammar (
  id bigint NOT NULL,
  element_id bigint NOT NULL,
  base_method character varying(255) NOT NULL,
  grammar_name character varying(255) NOT NULL,
  CONSTRAINT pkey_description_grammar PRIMARY KEY (id)
);
CREATE SEQUENCE transformation_function_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE TABLE transformation_function (
  id bigint NOT NULL,
  description_grammar_id bigint NOT NULL,
  function_statement text NOT NULL,
  CONSTRAINT pkey_transformation_function PRIMARY KEY (id)
);
CREATE TABLE transformation_function_external_element (
  transformation_function_id bigint NOT NULL,
  element_id bigint NOT NULL,
  CONSTRAINT pkey_transformation_function_external_element PRIMARY KEY (transformation_function_id, element_id)
);
CREATE TABLE grammar_container (
  description_grammar_id bigint NOT NULL,
  lexer_grammar text,
  parser_grammar text,
  CONSTRAINT pkey_grammar_container PRIMARY KEY (description_grammar_id)
);
--
-- Nonterminals
--
CREATE SEQUENCE element_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE TABLE element (
  id bigint NOT NULL,
  name character varying(255) NOT NULL,
  transient boolean default false,
  CONSTRAINT pkey_element PRIMARY KEY (id)
);
CREATE TABLE nonterminal (
  id bigint NOT NULL,
  parent_nonterminal_id bigint,
  terminal_id bigint,
  CONSTRAINT pkey_nonterminal PRIMARY KEY (id)
);
CREATE TABLE label (
  id bigint NOT NULL,
  parent_label_id bigint,
  transformation_function_id bigint NOT NULL,
  CONSTRAINT pkey_label PRIMARY KEY (id)
);
--
-- Terminals
--
CREATE SEQUENCE terminal_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE TABLE terminal (
  id bigint NOT NULL,
  name character varying(255) NOT NULL,
  CONSTRAINT pkey_terminal PRIMARY KEY (id)
);
CREATE TABLE xml_terminal (
  id bigint NOT NULL,
  namespace_prefix character varying(255) NOT NULL,
  is_attribute boolean default false,
  schema_id bigint NOT NULL,
  CONSTRAINT pkey_xml_terminal PRIMARY KEY (id)
);
--
-- Schema
--
CREATE SEQUENCE schema_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE TABLE schema (
  id bigint NOT NULL,
  root_nonterminal_id bigint,
  label character varying(255) NOT NULL,
  uuid character varying(255) NOT NULL,
  CONSTRAINT pkey_schema PRIMARY KEY (id)
);
CREATE TABLE xml_schema (
  id bigint NOT NULL,
  external_label character varying(255) NOT NULL,
  record_path character varying(255),
  CONSTRAINT pkey_xml_schema PRIMARY KEY (id)
);

CREATE SEQUENCE xml_namespace_id_seq INCREMENT 1 MINVALUE 1 START 1;
CREATE TABLE xml_namespace (
  id bigint NOT NULL,
  schema_id bigint NOT NULL,
  prefix character varying(255) NOT NULL,
  url character varying(255) NOT NULL,
  CONSTRAINT pkey_xml_namespace PRIMARY KEY (id)
);
--
-- Constraints
-- 
ALTER TABLE transformation_function 
	ADD CONSTRAINT fkey_transformation_function_description_grammar FOREIGN KEY (description_grammar_id) REFERENCES description_grammar (id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE transformation_function_external_element 
	ADD CONSTRAINT fkey_transformation_function_external_element_function FOREIGN KEY (transformation_function_id) REFERENCES transformation_function (id) ON UPDATE CASCADE ON DELETE CASCADE,
	ADD CONSTRAINT fkey_transformation_function_external_element_element FOREIGN KEY (element_id) REFERENCES element (id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE grammar_container
	ADD CONSTRAINT fkey_grammar_container_description_grammar FOREIGN KEY (description_grammar_id) REFERENCES description_grammar (id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE nonterminal
	ADD CONSTRAINT fkey_nonterminal_element FOREIGN KEY (id) REFERENCES element (id) ON UPDATE CASCADE ON DELETE CASCADE,
	ADD CONSTRAINT fkey_nonterminal_nonterminal FOREIGN KEY (parent_nonterminal_id) REFERENCES nonterminal (id) ON UPDATE CASCADE ON DELETE CASCADE,
	ADD CONSTRAINT fkey_nonterminal_terminal FOREIGN KEY (terminal_id) REFERENCES terminal (id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE label
	ADD CONSTRAINT fkey_label_element FOREIGN KEY (id) REFERENCES element (id) ON UPDATE CASCADE ON DELETE CASCADE,
	ADD CONSTRAINT fkey_label_label FOREIGN KEY (parent_label_id) REFERENCES label (id) ON UPDATE CASCADE ON DELETE CASCADE,
	ADD CONSTRAINT fkey_label_transformation_function FOREIGN KEY (transformation_function_id) REFERENCES transformation_function (id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE schema
	ADD CONSTRAINT fkey_schema_nonterminal_root FOREIGN KEY (root_nonterminal_id) REFERENCES nonterminal (id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE xml_schema
	ADD CONSTRAINT fkey_xml_schema_schema FOREIGN KEY (id) REFERENCES schema (id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE xml_namespace
	ADD CONSTRAINT fkey_xml_namespace_xml_schema FOREIGN KEY (schema_id) REFERENCES schema (id) ON UPDATE CASCADE ON DELETE CASCADE,
	ADD CONSTRAINT idx_xml_namespace_schema_id_prefix UNIQUE (schema_id, prefix);
ALTER TABLE xml_terminal
	ADD CONSTRAINT fkey_xml_terminal_terminal FOREIGN KEY (id) REFERENCES terminal (id) ON UPDATE CASCADE ON DELETE CASCADE,
	ADD CONSTRAINT fkey_xml_terminal_xml_schema FOREIGN KEY (schema_id) REFERENCES xml_schema (id) ON UPDATE CASCADE ON DELETE CASCADE,
	ADD CONSTRAINT fkey_xml_terminal_xml_namespace FOREIGN KEY (schema_id, namespace_prefix) REFERENCES xml_namespace (schema_id, prefix) ON UPDATE CASCADE ON DELETE CASCADE;
--
--	Version
--
CREATE TABLE db_version_history (
  category character varying(100) NOT NULL,
  version character varying(5) NOT NULL,
  updated timestamp with time zone
);
INSERT INTO db_version_history(category, version, updated) VALUES ('metamodel', '1.0.0', NOW());
