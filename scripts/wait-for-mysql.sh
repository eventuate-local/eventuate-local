#! /bin/sh

until (echo select 1 from dual | ./scripts/mysql-cli.sh -i > /dev/null)
do
 echo sleeping for mysql
 sleep 5
done
