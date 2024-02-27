#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
$TESTROOT/files/.build-graph.sh $1 $(basename $0) "bike-road" "cycling-road"