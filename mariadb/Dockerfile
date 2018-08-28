FROM mariadb:10.3.8
COPY replication.cnf /etc/mysql/conf.d
COPY initialize-database.sql /docker-entrypoint-initdb.d
