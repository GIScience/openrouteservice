#!/usr/bin/env bash
TESTROOT="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/test.conf
source $TESTROOT/files/testfunctions.sh

echo "CONTAINER_WORK_DIR=$CONTAINER_WORK_DIR"
echo "CONTAINER_CONF_DIR_USER=$CONTAINER_CONF_DIR_USER"
echo "CONTAINER_CONF_DIR_ETC=$CONTAINER_CONF_DIR_ETC"

mkdir -p ~/.m2
M2_FOLDER="$(realpath ~/.m2)"

echo "${FG_CYA}${B}building docker image ${IMAGE_NAME_JAR}${N}"
if podman ps -a | grep -q "$IMAGE_NAME_JAR"; then
  podman rm -f "$IMAGE_NAME_JAR";
fi
podman build -t local/"$IMAGE_NAME_JAR" -f $(dirname $0)/Dockerfile-jar -v $M2_FOLDER:/root/.m2 --build-arg CONTAINER_WORK_DIR="$CONTAINER_WORK_DIR" --build-arg CONTAINER_CONF_DIR_USER="$CONTAINER_CONF_DIR_USER" --build-arg CONTAINER_CONF_DIR_ETC="$CONTAINER_CONF_DIR_ETC" "$TESTROOT"/../..

echo "${FG_CYA}${B}building docker image ${IMAGE_NAME_MVN}${N}"
if podman ps -a | grep -q "$IMAGE_NAME_MVN"; then
  podman rm -f "$IMAGE_NAME_MVN";
fi
podman build -t local/"$IMAGE_NAME_MVN" -f $(dirname $0)/Dockerfile-mvn -v $M2_FOLDER:/root/.m2 --build-arg CONTAINER_WORK_DIR="$CONTAINER_WORK_DIR" --build-arg CONTAINER_CONF_DIR_USER="$CONTAINER_CONF_DIR_USER" --build-arg CONTAINER_CONF_DIR_ETC="$CONTAINER_CONF_DIR_ETC" "$TESTROOT"/../..