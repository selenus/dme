-- Version 1.7
-- Introducing new user and role infrastructure interacting with SAML IDPs
CREATE TABLE roles (
   id bigint NOT NULL, 
   authority character varying(100) NOT NULL, 
   description character varying(255), 
   "level" integer, 
   modified timestamp with time zone, 
   created timestamp with time zone, 
   CONSTRAINT roles_pkey PRIMARY KEY (id)
);

CREATE TABLE users
(
  id bigint NOT NULL,
  endpointid character varying(255) NOT NULL,
  nameid character varying(255) NOT NULL,
  eduPersonPrincipalName character varying(255),
  eduPersonEntitlement character varying(255),
  eduPersonAffiliation character varying(75),
  eduPersonScopedAffiliation character varying(255),
  endpointname character varying(255),
  expired boolean DEFAULT false,
  commonname character varying(150),
  lastname character varying(75),
  firstname character varying(75),
  email character varying(100),
  language character varying(2),
  currentmemberships text,
  lastlogin timestamp with time zone,
  modified timestamp with time zone,
  created timestamp with time zone,

  CONSTRAINT users_pkey PRIMARY KEY (id),
  CONSTRAINT users_unique_email UNIQUE (email)
);


CREATE TABLE roles_role_mapping (
   id bigint NOT NULL, 
   role_id bigint NOT NULL, 
   endpoint character varying(255), 
   "name" character varying(255), 
   description character varying(255), 
   isActive boolean DEFAULT true, 
   expires timestamp with time zone, 
   modified timestamp with time zone, 
   created timestamp with time zone, 
   CONSTRAINT roles_role_mapping_pkey PRIMARY KEY (id), 
   CONSTRAINT roles_role_mapping_roles_fkey FOREIGN KEY (role_id) REFERENCES roles (id) ON UPDATE CASCADE ON DELETE CASCADE,
   CONSTRAINT roles_role_mapping_endpoint_name_unique UNIQUE (endpoint, "name")
);

CREATE TABLE roles_user_mapping (
   role_id bigint NOT NULL, 
   user_id bigint NOT NULL, 
   description character varying(255), 
   isActive boolean DEFAULT false, 
   expires timestamp with time zone, 
   modified timestamp with time zone, 
   created timestamp with time zone, 
   CONSTRAINT roles_user_mapping_pkey PRIMARY KEY (role_id, user_id), 
   CONSTRAINT roles_user_mapping_roles_fkey FOREIGN KEY (role_id) REFERENCES roles (id) ON UPDATE CASCADE ON DELETE CASCADE,
   CONSTRAINT roles_user_mapping_users_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE,
   CONSTRAINT roles_user_mapping_role_user_unique UNIQUE (role_id, user_id)
);

INSERT INTO roles(id, authority, description, "level", modified, created)
    VALUES (1, 'ROLE_ADMINISTRATOR', 'Top-level administrator with all rights', 100, NOW(), NOW());

INSERT INTO roles(id, authority, description, "level", modified, created)
    VALUES (2, 'ROLE_CONTENTADMIN', 'Administrator with all rights to content', 80, NOW(), NOW());

INSERT INTO roles(id, authority, description, "level", modified, created)
    VALUES (3, 'ROLE_DOMAINEXPERT', 'Experts in their respective domain with write acces to generic crosswalks', 60, NOW(), NOW());

INSERT INTO roles(id, authority, description, "level", modified, created)
    VALUES (4, 'ROLE_CONTRIBUTOR', 'Contributor to user/archive level schemas and crosswalks', 40, NOW(), NOW());

INSERT INTO roles(id, authority, description, "level", modified, created)
    VALUES (5, 'ROLE_USER', 'Regular user with limited access privileges', 20, NOW(), NOW());
    
INSERT INTO roles(id, authority, description, "level", modified, created)
    VALUES (6, 'ROLE_AUTHENTICATED_GUEST', 'Authenticated user that has no assigned roles or rights and cannot be re-identified (transient or unspecified NameID)', 1, NOW(), NOW());

INSERT INTO db_version_history(id, version, updated) VALUES (8, '1.7', NOW());
-- End Update (Version 1.7)