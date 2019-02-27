#! /bin/bash

export TERM=dumb

set -e

GRADLE_OPTS=""

if [ "$1" = "--clean" ] ; then
  GRADLE_OPTS="clean"
  shift
fi

./gradlew ${GRADLE_OPTS} $* testClasses

. ./scripts/set-env-postgres-wal.sh

docker-compose -f docker-compose-postgres-wal.yml build
docker-compose -f docker-compose-postgres-wal.yml  up -d

./scripts/wait-for-postgres.sh

./gradlew $* :new-cdc:eventuate-local-java-cdc-connector-postgres-wal:cleanTest :new-cdc:eventuate-local-java-cdc-connector-postgres-wal:test

docker-compose -f docker-compose-postgres-wal.yml down -v --remove-orphans
