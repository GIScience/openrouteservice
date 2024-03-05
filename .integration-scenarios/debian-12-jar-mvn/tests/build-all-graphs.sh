#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $(basename $0) $*

configPT=$(makeTempFile $(basename $0) "\
ors:
  engine:
    source_file: ors-api/src/test/files/heidelberg.osm.gz
    profiles:
      bike-electric:
        enabled: true
      bike-mountain:
        enabled: true
      bike-regular:
        enabled: true
      bike-road:
        enabled: true
      car:
        enabled: true
      hgv:
        enabled: true
      hiking:
        enabled: true
      public-transport:
        enabled: true
        gtfs_file: ors-api/src/test/files/vrn_gtfs_cut.zip
      walking:
        enabled: true
      wheelchair:
        enabled: true
")

podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${configPT}":"${CONTAINER_WORK_DIR}/ors-config.yml" \
  "local/${IMAGE}:latest" &

awaitOrsReady 300 "${HOST_PORT}"
profiles=$(requestEnabledProfiles ${HOST_PORT})
cleanupTest

assertEquals "foot-walking wheelchair foot-hiking public-transport cycling-electric cycling-mountain driving-car driving-hgv cycling-regular cycling-road" "${profiles}"