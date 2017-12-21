#! /bin/bash -e

docker-compose -f docker-compose-postgres-wal.yml down
docker-compose -f docker-compose-postgres-wal.yml up --build -d
./scripts/wait-for-postgres.sh

docker exec $(echo ${PWD##*/} | sed -e 's/-//g')_postgres_1 /bin/sh -c "pg_recvlogical -U eventuate -d eventuate --slot test_slot --create-slot -P wal2json"