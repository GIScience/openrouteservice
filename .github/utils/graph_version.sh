#!/bin/bash
# Prints the graph version a running openrouteservice instance declares.
# /ors/v2/status answers before the profiles are ready, so this does not wait for
# a graph build.
host=${1}
port=${2}
timeout=${3}
# default to 2 sec sleep timer
sleep=${4:-2}

function wait_for_graph_version() {
  local url="$1"
  local timeout_sec="$2"
  local sleep="$3"

  start_time=$(date +%s)
  while true; do
    version=$(curl -s "${url}" | jq -r '.engine.graph_version // empty')
    if [[ -n "${version}" ]]; then
      echo "${version}"
      return 0
    fi
    current_time=$(date +%s)
    elapsed_time=$((current_time - start_time))
    if ((elapsed_time >= timeout_sec)); then
      echo "Timed out waiting for engine.graph_version after ${timeout_sec} seconds for ${url}" >&2
      return 1
    fi
    sleep "$sleep"
  done
}

wait_for_graph_version "$host:$port/ors/v2/status" "${timeout}" "${sleep}"
