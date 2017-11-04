#!/bin/bash

set -e

if [ -z "$DOCKER_COMPOSE" ]; then
    echo setting DOCKER_COMPOSE
    export DOCKER_COMPOSE="docker-compose -f docker-compose-postgres.yml -f docker-compose-new-cdc-postgres.yml"
else
    echo using existing DOCKER_COMPOSE = $DOCKER_COMPOSE
fi

export GRADLE_OPTIONS="-P excludeCdcLibs=true"

./gradlew $GRADLE_OPTIONS $* :new-cdc:eventuate-local-java-cdc-sql-service:clean :new-cdc:eventuate-local-java-cdc-sql-service:assemble

. ./scripts/set-env-postgres.sh

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v

$DOCKER_COMPOSE build
$DOCKER_COMPOSE up -d postgres
$DOCKER_COMPOSE up -d

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest

# wait for Postgres

echo waiting for Postgres

./scripts/wait-for-postgres.sh
./scripts/wait-for-services.sh $DOCKER_HOST_IP 8099

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:test

# Assert healthcheck good

echo testing restart Postgres restart scenario $(date)

docker stop  $(echo ${PWD##*/} | sed -e 's/-//g')_postgres_1

sleep 10

docker start  $(echo ${PWD##*/} | sed -e 's/-//g')_postgres_1

./scripts/wait-for-postgres.sh

./gradlew $GRADLE_OPTIONS :eventuate-local-java-jdbc-tests:cleanTest :eventuate-local-java-jdbc-tests:test

$DOCKER_COMPOSE stop
$DOCKER_COMPOSE rm --force -v



