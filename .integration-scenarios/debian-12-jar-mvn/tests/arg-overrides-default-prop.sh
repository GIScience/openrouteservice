#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $1 $(basename $0)

configCar=$(makeTempFile $(basename $0) "\
ors:
  engine:
    source_file:  ors-api/src/test/files/heidelberg.osm.gz
    profiles:
      car:
        enabled: true")

# The start param sets a property, that is not defined in the (loaded) yml config,
# but is present as internal default. This default should be overridden, but the
# properties from ${CONTAINER_CONF_DIR_USER}/ors-config.yml should also be loaded.
podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${configCar}":"${CONTAINER_CONF_DIR_USER}/ors-config.yml" \
  "local/${IMAGE}:latest" \
  $(getProgramArguments ${runType} --ors.engine.profiles.hgv.enabled=true) &

awaitOrsReady 60 "${HOST_PORT}"
profiles=$(requestEnabledProfiles ${HOST_PORT})
cleanupTest

assertEquals 'driving-hgv driving-car' "${profiles}"
