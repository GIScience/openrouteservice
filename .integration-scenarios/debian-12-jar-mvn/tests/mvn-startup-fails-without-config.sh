TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
CONTAINER=$(removeExtension "$(basename $0)")
HOST_PORT=$(findFreePort 8082)

# If there is no yml config and also no start parameter
# enabling a routing profile, ORS cannot start
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  local/"$IMAGE_NAME_MVN":latest &

# expect process finished before timeout
res=$(expectOrsStartupFails 60 $CONTAINER)
podman stop "$CONTAINER"

assertEquals "terminated" "$res"
