#!/bin/bash

set -e

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-${database}.yml -f docker-compose-new-cdc-${database}.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :new-cdc:eventuate-local-java-cdc-sql-service:clean :new-cdc:eventuate-local-java-cdc-sql-service:assemble

. ./scripts/set-env-mysql.sh

$DOCKER_COMPOSE down -v --remove-orphans

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d ${database}

./scripts/wait-for-mysql.sh

$DOCKER_COMPOSE up -d


./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest

# wait for MySQL

echo waiting for MySQL
./scripts/wait-for-mysql.sh

./scripts/wait-for-services.sh $DOCKER_HOST_IP "actuator/health" 8099

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:test -Dtest.single=JdbcAutoConfigurationIntegrationSyncTest

# Assert healthcheck good

echo testing MySQL and zookeeper restart scenario $(date)

$DOCKER_COMPOSE stop ${database} zookeeper

sleep 10

$DOCKER_COMPOSE start ${database} zookeeper

./scripts/wait-for-mysql.sh

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest :eventuate-local-java-jdbc-tests:test -Dtest.single=JdbcAutoConfigurationIntegrationSyncTest

$DOCKER_COMPOSE down -v --remove-orphans
