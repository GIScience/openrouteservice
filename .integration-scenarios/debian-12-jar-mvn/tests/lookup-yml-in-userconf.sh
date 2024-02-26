TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $1 $(basename $0)

podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  -v "$M2_FOLDER":/root/.m2 \
  -v "$TESTROOT"/graphs_volume:$CONTAINER_WORK_DIR/graphs \
  -v "$TESTROOT"/files/config-car.yml:$CONTAINER_CONF_DIR_USER/ors-config.yml \
  local/"$IMAGE":latest &

awaitOrsReady 60 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals "driving-car" "$profiles"
