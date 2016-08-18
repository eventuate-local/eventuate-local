#! /bin/bash

set -e

. ./scripts/set-env.sh

docker-compose stop
docker-compose rm --force -v

./scripts/initialize.sh

./gradlew $* cleanTest :eventuate-local-java-jdbc-tests:test -P ignoreE2EFailures=false



