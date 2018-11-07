create database eventuate;
GRANT ALL PRIVILEGES ON eventuate.* TO 'mysqluser'@'%' WITH GRANT OPTION;

USE eventuate;

DROP table IF EXISTS events;
DROP table IF EXISTS  entities;
DROP table IF EXISTS  snapshots;
DROP table IF EXISTS cdc_monitoring;

create table events (
  event_id varchar(500) PRIMARY KEY,
  event_type varchar(500),
  event_data varchar(500) NOT NULL,
  entity_type VARCHAR(500) NOT NULL,
  entity_id VARCHAR(500) NOT NULL,
  triggering_event VARCHAR(500),
  metadata VARCHAR(500),
  published TINYINT DEFAULT 0
);

CREATE INDEX events_idx ON events(entity_type, entity_id, event_id);
CREATE INDEX events_published_idx ON events(published, event_id);

create table entities (
  entity_type VARCHAR(500),
  entity_id VARCHAR(500),
  entity_version VARCHAR(500) NOT NULL,
  PRIMARY KEY(entity_type, entity_id)
);

CREATE INDEX entities_idx ON events(entity_type, entity_id);

create table snapshots (
  entity_type VARCHAR(500),
  entity_id VARCHAR(500),
  entity_version VARCHAR(500),
  snapshot_type VARCHAR(500) NOT NULL,
  snapshot_json VARCHAR(500) NOT NULL,
  triggering_events VARCHAR(500),
  PRIMARY KEY(entity_type, entity_id, entity_version)
);

create table cdc_monitoring (
  reader_id BIGINT PRIMARY KEY,
  last_time BIGINT
);
