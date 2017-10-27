#! /bin/sh

until (echo select 1 | ./scripts/postgres-cli.sh -i > /dev/null)
do
 echo sleeping for postgres
 sleep 5
done
