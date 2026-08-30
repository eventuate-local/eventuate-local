CREATE SCHEMA IF NOT EXISTS eventuate AUTHORIZATION SA;

DROP table IF EXISTS eventuate.events;
DROP table IF EXISTS eventuate.entities;
DROP table IF EXISTS eventuate.snapshots;

create table eventuate.events (
  id BIGINT PRIMARY KEY auto_increment,
  event_id VARCHAR,
  event_type VARCHAR,
  event_data VARCHAR,
  entity_type VARCHAR,
  entity_id VARCHAR,
  triggering_event VARCHAR,
  metadata VARCHAR,
  published TINYINT
);

create table eventuate.entities (
  entity_type VARCHAR,
  entity_id VARCHAR,
  entity_version VARCHAR,
  PRIMARY KEY(entity_type, entity_id)
);

create table eventuate.snapshots (
  entity_type VARCHAR,
  entity_id VARCHAR,
  entity_version VARCHAR,
  snapshot_type VARCHAR,
  snapshot_json VARCHAR,
  triggering_events VARCHAR,
  PRIMARY KEY(entity_type, entity_id,entity_version)
);

