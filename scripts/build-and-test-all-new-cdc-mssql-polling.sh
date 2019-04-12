#!/bin/bash

set -e

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-mssql-polling.yml -f docker-compose-new-cdc-mssql-polling.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :new-cdc:eventuate-local-java-cdc-sql-service:clean :new-cdc:eventuate-local-java-cdc-sql-service:assemble

. ./scripts/set-env-mssql-polling.sh

$DOCKER_COMPOSE down -v --remove-orphans

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d mssql
$DOCKER_COMPOSE up -d

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest

# wait for mssql

echo waiting for mssql

sleep 20
./scripts/wait-for-services.sh $DOCKER_HOST_IP "actuator/health" 8099

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:test -Dtest.single=JdbcAutoConfigurationIntegrationSyncTest

# Assert healthcheck good

echo testing mssql and zookeeper restart scenario $(date)

$DOCKER_COMPOSE stop mssql zookeeper

sleep 10

$DOCKER_COMPOSE start mssql zookeeper

sleep 20

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest :eventuate-local-java-jdbc-tests:test -Dtest.single=JdbcAutoConfigurationIntegrationSyncTest

$DOCKER_COMPOSE down -v --remove-orphans
