#!/usr/bin/env bash

echo "Running container as user $(whoami) with id $(id -u)"

if [[ -d /ors-core ]] || [[ -d /ors-conf ]]; then
  echo "You're mounting old paths. Remove them and migrate to the new docker setup: https://giscience.github.io/openrouteservice/run-instance/installation/running-with-docker"
  echo "Exit setup due to old folders /ors-core or /ors-conf being mounted"
  sleep 5
  exit 1
fi

ors_base=${1}
catalina_base=${ors_base}/tomcat
graphs=${ors_base}/ors-core/data/graphs

echo "ORS Path: ${ors_base}"
echo "Catalina Path: ${catalina_base}"


if [ -z "${CATALINA_OPTS}" ]; then
  export CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost"
fi

if [ -z "${JAVA_OPTS}" ]; then
  export JAVA_OPTS="-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g"
fi

{
  echo "CATALINA_BASE=\"${catalina_base}\""
  echo "CATALINA_HOME=\"${catalina_base}\""
  echo "CATALINA_PID=\"${catalina_base}/temp/tomcat.pid\""
  echo "CATALINA_OPTS=\"${CATALINA_OPTS}\""
  echo "JAVA_OPTS=\"${JAVA_OPTS}\""
} >"${catalina_base}"/bin/setenv.sh

if [ "${BUILD_GRAPHS}" = "True" ]; then
  rm -rf "${graphs:?}"/*
fi

echo "### openrouteservice configuration ###"
# Always overwrite the example config in case another one is present
cp -f "${ors_base}/tmp/ors-config.yml" "${ors_base}/ors-conf/ors-config-example.yml"
# Check for old .json configs
JSON_FILES=$(ls -d -- "${ors_base}/ors-conf/"*.json 2>/dev/null)
if [ -n "$JSON_FILES" ]; then
    echo "Old .json config found. They're deprecated and will be replaced in ORS version 8."
    echo "Please migrate to the new .yml example."
fi
# No config found. Use the base config
if [ ! -f "${ors_base}/ors-conf/ors-config.yml" ]; then
  echo "Copy ors-config.yml"
  cp -f "${ors_base}/tmp/ors-config.yml" "${ors_base}/ors-conf/ors-config.yml"
fi

if [ ! -f "${ors_base}/ors-core/data/osm_file.pbf" ]; then
  echo "Copy osm_file.pbf"
  cp -f "${ors_base}/tmp/osm_file.pbf" "${ors_base}/ors-core/data/osm_file.pbf"
fi

# so docker can stop the process gracefully
exec "${catalina_base}"/bin/catalina.sh run
