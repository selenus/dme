CREATE TABLE alias (
	id bigint NOT NULL,
	name varchar(100),
	description varchar(4096),
	element_id integer NOT NULL,
	schema_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE annotation (
	element_id integer NOT NULL,
	group_id integer,
	attribute varchar(50) NOT NULL,
	value varchar(4096),
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE attribute (
	id bigint NOT NULL,
	name varchar(100) NOT NULL,
	description varchar(4096),
	entity_id integer NOT NULL,
	domain_id integer NOT NULL,
	"min" integer,
	"max" integer,
	"key" character NOT NULL,
	schema_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE containment (
	id bigint NOT NULL,
    name varchar(100),
    description varchar(4096),
	parent_id integer,
	child_id integer NOT NULL,
    "min" integer,
    "max" integer,
	schema_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE data_source (
    id bigint NOT NULL,
    name varchar(100) NOT NULL,
    description varchar(4096),
    url varchar(200) NOT NULL,
    schema_id integer NOT NULL,
    element_id integer,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE db_version_history
(
  id bigint NOT NULL,
  version varchar,
  updated timestamp with time zone
);

CREATE TABLE "domain" (
    id bigint NOT NULL,
    name varchar(100) NOT NULL,
    description varchar(4096),
	schema_id integer,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE domainvalue (
    id bigint NOT NULL,
    value varchar(100) NOT NULL,
    description varchar(4096),
    domain_id integer NOT NULL,
    schema_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE entity (
    id bigint NOT NULL,
    name varchar(100) NOT NULL,
    description varchar(4096),
    schema_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE extensions (
    schema_id integer NOT NULL,
    base_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE tags (
    id bigint NOT NULL,
    name varchar(100) NOT NULL,
    parent_id integer,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE persistent_logins (
  username varchar(64) DEFAULT NULL,
  series varchar(64) DEFAULT NULL,
  token varchar(64) DEFAULT NULL,
  last_used timestamp with time zone DEFAULT NULL
);

CREATE TABLE project (
    id bigint NOT NULL,
    name varchar(100),
    author varchar(100),
    description varchar(4096),
    vocabulary_id integer,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE data_type (
	id bigint NOT NULL,
	type varchar(30) NOT NULL,
	description varchar(500),
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE functions (
	id bigint NOT NULL,
	name varchar(50) NOT NULL,
	description varchar(4096),
	expression varchar(200),
	category varchar(100),
	output_type integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE function_input (
    function_id integer NOT NULL,
    input_type integer NOT NULL,
    input_loc integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE function_implementation (
	function_id integer NOT NULL,
	language varchar(50) NOT NULL,
	dialect varchar(50),
	implementation varchar(500) NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE log_entry (
  id bigint NOT NULL,
  "level" varchar,
  message text,
  classname varchar(255),
  filename varchar(255),
  linenumber varchar,
  loggername varchar,
  methodname varchar,
  threadname varchar,
  stacktrace text,
  logTimestamp timestamp with time zone,
  startTimestamp timestamp with time zone
);

CREATE TABLE user_log (
  id bigint NOT NULL,
  authenticated boolean,
  remoteaddress varchar(255),
  sessionid varchar(255),
  username varchar(255),
  dn varchar(255),
  exception text,
  loggedin timestamp with time zone,
  loggedout timestamp with time zone
);

CREATE TABLE mapping (
    id bigint NOT NULL,
    name varchar(100) NOT NULL,
	author varchar(100) NOT NULL,
    project_id integer NOT NULL,
    source_id integer NOT NULL,
    target_id integer NOT NULL,
    description varchar(4096),
    message varchar(4096),
    is_locked boolean NOT NULL DEFAULT false,
	state integer NOT NULL DEFAULT 0,
    modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE mapping_cell (
	id bigint NOT NULL,
	mapping_id integer NOT NULL,
	input_ids varchar(200) NOT NULL,
	output_id integer NOT NULL,
	score numeric(6,3) NOT NULL,
	function_id integer,
	author varchar(400),
	modification_date date,
	notes varchar(4096),
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE project_schema (
    project_id integer NOT NULL,
    schema_id integer NOT NULL,
    model varchar(256),
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE relationship (
    id bigint NOT NULL,
    name varchar(100) NOT NULL,
    description varchar(4096),
    left_id integer NOT NULL,
    left_min integer,
    left_max integer,
    right_id integer NOT NULL,
    right_min integer,
    right_max integer,
    schema_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE "schema" (
    id bigint NOT NULL,
    name varchar(100) NOT NULL,
    author varchar(100),
    source varchar(200),
    "type" varchar(100),
    description varchar(4096),
    message varchar(4096),
    locked character NOT NULL DEFAULT 'f',
    is_locked boolean NOT NULL DEFAULT false,
    state integer NOT NULL DEFAULT 0,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE schema_tag (
    tag_id integer NOT NULL,
    schema_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE subtype (
    id bigint NOT NULL,
    parent_id integer NOT NULL,
    child_id integer NOT NULL,
    schema_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
);

CREATE TABLE synonym (
	id bigint NOT NULL,
	name varchar(100) NOT NULL,
	description varchar(4096),
	element_id integer NOT NULL,
	schema_id integer NOT NULL,
	modified timestamp with time zone,
	created timestamp with time zone
); 

CREATE TABLE universal_id (
	sequence_name varchar(100) NOT NULL,
	id bigint NOT NULL
);

CREATE TABLE version (
	id bigint NOT NULL
);

ALTER TABLE alias
    ADD CONSTRAINT alias_pkey PRIMARY KEY (id);

ALTER TABLE attribute
    ADD CONSTRAINT attribute_pkey PRIMARY KEY (id);

ALTER TABLE containment
    ADD CONSTRAINT containment_pkey PRIMARY KEY (id);
    
ALTER TABLE db_version_history
    ADD CONSTRAINT db_version_history_pkey PRIMARY KEY (id); 
    
ALTER TABLE "domain"
    ADD CONSTRAINT domain_pkey PRIMARY KEY (id);

ALTER TABLE domainvalue
    ADD CONSTRAINT domainvalue_pkey PRIMARY KEY (id);

ALTER TABLE entity
    ADD CONSTRAINT entity_pkey PRIMARY KEY (id);

ALTER TABLE tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);

ALTER TABLE data_source
    ADD CONSTRAINT instance_pkey PRIMARY KEY (id);

ALTER TABLE mapping_cell
    ADD CONSTRAINT mappingcell_pkey PRIMARY KEY (id);

ALTER TABLE project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);

ALTER TABLE mapping
    ADD CONSTRAINT mapping_pkey PRIMARY KEY (id);

ALTER TABLE relationship
    ADD CONSTRAINT relationship_pkey PRIMARY KEY (id);

ALTER TABLE "schema"
    ADD CONSTRAINT schema_pkey PRIMARY KEY (id);

ALTER TABLE synonym
    ADD CONSTRAINT synonym_pkey PRIMARY KEY (id);

ALTER TABLE data_type
    ADD CONSTRAINT datatype_pkey PRIMARY KEY (id);

ALTER TABLE functions
    ADD CONSTRAINT function_pkey PRIMARY KEY (id);

ALTER TABLE log_entry 
	ADD CONSTRAINT log_entry_pkey PRIMARY KEY (id);
	
ALTER TABLE user_log 
	ADD CONSTRAINT user_log_pkey PRIMARY KEY (id);


ALTER TABLE extensions
    ADD CONSTRAINT extensions_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE extensions
    ADD CONSTRAINT extensions_base_fkey FOREIGN KEY (base_id) REFERENCES "schema"(id);

ALTER TABLE attribute
    ADD CONSTRAINT attribute_entity_fkey FOREIGN KEY (entity_id) REFERENCES entity(id);

ALTER TABLE attribute
    ADD CONSTRAINT attribute_domain_fkey FOREIGN KEY (domain_id) REFERENCES "domain"(id);

ALTER TABLE domainvalue
    ADD CONSTRAINT domainvalue_domain_fkey FOREIGN KEY (domain_id) REFERENCES "domain"(id);

ALTER TABLE relationship
    ADD CONSTRAINT relationship_leftentity_fkey FOREIGN KEY (left_id) REFERENCES entity(id);

ALTER TABLE relationship
    ADD CONSTRAINT relationship_rightentity_fkey FOREIGN KEY (right_id) REFERENCES entity(id);

ALTER TABLE data_source
    ADD CONSTRAINT datasource_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE tags
    ADD CONSTRAINT tags_tags_fkey FOREIGN KEY (parent_id) REFERENCES tags(id);

ALTER TABLE schema_tag
    ADD CONSTRAINT schematag_tags_fkey FOREIGN KEY (tag_id) REFERENCES tags(id);

ALTER TABLE schema_tag
    ADD CONSTRAINT schematag_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE mapping_cell
    ADD CONSTRAINT mappingcell_mapping_id_fkey FOREIGN KEY (mapping_id) REFERENCES mapping(id);

ALTER TABLE mapping_cell
	ADD CONSTRAINT mappingcell_function_id_fkey FOREIGN KEY (function_id) REFERENCES functions(id);

ALTER TABLE project
    ADD CONSTRAINT project_schema_fkey FOREIGN KEY (vocabulary_id) REFERENCES "schema"(id);

ALTER TABLE project_schema
    ADD CONSTRAINT projectschema_project_fkey FOREIGN KEY (project_id) REFERENCES project(id);

ALTER TABLE project_schema
    ADD CONSTRAINT projectschema_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE alias
    ADD CONSTRAINT alias_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE attribute
    ADD CONSTRAINT attribute_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE containment
	ADD CONSTRAINT containment_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE "domain"
    ADD CONSTRAINT domain_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE domainvalue
    ADD CONSTRAINT domainvalue_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE entity
    ADD CONSTRAINT entity_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE relationship
    ADD CONSTRAINT relationship_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE subtype
    ADD CONSTRAINT subtype_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE synonym
    ADD CONSTRAINT synonym_schema_fkey FOREIGN KEY (schema_id) REFERENCES "schema"(id);

ALTER TABLE functions
    ADD CONSTRAINT function_output_fkey FOREIGN KEY (output_type) REFERENCES data_type(id);

ALTER TABLE function_input
    ADD CONSTRAINT functioninput_function_id_fkey FOREIGN KEY (function_id) REFERENCES functions(id);

ALTER TABLE function_input
    ADD CONSTRAINT functioninput_input_fkey FOREIGN KEY (input_type) REFERENCES data_type(id);

ALTER TABLE function_implementation
    ADD CONSTRAINT functionimp_function_id_fkey FOREIGN KEY (function_id) REFERENCES functions(id);



CREATE UNIQUE INDEX schema_tag_idx ON schema_tag (schema_id, tag_id);

CREATE INDEX alias_schema_idx ON alias (schema_id);

CREATE INDEX attribute_schema_idx ON attribute (schema_id);

CREATE INDEX containment_schema_idx ON containment (schema_id);

CREATE INDEX domain_schema_idx ON "domain" (schema_id);

CREATE INDEX domainvalue_schema_idx ON domainvalue (schema_id);

CREATE INDEX entity_schema_idx ON entity (schema_id);

CREATE INDEX relationship_schema_idx ON relationship (schema_id);

CREATE INDEX subtype_schema_idx ON subtype (schema_id);

CREATE INDEX synonym_schema_idx ON synonym (schema_id);

CREATE INDEX mappingcell_function_idx ON mapping_cell (function_id);

CREATE UNIQUE INDEX function_name_idx ON functions (name);

CREATE UNIQUE INDEX function_implementation_idx ON function_implementation (function_id, language, dialect);

CREATE INDEX annotation_element_idx ON annotation(element_id);

CREATE INDEX annotation_group_idx ON annotation(group_id);



CREATE VIEW schema_elements (id,name,description,"type",schema_id) AS
  SELECT id,name,'','alias',schema_id FROM alias
    UNION ALL
  SELECT id,name,description,'attribute',schema_id FROM attribute
    UNION ALL
  SELECT id,name,description,'containment',schema_id FROM containment
    UNION ALL
  SELECT id,name,description,'domain',schema_id FROM "domain"
    UNION ALL
  SELECT id,value,description,'domainvalue',schema_id FROM domainvalue
    UNION ALL
  SELECT id,name,description,'entity',schema_id FROM entity
    UNION ALL
  SELECT id,name,'','relationship',schema_id FROM relationship
    UNION ALL
  SELECT id,'','','subtype',schema_id FROM subtype;



INSERT INTO "domain"(id, "name", description, schema_id)
    VALUES (-1, 'Integer', 'Domain covering all integer values', NULL),
    (-2, 'Real', 'Domain covering all real values', NULL),
    (-3, 'String', 'Domain covering all strings', NULL),
    (-4, 'Timestamp', 'Domain consisting of date and/or time values', NULL),
    (-5, 'Boolean', 'Domain covering all boolean values', NULL),
    (-6, 'Any', 'Domain covering all values', NULL);
    
INSERT INTO data_type (id, type, description)
VALUES(440, 'Any', 'Any type of input'),
(441, 'Number', 'An input formatted as a number'),
(442, 'String', 'An input formatted as a string'),
(443, 'DateTime', 'An input formatted as a date or time');

INSERT INTO functions (id, name, description, expression, category, output_type)
VALUES(450, 'identity', 'Passes the value through un-changed', null, 'Basic', 440),
(460, 'abs', 'Returns the absolute value of the input', null, 'Math', 441),
(461, 'floor', 'Returns the next smallest integer', null, 'Math', 441),
(462, 'ceiling', 'Returns the next largest integer', null, 'Math', 441),
(463, 'round', 'Returns the nearest integer', null, 'Math', 441),
(464, 'min', 'Returns the smallest value from all tuples in the table', null, 'Math', 441),
(465, 'max', 'Returns the largest value from all tuples in the table', null, 'Math', 441),
(466, 'sum', 'Returns the sum of all tuples in the table', null, 'Math', 441),
(467, 'avg', 'Returns the average of all tuples in the table', null, 'Math', 441),
(468, 'add', 'Numerical addition of two numbers', null, 'Math', 441),
(469, 'sub', 'Numerical subtraction of two numbers', null, 'Math', 441),
(470, 'mult', 'Numerical multiplication of two numbers', null, 'Math', 441),
(480, 'concat', 'Takes in two fields and concatenates them together', null, 'String', 442),
(481, 'lower', 'Returns the value with all letters in lower case', null, 'String', 442),
(482, 'upper', 'Returns the value with all letters in upper case', null, 'String', 442),
(483, 'trim', 'Returns the value with all leading and trailing whitespace removed', null, 'String', 442),
(484, 'ltrim', 'Returns the value with all leading whitespace removed', null, 'String', 442),
(485, 'rtrim', 'Returns the value with all trailing whitespace removed', null, 'String', 442),
(486, 'length', 'Returns the length of the value', null, 'String', 441),
(490, 'year', 'Extracts the year from the date field', null, 'Date', 441),
(491, 'month', 'Extracts the year from the date field', null, 'Date', 441),
(492, 'day', 'Extracts the month from the date field', null, 'Date', 441),
(493, 'hour', 'Extracts the day from the date field', null, 'Date', 441),
(494, 'minute', 'Extracts the minute from the date field', null, 'Date', 441),
(495, 'second', 'Extracts the second from the date field', null, 'Date', 441);

INSERT INTO function_input (function_id, input_type, input_loc)
VALUES(450, 440, 1),
(460, 441, 1),
(461, 441, 1),
(462, 441, 1),
(463, 441, 1),
(464, 441, 1),
(465, 441, 1),
(466, 441, 1),
(467, 441, 1),
(468, 441, 1),
(468, 441, 2),
(469, 441, 1),
(469, 441, 2),
(470, 441, 1),
(470, 441, 2),
(480, 442, 1),
(480, 442, 2),
(481, 442, 1),
(482, 442, 1),
(483, 442, 1),
(484, 442, 1),
(485, 442, 1),
(486, 442, 1),
(490, 443, 1),
(491, 443, 1),
(492, 443, 1),
(493, 443, 1),
(494, 443, 1),
(495, 443, 1);

INSERT INTO function_implementation (function_id, language, dialect, implementation)
VALUES(450, 'SQL', null, '"$1"'),
(460, 'SQL', null, 'ABS($1)'),
(461, 'SQL', null, 'FLOOR($1)'),
(462, 'SQL', null, 'CEILING($1)'),
(463, 'SQL', null, 'FLOOR($1)'),
(464, 'SQL', null, 'MIN($1)'),
(465, 'SQL', null, 'MAX($1)'),
(466, 'SQL', null, 'SUM($1)'),
(467, 'SQL', null, 'AVG($1)'),
(468, 'SQL', null, '$1 + $2'),
(469, 'SQL', null, '$1 - $2'),
(470, 'SQL', null, '$1 * $2'),
(480, 'SQL', null, '$1 || " " || $2'),
(481, 'SQL', null, 'LOWER($1)'),
(482, 'SQL', null, 'UPPER($1)'),
(483, 'SQL', null, 'LTRIM(RTEIM($1))'),
(484, 'SQL', null, 'LTRIM($1)'),
(485, 'SQL', null, 'RTRIM($1)'),
(486, 'SQL', null, 'CHAR_LENGTH($1)'),
(490, 'SQL', null, 'EXTRACT(YEAR FROM $1)'),
(491, 'SQL', null, 'EXTRACT(MONTH FROM $1)'),
(492, 'SQL', null, 'EXTRACT(DAY FROM $1)'),
(493, 'SQL', null, 'EXTRACT(HOUR FROM $1)'),
(494, 'SQL', null, 'EXTRACT(MINUTE FROM $1)'),
(495, 'SQL', null, 'EXTRACT(SECOND FROM $1)');

INSERT INTO version (id)
VALUES(16);

INSERT INTO universal_id (sequence_name, id)
VALUES('all', 100000);

INSERT INTO project(id, "name", author, description) VALUES (1, 'DARIAH', 'Setup', 'Main DARIAH Mapping Project');
INSERT INTO project(id, "name", author, description) VALUES (2, 'DARIAH_BAK', 'Setup', 'Main DARIAH Mapping Project');


INSERT INTO db_version_history(id, version, updated) VALUES (1, '1.0', NOW());
