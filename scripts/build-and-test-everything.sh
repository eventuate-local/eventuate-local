#! /bin/bash -e


SCRIPTS="./scripts/build-and-test-all.sh ./scripts/build-and-test-all-cdc.sh ./scripts/build-and-test-all-new-cdc.sh ./scripts/build-and-test-all-new-cdc-postgres.sh"

date > build-and-test-everything.log

for script in $SCRIPTS ; do
   echo '****************************************** Running' $script
   date >> build-and-test-everything.log
   echo '****************************************** Running' $script >> build-and-test-everything.log
   $script | tee -a build-and-test-everything.log
done

echo 'Finished successfully!!!'
