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

docker-compose -f docker-compose-${database}.yml stop
docker-compose -f docker-compose-${database}.yml rm --force -v

docker-compose -f docker-compose-${database}.yml build
docker-compose -f docker-compose-${database}.yml up -d

./scripts/wait-for-mysql.sh

./gradlew $* build -x :new-cdc:eventuate-local-java-cdc-connector-postgres-wal:test -x :eventuate-local-java-embedded-cdc:test

docker-compose -f docker-compose-${database}.yml stop
docker-compose -f docker-compose-${database}.yml rm --force -v