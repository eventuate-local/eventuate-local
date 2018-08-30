#! /bin/bash

export TERM=dumb

set -e

. ./scripts/set-env-mysql.sh

GRADLE_OPTS=""

if [ "$1" = "--clean" ] ; then
  GRADLE_OPTS="clean"
  shift
fi

./gradlew ${GRADLE_OPTS} testClasses

docker-compose -f docker-compose-${database}.yml down --remove-orphans -v

docker-compose -f docker-compose-${database}.yml build
docker-compose -f docker-compose-${database}.yml up -d

./scripts/wait-for-mysql.sh

./gradlew $* build -x :new-cdc:eventuate-local-java-cdc-connector-postgres-wal:test

#test spring compatibility

./gradlew -a :eventuate-local-java-jdbc-tests:cleanTest
./gradlew -a :eventuate-local-java-jdbc-tests:test --tests "io.eventuate.local.java.jdbckafkastore.JdbcAutoConfigurationIntegrationTest" -P springBootVersion=2.0.0.M7

docker-compose -f docker-compose-${database}.yml down --remove-orphans -v
