#! /bin/bash

docker run -it --rm --entrypoint=bin/zkCli.sh eventuateio/eventuateio-local-zookeeper:0.5.0  -server $EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING

