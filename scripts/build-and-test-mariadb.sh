#!/bin/bash -e

./scripts/build-and-test-mariadb-common.sh
./scripts/build-and-test-mariadb-embedded-cdc.sh
./scripts/build-and-test-mariadb-cdc-connector-polling.sh
./scripts/build-and-test-mariadb-cdc-connector-mariadb-binlog.sh
