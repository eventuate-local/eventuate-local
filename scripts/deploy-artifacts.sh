#! /bin/bash -e

DOCKER_COMPOSE_PREFIX=$(echo ${PWD##*/} | sed -e 's/-//g')_

DOCKER_REPO=eventuateio
REMOTE_PREFIX=eventuateio-local
IMAGES="cdc-service mysql postgres zookeeper kafka"

BRANCH=$(git rev-parse --abbrev-ref HEAD)

PUSH_DISABLED=

#if [[  "$BRANCH" = "wip-unified-cdc-2" ]] ; then
#  BINTRAY_REPO_TYPE=snapshot
#  VERSION=0.30.0-SNAPSHOT
#elif

if ! [[  $BRANCH =~ ^[0-9]+ ]] ; then
  echo Not release $BRANCH - no PUSH
  PUSH_DISABLED=yes
  BINTRAY_REPO_TYPE=no-pushing
elif [[  $BRANCH =~ RELEASE$ ]] ; then
  BINTRAY_REPO_TYPE=release
elif [[  $BRANCH =~ M[0-9]+$ ]] ; then
    BINTRAY_REPO_TYPE=milestone
elif [[  $BRANCH =~ RC[0-9]+$ ]] ; then
    BINTRAY_REPO_TYPE=rc
else
  echo cannot figure out bintray for this branch $BRANCH
  exit -1
fi

echo BINTRAY_REPO_TYPE=${BINTRAY_REPO_TYPE}

if [ -z "$VERSION" ] ; then
    VERSION=$BRANCH
fi

# Dockerfiles look for snapshot version of JAR!

$PREFIX ./gradlew assemble
docker-compose -f docker-compose-mysql.yml -f docker-compose-new-cdc-mysql.yml build cdcservice mysql zookeeper kafka
docker-compose -f docker-compose-postgres-wal.yml -f docker-compose-new-cdc-postgres-wal.yml build postgres

$PREFIX ./gradlew -P version=${VERSION} \
  testClasses

if [ -z "$PUSH_DISABLED" ] ; then
    $PREFIX ./gradlew -P version=${VERSION} \
      -P bintrayRepoType=${BINTRAY_REPO_TYPE} \
      -P deployUrl=https://dl.bintray.com/eventuateio-oss/eventuate-maven-${BINTRAY_REPO_TYPE} \
      bintrayUpload
else
    echo $PUSH_DISABLED - not uploading to bintray $VERSION
fi

function tagAndPush() {
  LOCAL=$1
  REMOTE="$REMOTE_PREFIX-$2"
  $PREFIX docker tag ${DOCKER_COMPOSE_PREFIX?}$LOCAL $DOCKER_REPO/$REMOTE:$VERSION
  $PREFIX docker tag ${DOCKER_COMPOSE_PREFIX?}$LOCAL $DOCKER_REPO/$REMOTE:latest

  if [ -z "$PUSH_DISABLED" ] ; then
    echo Pushing $DOCKER_REPO/$REMOTE:$VERSION
    $PREFIX docker push $DOCKER_REPO/$REMOTE:$VERSION
    $PREFIX docker push $DOCKER_REPO/$REMOTE:latest
  else
    echo $PUSH_DISABLED - skipping docker push $DOCKER_REPO/$REMOTE:$VERSION
  fi
}


if [ -z "$PUSH_DISABLED" ] ; then
    $PREFIX docker login -u ${DOCKER_USER_ID?} -p ${DOCKER_PASSWORD?}
fi

for image in $IMAGES ; do
    tagAndPush $(echo $image | sed -e 's/-//g')  $image
done
