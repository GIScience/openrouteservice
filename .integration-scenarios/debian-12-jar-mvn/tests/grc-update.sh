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
      enabled: true
      max_backups: 1
      download_schedule:   '0/10 * * * * *'
      activation_schedule: '5/10 * * * * *'
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

awaitOrsReady 20 "${HOST_PORT}"

graphInfoFileName="$graphs_path/driving-car/graph_info.yml"

yq -i '.importDate = "2020-06-26T10:00:00+0000"' $graphInfoFileName  || exit 1
sleep 20 # more than the download_schedule and (shifted) activation_schedule

profiles=$(requestEnabledProfiles ${HOST_PORT})
profileBackupCount=$(find ${graphs_path} -type d -name 'driving-car_20*' | wc -l)

cleanupTest

assertEquals "1" "$profileBackupCount" "Number of backup directories for profile driving-car"
assertEquals "driving-car" "${profiles}" "Enabled profiles"
