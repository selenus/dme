-- Version 1.1
ALTER TABLE subtype ADD COLUMN "name" character varying(100);
ALTER TABLE subtype ADD COLUMN description character varying(4096);

INSERT INTO db_version_history(id, version, updated) VALUES (2, '1.1', NOW());
-- End Update (Version 1.1)