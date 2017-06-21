#!/bin/bash

set -e

export DOCKER_COMPOSE="docker-compose -f docker-compose.yml -f docker-compose-cdc.yml"

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :eventuate-local-java-cdc-service:assemble

. ./scripts/set-env.sh

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d mysql
$DOCKER_COMPOSE up -d

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest

# wait for MySQL

echo waiting for MySQL
sleep 10

./scripts/mysql-cli.sh  -i < eventuate-local-java-embedded-cdc/src/test/resources/cdc-test-schema.sql

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:test

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v



