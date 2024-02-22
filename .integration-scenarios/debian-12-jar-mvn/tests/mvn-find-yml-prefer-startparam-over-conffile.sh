TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
CONTAINER=$(removeExtension "$(basename $0)")
HOST_PORT=$(findFreePort 8082)

# Although the config file $WORK_DIR/ors-config.yml exists, it should not be used,
# but instead the config file specified as start parameter (first positional parameter)
podman run --replace --name "$CONTAINER" -p $HOST_PORT:8082 \
  -v "$TESTROOT"/files/config-car.yml:"$WORK_DIR/ors-config.yml" \
  -v "$TESTROOT"/files/config-hgv.yml:"$WORK_DIR/config-hgv.yml" \
  local/"$IMAGE_NAME_MVN":latest \
  "-Dspring-boot.run.arguments='$WORK_DIR/config-hgv.yml'" &

awaitOrsReady 60 $HOST_PORT
profiles=$(requestEnabledProfiles $HOST_PORT)
podman stop "$CONTAINER"

assertEquals "driving-hgv" "$profiles"
