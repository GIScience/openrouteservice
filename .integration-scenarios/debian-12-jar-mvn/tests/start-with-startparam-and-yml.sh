TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $1 $(basename $0)

# The yml config $CONF_DIR_USER/ors-config.yml should be loaded
# and additionally the default hgv profile should be enabled
# by the start param
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  -v "$TESTROOT"/files/config-car.yml:$CONF_DIR_USER/ors-config.yml \
  local/"$IMAGE":latest \
  $(getProgramArguments $runType --ors.engine.profiles.hgv.enabled=true) &

awaitOrsReady 60 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals 'driving-hgv driving-car' "$profiles"
