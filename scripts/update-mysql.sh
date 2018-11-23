#! /bin/sh

echo "create table cdc_monitoring (reader_id BIGINT PRIMARY KEY, last_time BIGINT)" | ./scripts/mysql-cli.sh -i