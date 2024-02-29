#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $1 $(basename $0)

# If there is no yml config and also no start parameter
# enabling a routing profile, ORS cannot start,
# even if one profile is enabled as param,
# the required param source_file is still missing.
podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  "local/${IMAGE}:latest" \
  $(getProgramArguments $runType --ors.engine.profiles.hgv.enabled=true) &

# expect process finished timout
res=$(expectOrsStartupFails 30 "$CONTAINER" )
# stop container if was not finished
cleanupTest

assertEquals "terminated" "$res"
