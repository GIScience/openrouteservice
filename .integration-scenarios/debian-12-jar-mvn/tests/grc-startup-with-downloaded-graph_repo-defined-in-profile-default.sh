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
      repo:
        repository_uri: '/home/ors/graph_repo'
        repository_name: vendor-xyz
        repository_profile_group: fastisochrones
        graph_extent: heidelberg
    graph_management:
      enabled: true
    profiles:
      driving-car:
        enabled: true
")

# The test asserts that ORS is able to startup without a local graph AND without a source_file
# when graph_management is enabled and the configured graph is available in the remote repository.
# 'source_file' is set to null, to avoid the graph is built locally.

podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${graphs_path}":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${PROJECTROOT}/ors-engine/src/test/resources/test-filesystem-repos":"/home/ors/graph_repo" \
  -v "${configCar}":${CONTAINER_WORK_DIR}/ors-config.yml \
  "local/${IMAGE}:latest" &

awaitOrsReady 60 "${HOST_PORT}"

profiles=$(requestEnabledProfiles ${HOST_PORT})

cleanupTest

assertEquals "driving-car" "${profiles}"
