-- Version 1.2
-- Required to fullfill requirements of new model thus keeping backwards compatibility
-- 	with old M3 model (value needed) 
ALTER TABLE domainvalue ADD COLUMN name character varying(100);

INSERT INTO db_version_history(id, version, updated) VALUES (3, '1.2', NOW());
-- End Update (Version 1.2)