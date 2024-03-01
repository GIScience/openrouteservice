#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
$TESTROOT/files/.build-graph.sh $(basename $0) $1 $2 "bike-electric" "cycling-electric"