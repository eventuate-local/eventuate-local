#! /bin/bash

set -e

export database=mysql

./scripts/_build-and-test-all.sh -P springBootVersion=3.0.1
