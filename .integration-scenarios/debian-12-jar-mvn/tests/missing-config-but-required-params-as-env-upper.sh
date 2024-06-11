#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $(basename $0) $*

# Even if no yml config file is present, the ors is runnable
# if at least one routing profile is enabled with a environment variable
# and a source_file is also specified.
podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  --env ORS_ENGINE_PROFILES_HGV_ENABLED=true \
  --env ORS_ENGINE_SOURCE_FILE=ors-api/src/test/files/heidelberg.osm.gz \
  "local/${IMAGE}:latest" &

awaitOrsReady 60 "${HOST_PORT}"
profiles=$(requestEnabledProfiles ${HOST_PORT})
cleanupTest

assertEquals "driving-hgv" "${profiles}"
