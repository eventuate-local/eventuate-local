#! /bin/bash -e

set -o pipefail

SCRIPTS="
./scripts/build-and-test-mysql.sh
./scripts/build-and-test-all-new-cdc-mysql.sh
./scripts/build-and-test-mysql-migration.sh
./scripts/build-and-test-mariadb.sh
./scripts/build-and-test-postgres-wal.sh
./scripts/build-and-test-all-new-cdc-postgres-wal.sh
./scripts/build-and-test-all-new-cdc-mariadb.sh
./scripts/build-and-test-all-new-cdc-postgres-polling.sh
./scripts/build-and-test-all-new-cdc-unified.sh
"

date > build-and-test-everything.log

for script in $SCRIPTS ; do
   echo '****************************************** Running' $script
   date >> build-and-test-everything.log
   echo '****************************************** Running' $script >> build-and-test-everything.log
   $script | tee -a build-and-test-everything.log
done

echo 'Finished successfully!!!'
