#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $(basename $0) $*

configCar=$(makeTempFile $(basename $0) "\
ors:
  engine:
    source_file:  ors-api/src/test/files/heidelberg.osm.gz
    profiles:
      car:
        enabled: true")

# This test asserts that the environment variable PBF_FILE_PATH
# IS NOT EVALUATED when a YAML config is used.
# Here, the yml config contains a valid path to an existing OSM file
# and PBF_FILE_PATH contains a wrong path.
# The expectation is, that the correct path from the yml survives
# and openrouteservice starts up with the expected routing profile.
podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${configCar}":${CONTAINER_WORK_DIR}/ors-config.yml \
  --env PBF_FILE_PATH=ors-api/src/test/files/xxxx.osm.gz \
  "local/${IMAGE}:latest" &

awaitOrsReady 60 "${HOST_PORT}"
profiles=$(requestEnabledProfiles ${HOST_PORT})
cleanupTest

assertEquals "driving-car" "${profiles}"
