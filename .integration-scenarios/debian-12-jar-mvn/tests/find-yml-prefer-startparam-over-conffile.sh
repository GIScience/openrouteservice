TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $1 $(basename $0)

# Although the config file $WORK_DIR/ors-config.yml exists, it should not be used,
# but instead the config file specified as start parameter (first positional parameter)
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  -v "$TESTROOT"/files/config-car.yml:$WORK_DIR/ors-config.yml \
  -v "$TESTROOT"/files/config-hgv.yml:$WORK_DIR/config-hgv.yml \
  local/"$IMAGE":latest \
  $(getProgramArguments $runType $WORK_DIR/config-hgv.yml) &

awaitOrsReady 60 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals "driving-hgv" "$profiles"
