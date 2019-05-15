#! /bin/bash

set -e

. ./scripts/set-env-mysql.sh

./gradlew $* testClasses

docker-compose down -v --remove-orphans

docker-compose up --build -d mysql zookeeper kafka

./scripts/wait-for-mysql.sh

docker-compose up --build -d

./scripts/wait-for-services.sh $DOCKER_HOST_IP "actuator/health" 8099

./gradlew build

docker-compose down -v --remove-orphans
