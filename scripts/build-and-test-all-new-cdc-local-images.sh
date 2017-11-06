#!/bin/bash

set -e

export DOCKER_COMPOSE="docker-compose -f docker-compose.yml -f docker-compose-new-cdc.yml -f docker-compose-eventuate-local-new-cdc-for-testing.yml"

./scripts/build-and-test-all-new-cdc.sh


