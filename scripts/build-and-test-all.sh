#! /bin/bash

set -e

. ./scripts/set-env.sh

./gradlew $* testClasses

docker-compose stop
docker-compose rm --force -v

docker-compose build
docker-compose up -d

./gradlew $* build

docker-compose stop
docker-compose rm --force -v



