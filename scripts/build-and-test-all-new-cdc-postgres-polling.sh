#!/bin/bash

set -e

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-postgres-polling.yml -f docker-compose-new-cdc-postgres-polling.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :new-cdc:eventuate-local-java-cdc-sql-service:clean :new-cdc:eventuate-local-java-cdc-sql-service:assemble

. ./scripts/set-env-postgres-polling.sh

$DOCKER_COMPOSE down -v --remove-orphans

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d postgres
$DOCKER_COMPOSE up -d

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest

# wait for Postgres

echo waiting for Postgres

./scripts/wait-for-postgres.sh
./scripts/wait-for-services.sh $DOCKER_HOST_IP "actuator/health" 8099

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:test -Dtest.single=JdbcAutoConfigurationIntegrationSyncTest

# Assert healthcheck good

echo testing Postgres and zookeeper restart scenario $(date)

$DOCKER_COMPOSE stop postgres zookeeper

sleep 10

$DOCKER_COMPOSE start postgres zookeeper

./scripts/wait-for-postgres.sh

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest :eventuate-local-java-jdbc-tests:test -Dtest.single=JdbcAutoConfigurationIntegrationSyncTest

$DOCKER_COMPOSE down -v --remove-orphans
