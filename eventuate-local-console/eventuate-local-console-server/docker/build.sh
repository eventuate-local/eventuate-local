#!/usr/bin/env bash
set -e

rm -fr build
mkdir build

cd ../../
git archive --format=tar.gz -o eventuate-local-console-server/docker/build/eventuate-local-console.tar.gz HEAD

cd eventuate-local-console-server/docker
docker build -t eventuate-local-console-test .
 
