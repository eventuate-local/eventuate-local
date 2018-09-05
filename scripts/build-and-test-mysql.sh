#!/bin/bash -e

export database=mysql

./scripts/_build-and-test-mysql.sh -P testCdcMigration=true :eventuate-local-java-embedded-cdc:cleanTest :eventuate-local-java-embedded-cdc:test build
