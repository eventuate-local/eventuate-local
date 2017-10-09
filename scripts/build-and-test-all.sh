#! /bin/bash

export TERM=dumb

set -e

. ./scripts/set-env.sh

./gradlew clean
./gradlew $* testClasses

docker-compose stop
docker-compose rm --force -v

docker-compose build
docker-compose up -d

sleep 60

./gradlew $* build

docker-compose stop
docker-compose rm --force -v



