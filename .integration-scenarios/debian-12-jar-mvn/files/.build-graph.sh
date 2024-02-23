# Called from tests/build-graph-*.sh

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
callingScript=$2
yml=$3
expectedProfiles=$4
prepareTest $1 $callingScript

# The start param sets a property, that is not defined in the (loaded) yml config,
# but is present as internal default. This default should be overridden, but the
# properties from $CONTAINER_CONF_DIR_USER/ors-config.yml should also be loaded.
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  -v "$TESTROOT"/graphs_volume:$CONTAINER_WORK_DIR/graphs \
  -v "$TESTROOT"/files/"$yml":$CONTAINER_WORK_DIR/ors-config.yml \
  local/"$IMAGE":latest &
#ok  -Dspring-boot.run.arguments="--ors.engine.profiles.car.enabled=true --ors.engine.source_file=ors-api/src/test/files/heidelberg.osm.gz" &
#ok:  -Dspring-boot.run.arguments='--ors.engine.profiles.car.enabled=true --ors.engine.source_file=ors-api/src/test/files/heidelberg.osm.gz' &
#nok:  -Dspring-boot.run.arguments='ors.engine.profiles.car.enabled=true ors.engine.source_file=ors-api/src/test/files/heidelberg.osm.gz' &

awaitOrsReady 60 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals "$expectedProfiles" "$profiles"
