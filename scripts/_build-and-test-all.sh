#! /bin/bash

set -e

export EVENTUATE_EVENT_TRACKER_ITERATIONS=160

docker="./gradlew ${database}Compose"

./gradlew $* testClasses

${docker}Down
${docker}Up

./gradlew cleanTest build

${docker}Down

export USE_DB_ID=true
export EVENTUATE_OUTBOX_ID=1

${docker}Up

./gradlew cleanTest build

${docker}Down
