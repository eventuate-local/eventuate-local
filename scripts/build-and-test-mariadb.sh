#!/bin/bash -e

export database=mariadb

./scripts/_build-and-test-mysql.sh :new-cdc:eventuate-local-java-cdc-connector-mysql-binlog:cleanTest :new-cdc:eventuate-local-java-cdc-connector-mysql-binlog:test
