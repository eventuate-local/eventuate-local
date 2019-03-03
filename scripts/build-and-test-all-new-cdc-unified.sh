#!/bin/bash

set -e

. ./scripts/_set-env.sh

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-unified.yml -f docker-compose-new-cdc-unified.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS assemble

. ./scripts/set-env-mysql.sh

$DOCKER_COMPOSE down -v --remove-orphans

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d postgrespollingpipeline mysqlbinlogpipeline postgreswalpipeline

./scripts/wait-for-mysql.sh
./scripts/wait-for-postgres.sh
export POSTGRES_PORT=5433
./scripts/wait-for-postgres.sh

$DOCKER_COMPOSE up -d

./scripts/wait-for-services.sh $DOCKER_HOST_IP "actuator/health" 8099

echo "testing mysql binlog"

. ./scripts/set-env-mysql.sh
./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:clean :eventuate-local-java-jdbc-tests:test -Dtest.single=JdbcAutoConfigurationIntegrationSyncTest

echo "testing postgres polling"

. ./scripts/set-env-postgres-polling.sh
./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:clean :eventuate-local-java-jdbc-tests:test -Dtest.single=JdbcAutoConfigurationIntegrationSyncTest

echo "testing postgres wal"

. ./scripts/set-env-postgres-wal.sh
export SPRING_DATASOURCE_URL=jdbc:postgresql://${DOCKER_HOST_IP}:5433/eventuate
./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:clean :eventuate-local-java-jdbc-tests:test -Dtest.single=JdbcAutoConfigurationIntegrationSyncTest

$DOCKER_COMPOSE down -v --remove-orphans
