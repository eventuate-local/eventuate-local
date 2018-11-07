FROM mysql:8.0.0
COPY replication.cnf /etc/mysql/conf.d
COPY initialize-database.sql /docker-entrypoint-initdb.d
