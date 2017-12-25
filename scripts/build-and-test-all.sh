#! /bin/bash

export TERM=dumb

set -e

. ./scripts/set-env-mysql.sh

GRADLE_OPTS=""

if [ "$1" = "--clean" ] ; then
  GRADLE_OPTS="clean"
  shift
fi

./gradlew ${GRADLE_OPTS} $* testClasses

docker-compose -f docker-compose-mysql.yml stop
docker-compose -f docker-compose-mysql.yml rm --force -v

docker-compose -f docker-compose-mysql.yml build
docker-compose -f docker-compose-mysql.yml up -d

./scripts/wait-for-mysql.sh

./gradlew $* build -x :new-cdc:eventuate-local-java-cdc-connector-postgres-wal:test

docker-compose -f docker-compose-mysql.yml stop
docker-compose -f docker-compose-mysql.yml rm --force -v

echo testing postgres wal

. ./scripts/set-env-postgres-wal.sh

docker-compose -f docker-compose-postgres-wal.yml build
docker-compose -f docker-compose-postgres-wal.yml  up -d

./gradlew $* :new-cdc:eventuate-local-java-cdc-connector-postgres-wal:cleanTest :new-cdc:eventuate-local-java-cdc-connector-postgres-wal:test

docker-compose -f docker-compose-postgres-wal.yml  stop
docker-compose -f docker-compose-postgres-wal.yml  rm --force -v