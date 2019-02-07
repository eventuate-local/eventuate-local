#!/bin/bash

. ./scripts/set-env-mysql.sh

export E2EMigrationTest=true

set -e

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-eventuate-local-mysql-for-migration.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

$DOCKER_COMPOSE down

$DOCKER_COMPOSE up --build -d mysql zookeeper kafka

./scripts/wait-for-mysql.sh

$DOCKER_COMPOSE up --build -d oldcdcservice

./scripts/wait-for-services.sh $DOCKER_HOST_IP "health" 8099

./gradlew :eventuate-local-java-migration:cleanTest

./gradlew eventuate-local-java-migration:test --tests "io.eventuate.local.cdc.debezium.migration.MigrationOldCdcPhaseE2ETest"

$DOCKER_COMPOSE stop oldcdcservice

./gradlew assemble
$DOCKER_COMPOSE up --build -d cdcservice

./scripts/wait-for-services.sh $DOCKER_HOST_IP "actuator/health" 8099

./gradlew eventuate-local-java-migration:test --tests "io.eventuate.local.cdc.debezium.migration.MigrationNewCdcPhaseE2ETest"

$DOCKER_COMPOSE down
