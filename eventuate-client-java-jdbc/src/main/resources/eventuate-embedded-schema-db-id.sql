CREATE SCHEMA IF NOT EXISTS eventuate AUTHORIZATION SA;
SET SCHEMA eventuate;

DROP table IF EXISTS events;
DROP table IF EXISTS  entities;
DROP table IF EXISTS  snapshots;

create table events (
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

create table entities (
  entity_type VARCHAR,
  entity_id VARCHAR,
  entity_version VARCHAR,
  PRIMARY KEY(entity_type, entity_id)
);

create table snapshots (
  entity_type VARCHAR,
  entity_id VARCHAR,
  entity_version VARCHAR,
  snapshot_type VARCHAR,
  snapshot_json VARCHAR,
  triggering_events VARCHAR,
  PRIMARY KEY(entity_type, entity_id,entity_version)
);

