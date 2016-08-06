DROP table IF EXISTS events;
DROP table IF EXISTS  entities;

create table events (
  event_id varchar PRIMARY KEY,
  event_type varchar,
  event_data varchar,
  entity_type VARCHAR,
  entity_id VARCHAR,
  triggering_event VARCHAR
);

create table entities (
  entity_type VARCHAR,
  entity_id VARCHAR,
  entity_version VARCHAR,
  PRIMARY KEY(entity_type, entity_id)
);
