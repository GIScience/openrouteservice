#!/bin/bash
TESTROOT="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/test.conf
source $TESTROOT/files/testfunctions.sh
echo "WORK_DIR=$WORK_DIR"
echo "CONF_DIR_USER=$CONF_DIR_USER"
echo "CONF_DIR_ETC=$CONF_DIR_ETC"
echo "TESTFILES_DIR=$TESTFILES_DIR"
echo "TESTSUITES_DIR=$TESTSUITES_DIR"

## Check if the container exists and remove it
#if podman ps -a | grep -q mvn-jar-run-test; then podman rm -f mvn-jar-run-test; fi
#
## Build the container
#podman build -t local/mvn-jar-run-test \
#    --build-arg WORK_DIR=$WORK_DIR --build-arg CONF_DIR_USER=$CONF_DIR_USER --build-arg CONF_DIR_ETC=$CONF_DIR_ETC --build-arg TESTFILES_DIR=$TESTFILES_DIR --build-arg TESTSUITES_DIR=$TESTSUITES_DIR \
#    -f .integration-scenarios/debian-12-jar-mvn/Dockerfile .
#
## Run container
#podman run -d --name mvn-jar-run-test -p 8082:8082 \
#    -v /home/jh/tmp/ors-temp-check-run/.integration-scenarios/debian-12-jar-mvn/testfiles:$TESTFILES_DIR \
#    -v /home/jh/tmp/ors-temp-check-run/.integration-scenarios/debian-12-jar-mvn/testsuites:$TESTSUITES_DIR \
#    -it local/mvn-jar-run-test:latest
#
##podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/warmup.sh"
#
#podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/build-jar.sh" || exit 1
#podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/warmup.sh" || exit 1
#podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/runSuite.sh jar" #|| exit 1
#podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/runSuite.sh mvn" #|| exit 1
##podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/jar/test-startup-fails-without-config.sh" || exit 1
##podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/jar/test-find-config-etc.sh" || exit 1
##podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/jar/test-find-config-workdir.sh" || exit 1
##
##podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/mvn/test-startup-fails-without-config.sh" || exit 1
##podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/mvn/test-find-config-etc.sh" || exit 1
##podman exec mvn-jar-run-test /bin/bash -c "/home/ors/testsuites/mvn/test-find-config-workdir.sh" || exit 1
#
#echo "$(basename $0) done - container still running"


## Check if the container exists and remove it
echo "${FG_CYA}${B}building docker image ${IMAGE_NAME_JAR}${N}"
if podman ps -a | grep -q "$IMAGE_NAME_JAR"; then
  podman rm -f "$IMAGE_NAME_JAR";
fi
podman build -t local/"$IMAGE_NAME_JAR" -f $(dirname $0)/Dockerfile-jar $TESTROOT/../..

echo "${FG_CYA}${B}building docker image ${IMAGE_NAME_MVN}${N}"
if podman ps -a | grep -q "$IMAGE_NAME_MVN"; then
  podman rm -f "$IMAGE_NAME_MVN";
fi
podman build -t local/"$IMAGE_NAME_MVN" -f $(dirname $0)/Dockerfile-mvn $TESTROOT/../..