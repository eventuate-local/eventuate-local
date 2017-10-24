#!/bin/bash

set -e

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose.yml -f docker-compose-new-cdc.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :new-cdc:eventuate-local-java-cdc-mysql-service:clean :new-cdc:eventuate-local-java-cdc-mysql-service:assemble

. ./scripts/set-env.sh

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d mysql

./scripts/wait-for-mysql.sh

$DOCKER_COMPOSE up -d

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest

# wait for MySQL

echo waiting for MySQL
./scripts/wait-for-mysql.sh

./scripts/mysql-cli.sh  -i < eventuate-local-java-embedded-cdc/src/test/resources/cdc-test-schema.sql

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:test

# Assert healthcheck good

echo testing restart MySQL restart scenario $(date)

docker stop  $(echo ${PWD##*/} | sed -e 's/-//g')_mysql_1

sleep 10

docker start  $(echo ${PWD##*/} | sed -e 's/-//g')_mysql_1

./scripts/wait-for-mysql.sh

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest :eventuate-local-java-jdbc-tests:test

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v



