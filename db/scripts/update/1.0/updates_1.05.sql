-- Version 1.5
-- Schema might point to file
ALTER TABLE schema
  ADD COLUMN file_id bigint,
  ADD CONSTRAINT schema_files_fkey FOREIGN KEY (file_id) REFERENCES files (id)
   ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX fki_schema_files_fkey ON schema(file_id);

INSERT INTO db_version_history(id, version, updated) VALUES (6, '1.5', NOW());
-- End Update (Version 1.5)