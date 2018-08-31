export database=mariadb

./scripts/_build-and-test-mysql.sh -x :eventuate-local-java-embedded-cdc:test -x :new-cdc:eventuate-local-java-cdc-connector-polling:test -x :new-cdc:eventuate-local-java-cdc-connector-mysql-binlog:test
