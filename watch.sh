#! /bin/bash -e

docker run -it --name watcher --rm \
 --link eventuatelocal_zookeeper_1:zookeeper \
 debezium/kafka:0.2 watch-topic -a -k mysql-server-1.test.events


