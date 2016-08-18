#! /bin/bash -e

./scripts/mysql-cli.sh  -i <<END

create database eventuate;

GRANT ALL PRIVILEGES ON eventuate.* TO 'mysqluser'@'%' WITH GRANT OPTION;

USE eventuate;

DROP table IF EXISTS events;
DROP table IF EXISTS  entities;

create table events (
  event_id varchar(1000) PRIMARY KEY,
  event_type varchar(1000),
  event_data varchar(1000),
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  triggering_event VARCHAR(1000)
);

CREATE INDEX events_idx ON events(entity_type, entity_id, event_id);

create table entities (
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  entity_version VARCHAR(1000),
  PRIMARY KEY(entity_type, entity_id)
);



END