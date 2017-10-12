#! /bin/bash -e

if [ $(ls build/libs/*SNAPSHOT.jar | wc -l) != "1" ] ; then
    echo not exactly one jar in build/libs/
    exit 99
fi

docker build -t test-eventuate-local-java-new-cdc-mysql-service .
