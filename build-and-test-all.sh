#! /bin/bash

set -e

. ./set-env.sh

docker-compose stop
docker-compose rm --force -v

./initialize.sh

./gradlew cleanTest :eventuate-local-java-jdbc-tests:test -P ignoreE2EFailures=false



