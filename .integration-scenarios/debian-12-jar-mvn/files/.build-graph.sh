# Called from tests/build-graph-*.sh

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
callingScript=$1
runType=$2
image=$3
enabledProfile=$4
expectedProfiles=$5
prepareTest $callingScript $runType $image

orsConfig=$(makeTempFile $callingScript "\
ors:
  engine:
    source_file:  ors-api/src/test/files/heidelberg.osm.gz
    profiles:
      ${enabledProfile}:
        enabled: true")

podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${orsConfig}":"${CONTAINER_WORK_DIR}/ors-config.yml" \
  "local/${IMAGE}:latest" &

awaitOrsReady 60 "${HOST_PORT}"
profiles=$(requestEnabledProfiles $HOST_PORT)
cleanupTest

assertEquals "$expectedProfiles" "$profiles"
