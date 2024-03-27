#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $(basename $0) $*

configPT=$(makeTempFile $(basename $0) "\
ors:
  engine:
    source_file: ors-api/src/test/files/heidelberg.osm.gz
    profile_default:
      maximum_distance: 111111
      maximum_distance_dynamic_weights: 111111
      maximum_distance_avoid_areas: 111111
      maximum_waypoints: 11
    profiles:
      car:
        enabled: true
        maximum_waypoints: 55
")

podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${configPT}":"${CONTAINER_WORK_DIR}/ors-config.yml" \
  "local/${IMAGE}:latest" &

awaitOrsReady 300 "${HOST_PORT}"
statusString=$(requestStatusString ${HOST_PORT})
cleanupTest

assertEquals "111111" "$(echo $statusString | jq -r '.profiles."profile 1".limits.maximum_distance')" "maximum_distance"
assertEquals "55"     "$(echo $statusString | jq -r '.profiles."profile 1".limits.maximum_waypoints')" "maximum_waypoints"
assertEquals "111111" "$(echo $statusString | jq -r '.profiles."profile 1".limits.maximum_distance_dynamic_weights')" "maximum_distance_dynamic_weights"
assertEquals "111111" "$(echo $statusString | jq -r '.profiles."profile 1".limits.maximum_distance_avoid_areas')" "maximum_distance_avoid_areas"
