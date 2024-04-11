#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $(basename $0) $*

configCar=$(makeTempFile $(basename $0) "\
ors:
  engine:
    profiles:
      car:
        enabled: true")

# The profile configured as run argument should be preferred over environment variable.
# The default yml file should not be used when ORS_CONFIG_LOCATION is set,
# even if the file does not exist. Fallback to default ors-config.yml is not desired!
podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${configCar}":${CONTAINER_WORK_DIR}/ors-config.yml \
  --env ORS_CONFIG_LOCATION=${CONTAINER_WORK_DIR}/nonexisting.yml \
  "local/${IMAGE}:latest" \
  $(getProgramArguments ${runType} ${CONTAINER_WORK_DIR}/config-car.yml) &


# expect process finished timout
res=$(expectOrsStartupFails 60 "$CONTAINER" )
# stop container if was not finished
cleanupTest

assertEquals "terminated" "$res"
