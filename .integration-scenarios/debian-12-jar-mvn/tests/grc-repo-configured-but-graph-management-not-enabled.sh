#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
PROJECTROOT="$( cd "$(dirname "$0")"/../../.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $(basename $0) $*

tempdir=$(makeTempDir $(basename $0))
graphs_path="${tempdir}/graphs"
mkdir $graphs_path
configCar=$(writeToFile $tempdir ors-config.yml "\
ors:
  engine:
    profile_default:
      source_file:
    graph_management:
      enabled: false
      download_schedule:   '0/15 * * * * *'
      activation_schedule: '5/15 * * * * *'
    profiles:
      driving-car:
        enabled: true
        repo:
          repository_uri: '/home/ors/graph_repo'
          repository_name: vendor-xyz
          repository_profile_group: fastisochrones
          graph_extent: heidelberg
")

# The test asserts that valid graph repo configuration for a profile is ignored and the existing graph is not downloaded from the repo
# when graph_management is not enabled.
# 'source_file' is set to null, to avoid the graph is built locally.
# Startup of ORS should fail because no local graph exists, and the graph is neigher generated nor downloaded.

podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${graphs_path}":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${PROJECTROOT}/ors-engine/src/test/resources/test-filesystem-repos":"/home/ors/graph_repo" \
  -v "${configCar}":${CONTAINER_WORK_DIR}/ors-config.yml \
  "local/${IMAGE}:latest" &

# expect process finished timout
res=$(expectOrsStartupFails 60 "$CONTAINER" )
# stop container if was not finished
cleanupTest

assertEquals "terminated" "$res"
