TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $1 $(basename $0)

# Even if no yml config file is present, the ors is runnable
# if at least one routing profile is enabled with a environment variable.
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  -v "$TESTROOT"/graphs_volume:$CONTAINER_WORK_DIR/graphs \
  --env ORS_ENGINE_PROFILES_HGV_ENABLED=true \
  local/"$IMAGE":latest &

awaitOrsReady 60 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals "driving-hgv" "$profiles"
