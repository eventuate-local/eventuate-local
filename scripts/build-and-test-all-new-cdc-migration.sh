#! /bin/bash

export TERM=dumb

set -e

. ./scripts/set-env.sh

./gradlew clean

docker-compose stop
docker-compose rm --force -v

docker-compose build
docker-compose up -d

./scripts/wait-for-mysql.sh

./gradlew eventuate-local-java-embedded-cdc:test --tests "io.eventuate.local.cdc.debezium.PrepareMigrationToNewCdcTest"
./gradlew new-cdc:eventuate-local-java-cdc-connector-mysql-binlog:test --tests "io.eventuate.local.mysql.binlog.MySQLMigrationTest"


docker-compose stop
docker-compose rm --force -v
