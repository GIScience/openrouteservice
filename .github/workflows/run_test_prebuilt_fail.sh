#!/usr/bin/env bash
# Tests whether ors is set up using pre-built graphs and ors.war file.
# Test succeeds if ors is ready within maximum number of tries.
# Success: exit 0, Fail: exit 1

STDOUT=$(docker run -p 127.0.0.1:8080:8080/tcp -v /Users/chludwig/Development/ORS/original_ors/openrouteservice/docker/graphs:/ors-core/data/graphs -v /Users/chludwig/Development/ORS/original_ors/openrouteservice/docker/elevation_cache:/ors-core/data/elevation_cache --name ors-test-container-2 ors-test-2)
EXPECTED_ERROR_MESSAGE='/ors-core/data/pre-built/graph.tar.xz not found. Please add it to the directory /pre-built.'

if [[ "$STDOUT" == *"$EXPECTED_ERROR_MESSAGE"* ]]; then
  echo "Test successful: Running container failed with correct error message."
  docker rm ors-test-container-2
  exit 0
else
  echo "Test failed: Wrong error message."
  exit 1
fi
