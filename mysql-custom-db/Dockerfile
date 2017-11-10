FROM mysql:5.7.13
COPY replication.cnf /etc/mysql/conf.d
COPY initialize-database.sql /docker-entrypoint-initdb.d
