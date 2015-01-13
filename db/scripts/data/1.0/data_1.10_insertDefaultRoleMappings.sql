-- Applies to DB-Version 1.10-
-- Insert the basic role mappings for DARIAH in order to have a functioning startup database
INSERT INTO roles_role_mapping(id, role_id, endpoint, "name", isactive, modified, created)
    VALUES 	(1, 1, 'https://ldap-dariah.esc.rzg.mpg.de/idp/shibboleth', 'schema-registry-admins', true, NOW(), NOW()),
			(2, 5, 'https://ldap-dariah.esc.rzg.mpg.de/idp/shibboleth', 'dariah-de-contributors', true, NOW(), NOW()),
    		(3, 4, 'https://ldap-dariah.esc.rzg.mpg.de/idp/shibboleth', 'schema-registry-developers', true, NOW(), NOW()),
    		(4, 4, 'https://ldap-dariah.esc.rzg.mpg.de/idp/shibboleth', 'schema-registry-contributors', true, NOW(), NOW());
