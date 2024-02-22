TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $1 $(basename $0)

# Even if no yml config file is present, the ors is runnable
# if at least one routing profile is enabled with a start parameter.
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  local/"$IMAGE":latest \
  $(getProgramArguments $runType --ors.engine.profiles.hgv.enabled=true) &

awaitOrsReady 60 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals "driving-hgv" "$profiles"
