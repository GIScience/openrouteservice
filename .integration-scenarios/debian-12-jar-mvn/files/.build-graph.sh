# Called from tests/build-graph-*.sh

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
runType=$1
callingScript=$2
enabledProfile=$3
expectedProfiles=$4
prepareTest $runType $callingScript

orsConfig=$(makeTempFile $callingScript "\
ors:
  engine:
    source_file:  ors-api/src/test/files/heidelberg.osm.gz
    profiles:
      ${enabledProfile}:
        enabled: true")


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

assertEquals "$expectedProfiles" "$profiles"
