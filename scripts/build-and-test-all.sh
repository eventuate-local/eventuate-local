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

docker-compose stop
docker-compose rm --force -v



