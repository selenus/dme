-- Version 1.10
-- View for ReadOnlySchemaElements contains full Types
DROP VIEW schema_elements;

CREATE OR REPLACE VIEW schema_elements AS 
        (        (        (        (        (        (         SELECT alias.id, 
                                                            alias.name, 
                                                            ''::character varying AS description, 
                                                            'de.dariah.schereg.base.model.Alias'::text AS type, 
                                                            alias.schema_id
                                                           FROM alias
                                                UNION ALL 
                                                         SELECT attribute.id, 
                                                            attribute.name, 
                                                            attribute.description, 
                                                            'de.dariah.schereg.base.model.Attribute'::text AS type, 
                                                            attribute.schema_id
                                                           FROM attribute)
                                        UNION ALL 
                                                 SELECT containment.id, 
                                                    containment.name, 
                                                    containment.description, 
                                                    'de.dariah.schereg.base.model.Containment'::text AS type, 
                                                    containment.schema_id
                                                   FROM containment)
                                UNION ALL 
                                         SELECT domain.id, domain.name, 
                                            domain.description, 
                                            'de.dariah.schereg.base.model.Domain'::text AS type, 
                                            domain.schema_id
                                           FROM domain)
                        UNION ALL 
                                 SELECT domainvalue.id, 
                                    domainvalue.value AS name, 
                                    domainvalue.description, 
                                    'de.dariah.schereg.base.model.DomainValue'::text AS type, 
                                    domainvalue.schema_id
                                   FROM domainvalue)
                UNION ALL 
                         SELECT entity.id, entity.name, entity.description, 
                            'de.dariah.schereg.base.model.Entity'::text AS type, entity.schema_id
                           FROM entity)
        UNION ALL 
                 SELECT relationship.id, relationship.name, 
                    ''::character varying AS description, 
                    'de.dariah.schereg.base.model.Relationship'::text AS type, relationship.schema_id
                   FROM relationship)
UNION ALL 
         SELECT subtype.id, ''::character varying AS name, 
            ''::character varying AS description, 'de.dariah.schereg.base.model.Subtype'::text AS type, 
            subtype.schema_id
           FROM subtype;
           
ALTER TABLE db_version_history DROP CONSTRAINT db_version_history_pkey;
ALTER TABLE db_version_history DROP COLUMN id;

ALTER TABLE db_version_history ADD CONSTRAINT db_version_history_pkey PRIMARY KEY(version);
           
INSERT INTO db_version_history(id, version, updated) VALUES (11, '1.10', NOW());
-- End Update (Version 1.10)