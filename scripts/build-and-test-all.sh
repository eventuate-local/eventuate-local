#! /bin/bash

set -e

export EVENTUATE_EVENT_TRACKER_ITERATIONS=160

docker="./gradlew mysqlCompose"

./gradlew $* testClasses

${docker}Down
${docker}Up

./gradlew build

${docker}Down
