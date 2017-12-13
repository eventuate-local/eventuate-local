#! /bin/bash

export TERM=dumb

set -e

. ./scripts/set-env.sh

GRADLE_OPTS=""

if [ "$1" = "--clean" ] ; then
  GRADLE_OPTS="clean"
  shift
fi

./gradlew ${GRADLE_OPTS} $* testClasses

docker-compose stop
docker-compose rm --force -v

docker-compose build
docker-compose up -d

./scripts/wait-for-mysql.sh

./gradlew $* build

#test spring compatibility

./gradlew -a :eventuate-local-java-jdbc-tests:cleanTest
./gradlew -a :eventuate-local-java-jdbc-tests:test --tests "io.eventuate.local.java.jdbckafkastore.JdbcAutoConfigurationIntegrationTest" -P springBootVersion=2.0.0.M7

docker-compose stop
docker-compose rm --force -v



