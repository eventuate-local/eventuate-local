#! /bin/bash

set -e

export database=mssql
export SPRING_PROFILES_ACTIVE=mssql
export MICRONAUT_ENVIRONMENTS=mssql

./scripts/_build-and-test-all.sh