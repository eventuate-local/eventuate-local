#! /bin/bash -e

curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json"  ${DOCKER_HOST_IP?}:9095/connectors/ -d @./create-connector-request.json

