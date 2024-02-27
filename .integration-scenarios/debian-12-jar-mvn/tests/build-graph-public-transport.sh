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
      public-transport:
        enabled: true
        profile: public-transport
        encoder_options:
          block_fords: false
        maximum_visited_nodes: 15000
        gtfs_file: ors-api/src/test/files/vrn_gtfs_cut.zip
")


# The start param sets a property, that is not defined in the (loaded) yml config,
# but is present as internal default. This default should be overridden, but the
# properties from $CONTAINER_CONF_DIR_USER/ors-config.yml should also be loaded.
podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${orsConfig}":"${CONTAINER_WORK_DIR}/ors-config.yml" \
  "local/${IMAGE}:latest" &
#ok  -Dspring-boot.run.arguments="--ors.engine.profiles.car.enabled=true --ors.engine.source_file=ors-api/src/test/files/heidelberg.osm.gz" &
#ok:  -Dspring-boot.run.arguments='--ors.engine.profiles.car.enabled=true --ors.engine.source_file=ors-api/src/test/files/heidelberg.osm.gz' &
#nok:  -Dspring-boot.run.arguments='ors.engine.profiles.car.enabled=true ors.engine.source_file=ors-api/src/test/files/heidelberg.osm.gz' &

awaitOrsReady 60 "${HOST_PORT}"
profiles=$(requestEnabledProfiles $HOST_PORT)
cleanupTest

assertEquals "public-transport" "$profiles"
