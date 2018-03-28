#! /bin/bash

export TERM=dumb

set -e

. ./scripts/set-env-mysql.sh

./gradlew clean

docker-compose -f docker-compose-mysql.yml stop
docker-compose -f docker-compose-mysql.yml rm --force -v

docker-compose -f docker-compose-mysql.yml build
docker-compose -f docker-compose-mysql.yml up -d

./scripts/wait-for-mysql.sh

./gradlew eventuate-local-java-embedded-cdc:test --tests "io.eventuate.local.cdc.debezium.PrepareMigrationToNewCdcTest"
./gradlew new-cdc:eventuate-local-java-cdc-connector-mysql-binlog:test --tests "io.eventuate.local.mysql.binlog.MySQLMigrationTest"


docker-compose -f docker-compose-mysql.yml stop
docker-compose -f docker-compose-mysql.yml rm --force -v
