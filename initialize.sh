#! /bin/bash -e

. ./set-env.sh

./gradlew :eventuate-local-java-publisher:assemble

docker-compose up -d connect

echo sleeping to initialize....

sleep 15

./create-mysql-database.sh

./create-topics.sh

echo sleeping to initialize....

sleep 15

./create-connector.sh

sleep 15

docker-compose up -d publisher





