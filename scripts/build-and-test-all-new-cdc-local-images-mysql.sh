#!/bin/bash

set -e

export DOCKER_COMPOSE="docker-compose -f docker-compose-mysql.yml -f docker-compose-new-cdc-mysql.yml -f docker-compose-eventuate-local-new-cdc-for-testing-mysql.yml"

./scripts/build-and-test-all-new-cdc-mysql.sh


