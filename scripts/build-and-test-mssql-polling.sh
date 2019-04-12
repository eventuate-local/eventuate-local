#! /bin/bash

export TERM=dumb

set -e

GRADLE_OPTS=""

if [ "$1" = "--clean" ] ; then
  GRADLE_OPTS="clean"
  shift
fi

./gradlew ${GRADLE_OPTS} $* testClasses

. ./scripts/set-env-mssql-polling.sh

docker-compose -f docker-compose-mssql-polling.yml  up --build -d

./scripts/wait-for-mssql.sh

./gradlew $* :new-cdc:eventuate-local-java-cdc-connector-polling:cleanTest :new-cdc:eventuate-local-java-cdc-connector-polling:test -Dtest.single=PollingCdcProcessorTest

docker-compose -f docker-compose-mssql-polling.yml down -v --remove-orphans
