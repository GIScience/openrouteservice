TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $1 $(basename $0)

# The profile configured as run argument should be preferred over environment variable.
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  -v "$TESTROOT"/graphs_volume:$WORK_DIR/graphs \
  -v "$TESTROOT"/files/config-car.yml:$WORK_DIR/config-car.yml \
  -v "$TESTROOT"/files/config-hgv.yml:$WORK_DIR/config-hgv.yml \
  --env ORS_CONFIG_LOCATION=$WORK_DIR/config-hgv.yml \
  local/"$IMAGE":latest \
  $(getProgramArguments $runType $WORK_DIR/config-car.yml) &

awaitOrsReady 60 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals "driving-car" "$profiles"
