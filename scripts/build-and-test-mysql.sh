#!/bin/bash -e

./scripts/build-and-test-mysql-common.sh
./scripts/build-and-test-mysql-embedded-cdc.sh
./scripts/build-and-test-mysql-cdc-connector-polling.sh
./scripts/build-and-test-mysql-cdc-connector-mysql-binlog.sh
