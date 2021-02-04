#! /bin/bash

set -e

export database=postgres
export SPRING_PROFILES_ACTIVE=postgres
export MICRONAUT_ENVIRONMENTS=postgres

./scripts/_build-and-test-all.sh