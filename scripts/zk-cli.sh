#! /bin/bash

docker run -it --rm debezium/zookeeper:0.2 bin/zkCli.sh -server $EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING

