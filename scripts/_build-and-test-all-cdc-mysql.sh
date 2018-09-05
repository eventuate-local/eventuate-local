#!/bin/bash

set -e

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-${database}.yml -f docker-compose-cdc-${database}.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :eventuate-local-java-cdc-service:clean :eventuate-local-java-cdc-service:assemble

. ./scripts/set-env-mysql.sh

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d ${database}
$DOCKER_COMPOSE up -d


./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest

# wait for MySQL

echo waiting for MySQL

./scripts/wait-for-mysql.sh

./scripts/mysql-cli.sh  -i < eventuate-local-java-embedded-cdc/src/test/resources/cdc-test-schema.sql

./scripts/wait-for-services.sh $DOCKER_HOST_IP 8099

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:test

# Assert healthcheck good

echo testing restart MySQL restart scenario $(date)

$DOCKER_COMPOSE stop ${database}

sleep 10

$DOCKER_COMPOSE start ${database}

./scripts/wait-for-mysql.sh

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest :eventuate-local-java-jdbc-tests:test

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v
