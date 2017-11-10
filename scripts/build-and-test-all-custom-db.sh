#! /bin/bash

export TERM=dumb

set -e

. ./scripts/set-env-custom-db.sh

GRADLE_OPTS=""

if [ "$1" = "--clean" ] ; then
  GRADLE_OPTS="clean"
  shift
fi

./gradlew ${GRADLE_OPTS} $* testClasses

docker-compose -f docker-compose-custom-db.yml stop
docker-compose -f docker-compose-custom-db.yml rm --force -v

docker-compose -f docker-compose-custom-db.yml build
docker-compose -f docker-compose-custom-db.yml up -d

./scripts/wait-for-mysql.sh

./gradlew $* build

docker-compose -f docker-compose-custom-db.yml stop
docker-compose -f docker-compose-custom-db.yml rm --force -v



