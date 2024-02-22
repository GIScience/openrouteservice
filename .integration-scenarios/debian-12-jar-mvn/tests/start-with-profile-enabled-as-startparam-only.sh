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

# even if no yml config file is present the ors is startable
# if at least one routing profile is enabled with a start parameter
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  local/"$IMAGE":latest \
  $(getProgramArguments $runType --ors.engine.profiles.hgv.enabled=true) &

awaitOrsReady 60 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals "driving-hgv" "$profiles"
