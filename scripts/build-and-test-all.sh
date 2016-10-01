#! /bin/bash

set -e

. ./scripts/set-env.sh

docker-compose stop
docker-compose rm --force -v

docker-compose up -d

./gradlew $* build

docker-compose stop
docker-compose rm --force -v



