TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $1 $(basename $0)

# If there is no yml config and also no start parameter
# enabling a routing profile, ORS cannot start
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  -v "$TESTROOT"/graphs_volume:$WORK_DIR/graphs \
  local/"$IMAGE":latest  &

# expect process finished timout
res=$(expectOrsStartupFails 60 "$CONTAINER" )
# stop container if was not finished
podman stop "$CONTAINER"

assertEquals "terminated" "$res"