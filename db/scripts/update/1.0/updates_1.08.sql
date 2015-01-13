-- Version 1.8
-- Modifying mapping cell input concept from OpenII to a cleaner, more extensible model
CREATE TABLE mapping_cell_input (
   id bigint NOT NULL, 
   entity_id bigint,
   mapping_cell_id bigint NOT NULL,
   constant_value character varying(50),
   constant_data_type character varying(255),
   CONSTRAINT mapping_cell_input_pkey PRIMARY KEY (id)
);

ALTER TABLE mapping_cell_input 
   ADD CONSTRAINT fkey_mapping_cell_input_mapping_cell FOREIGN KEY (mapping_cell_id) REFERENCES mapping_cell (id) ON UPDATE CASCADE ON DELETE CASCADE;

CREATE INDEX fki_fkey_mapping_cell_input_mapping_cell ON mapping_cell_input(mapping_cell_id);

ALTER TABLE mapping_cell DROP COLUMN input_ids;
ALTER TABLE mapping_cell DROP COLUMN modification_date;

ALTER TABLE mapping_cell ADD COLUMN "name" character varying(100);
ALTER TABLE mapping_cell ADD COLUMN "description" character varying(4096);

ALTER TABLE mapping_cell ADD COLUMN "function" character varying(4096);

INSERT INTO db_version_history(id, version, updated) VALUES (9, '1.8', NOW());
-- End Update (Version 1.8)