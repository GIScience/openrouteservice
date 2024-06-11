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

podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${configCar}":${CONTAINER_WORK_DIR}/ors-config.yml \
  "local/${IMAGE}:latest" &

awaitOrsReady 60 "${HOST_PORT}"


httpCode=$(
curl -X POST -s -o /dev/null -w '%{response_code}' \
"$(getOrsUrl ${HOST_PORT})/directions/driving-car/geojson" \
-H 'Content-Type: application/json; charset=utf-8' \
-H 'Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8' \
-H 'Authorization: 5b3ce3597851110001cf6248faf196eef8cb4eff986d68da3e856ca8' \
-d '{"coordinates":[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]],"options":{"avoid_polygons":{"type":"Polygon","coordinates":[[[8.68076562,49.4192374],[8.68076562,49.416990],[8.6859798,49.416990],[8.68076562,49.416990],[8.68076562,49.4192374]]]}}}'
)

cleanupTest

assertEquals "200" "$httpCode"
