#!/bin/bash
# This script sets environment variables for the openrouteservice systemd webapp
# There is no other way to pass environment variables to the webapp
export CATALINA_OPTS="$CATALINA_OPTS \
-Xms100M \
-Xmx600M \
-server \
-XX:+UseParallelGC\
"

export JAVA_OPTS="$JAVA_OPTS -Dors.engine.source_file=/home/ors/files/heidelberg.osm.gz"
