TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
CONTAINER=$(removeExtension "$(basename $0)")
HOST_PORT=$(findFreePort 8082)
runType=$1
case $runType in
  jar) IMAGE=$IMAGE_NAME_JAR;;
  mvn) IMAGE=$IMAGE_NAME_MVN;;
esac

# If there is no yml config and also no start parameter
# enabling a routing profile, ORS cannot start
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 local/"$IMAGE":latest  &

# expect process finished timout
res=$(expectOrsStartupFails 60 "$CONTAINER" )
# stop container if was not finished
podman stop "$CONTAINER"

assertEquals "terminated" "$res"
