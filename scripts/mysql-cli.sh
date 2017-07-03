#! /bin/bash -e

docker run $* \
   --name mysqlterm --link eventuatelocal_mysql_1:mysql --rm mysql:5.7.13 \
   sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD" -o eventuate'
