#!/bin/bash
set -e
./build.sh

docker-compose $*
