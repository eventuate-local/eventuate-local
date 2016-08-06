#! /bin/bash -e

docker run -it --rm --link eventuatelocal_zookeeper_1:zookeeper \
   debezium/kafka:0.2 create-topic -r 1 schema-changes.eventuate
