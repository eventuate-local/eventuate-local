#! /bin/bash -e

set -o pipefail

SCRIPTS="
./scripts/build-and-test-all-mysql.sh
./scripts/build-and-test-all-mssql.sh
./scripts/build-and-test-all-postgres.sh
"

date > build-and-test-everything.log

for script in $SCRIPTS ; do
   echo '****************************************** Running' $script
   date >> build-and-test-everything.log
   echo '****************************************** Running' $script >> build-and-test-everything.log
   $script | tee -a build-and-test-everything.log
done

echo 'Finished successfully!!!'
