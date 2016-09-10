#! /bin/bash -e

docker-compose stop
docker-compose rm -v --force

. ./scripts/initialize.sh

./gradlew build

docker-compose stop
docker-compose rm -v --force


