#! /bin/bash -e

. ./scripts/set-env.sh

docker-compose up -d

echo sleeping to initialize....

sleep 15

./scripts/create-mysql-database.sh





