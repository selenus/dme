-- Version 1.6
-- LogEntry can be deleted (log4j relict)
DROP TABLE log_entry;

INSERT INTO db_version_history(id, version, updated) VALUES (7, '1.6', NOW());
-- End Update (Version 1.6)