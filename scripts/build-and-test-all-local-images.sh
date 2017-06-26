#!/bin/bash

set -e

export DOCKER_COMPOSE="docker-compose -f docker-compose.yml -f docker-compose-cdc.yml -f docker-compose-eventuate-local-for-testing.yml"

./scripts/build-and-test-all-cdc.sh


