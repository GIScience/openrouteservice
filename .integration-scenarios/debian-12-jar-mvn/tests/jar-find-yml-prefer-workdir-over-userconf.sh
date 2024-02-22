TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
CONTAINER=$(removeExtension "$(basename $0)")
HOST_PORT=$(findFreePort 8082)

podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  -v "$TESTROOT"/files/config-car.yml:$WORK_DIR/ors-config.yml \
  -v "$TESTROOT"/files/config-hgv.yml:$CONF_DIR_USER/ors-config.yml \
  local/"$IMAGE_NAME_JAR":latest  &

awaitOrsReady 30 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals "driving-car" "$profiles"
