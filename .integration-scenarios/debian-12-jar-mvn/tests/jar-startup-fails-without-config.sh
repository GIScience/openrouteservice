TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
CONTAINER=$(removeExtension "$(basename $0)")
HOST_PORT=$(findFreePort 8082)

# do not set params to enable a routing profile
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 local/"$IMAGE_NAME_JAR":latest  &

# expect process finished timout
res=$(expectOrsStartupFails 60 "$CONTAINER" )
# stop container if was not finished
podman stop "$CONTAINER"

assertEquals "terminated" "$res"
