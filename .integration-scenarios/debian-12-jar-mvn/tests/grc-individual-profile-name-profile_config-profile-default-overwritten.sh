#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
PROJECTROOT="$( cd "$(dirname "$0")"/../../.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $(basename $0) $*

tempdir=$(makeTempDir $(basename $0))
graphs_path="${tempdir}/graphs"
mkdir $graphs_path
configCar=$(writeToFile $tempdir ors-config.yml "\
ors:
  endpoints:
    isochrones:
      maximum_intervals: 2
  engine:
    profile_default:
      enabled: false
      graph_path: graphs
      source_file:
      # These properties are defined in the graph_info.yml from the repository.
      # Here, all values are overwritten, but when the graph is loaded, the values delivered with the graph should be used, e.g. should overwrite the values from here:
      elevation: false
      elevation_smoothing: false
      encoder_flags_size: 0
      instructions: false
      optimize: true
      traffic: true
      interpolate_bridges_and_tunnels: false
      force_turn_costs: true
      location_index_resolution: 0
      location_index_search_iterations: 0
      encoder_options:
        block_fords: true
        consider_elevation: true
        turn_costs: false
        use_acceleration: false
        conditional_access: true
        conditional_speed: true
      preparation:
        min_network_size: 0
        min_one_way_network_size: 0
        methods:
          ch:
            threads: 0
            weightings: shortest
            enabled: false
          lm:
            threads: 0
            weightings: "shortest,fastest"
            landmarks: 0
            enabled: true
          core:
            threads: 0
            weightings: "shortest,fastest"
            landmarks: 0
            lmsets: railways
            enabled: false
          fastisochrones:
            threads: 0
            weightings: "fastest"
            enabled: true
      execution:
        methods:
          lm:
            active_landmarks: 0
          core:
            active_landmarks: 0
      ext_storages:
        Tollways:
          enabled: false
        WayCategory:
          enabled: false
        WaySurfaceType:
          enabled: false
        HeavyVehicle:
          enabled: false
          restrictions: false
        RoadAccessRestrictions:
          enabled: false
          use_for_warnings: false
    graph_management:
      enabled: true
    profiles:
      bobby-car:
        enabled: true
        encoder_name: driving-car
        repo:
          repository_uri: '/home/ors/graph_repo'
          repository_name: vendor-xyz
          repository_profile_group: fastisochrones
          graph_extent: heidelberg
")

# The test asserts that ORS uses the profile properties coming from the repo and not the ones from the config file (profile_default),
# which are configured with stupid values.
# All kinds of requests should work fine.
# 'source_file' is set to null, to avoid the graph is built locally.

podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${graphs_path}":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${PROJECTROOT}/ors-engine/src/test/resources/test-filesystem-repos":"/home/ors/graph_repo" \
  -v "${configCar}":${CONTAINER_WORK_DIR}/ors-config.yml \
  "local/${IMAGE}:latest" &

awaitOrsReady 60 "${HOST_PORT}"

profiles=$(requestEnabledProfiles ${HOST_PORT})

assertEquals "bobby-car" "${profiles}"

sendSeveralRequestsExpecting200 ${HOST_PORT} "bobby-car" ${tempdir}
