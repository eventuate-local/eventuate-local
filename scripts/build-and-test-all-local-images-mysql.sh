#!/bin/bash

set -e

export DOCKER_COMPOSE="docker-compose -f docker-compose-mysql.yml -f docker-compose-cdc-mysql.yml -f docker-compose-eventuate-local-for-testing-mysql.yml"

./scripts/build-and-test-all-cdc-mysql.sh


