#! /bin/bash -e

if [ -z "$ADVERTISED_HOST_NAME" ] ; then
  echo ADVERTISED_HOST_NAME is blank or not set. Finding IP address
  export ADVERTISED_HOST_NAME=$(ip addr | grep 'BROADCAST' -A2 | tail -n1 | awk '{print $2}' | cut -f1  -d'/')
fi

echo ADVERTISED_HOST_NAME=${ADVERTISED_HOST_NAME}

sed -i "s/ADVERTISED_HOST_NAME/${ADVERTISED_HOST_NAME?}/" /usr/local/kafka-config/server.properties

sed -i "s/ZOOKEEPER_SERVERS/${ZOOKEEPER_SERVERS?}/" /usr/local/kafka-config/server.properties

bin/kafka-server-start.sh /usr/local/kafka-config/server.properties
