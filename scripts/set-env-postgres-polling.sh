. ./scripts/_set-env.sh

export SPRING_DATASOURCE_URL=jdbc:postgresql://${DOCKER_HOST_IP}/eventuate
export SPRING_DATASOURCE_USERNAME=eventuate
export SPRING_DATASOURCE_PASSWORD=eventuate
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
export SPRING_PROFILES_ACTIVE=EventuatePolling
export EVENTUATELOCAL_CDC_READER_NAME=PostgresPollingReader

echo SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL
