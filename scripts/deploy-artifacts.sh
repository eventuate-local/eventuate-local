#! /bin/bash -e

DOCKER_COMPOSE_PREFIX=$(echo ${PWD##*/} | sed -e 's/-//g')_

DOCKER_REPO=eventuateio
REMOTE_PREFIX=eventuateio-local
IMAGES="cdc-service new-cdc-service mysql postgres zookeeper kafka"

BRANCH=$(git rev-parse --abbrev-ref HEAD)

PUSH_DISABLED=

if ! [[  $BRANCH =~ ^[0-9]+ ]] ; then
  echo Not release $BRANCH - no PUSH
  PUSH_DISABLED=yes
fi

VERSION=$BRANCH

# Dockerfiles look for snapshot version of JAR!

$PREFIX ./gradlew assemble
docker-compose -f docker-compose-mysql.yml -f docker-compose-cdc-mysql.yml build cdcservice mysql zookeeper kafka
docker-compose -f docker-compose-mysql.yml -f docker-compose-new-cdc-mysql.yml build newcdcservice
docker-compose -f docker-compose-postgres-wal.yml -f docker-compose-new-cdc-postgres-wal.yml build postgres

$PREFIX ./gradlew -P version=${VERSION} \
  testClasses

if [ -z "$PUSH_DISABLED" ] ; then
    $PREFIX ./gradlew -P version=${VERSION} \
      -P deployUrl=https://dl.bintray.com/eventuateio-oss/eventuate-maven-release \
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

$PREFIX docker login -u ${DOCKER_USER_ID?} -p ${DOCKER_PASSWORD?}


for image in $IMAGES ; do
    tagAndPush $(echo $image | sed -e 's/-//g')  $image
done
