#!/usr/bin/env bash

echo "Running container as user $(whoami) with id $(id -u)"

ors_base=${1}
catalina_base=${ors_base}/tomcat
echo "ORS Path: ${ors_base}"
echo "Catalina Path: ${catalina_base}"

graphs=${ors_base}/ors-core/data/graphs
tomcat_ors_config=${catalina_base}/webapps/ors/WEB-INF/classes/ors-config.json
source_ors_config=${ors_base}/ors-core/ors-config.json
public_ors_config=${ors_base}/ors-conf/ors-config.json

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
# if Tomcat built before, copy the mounted ors-config.json to the Tomcat webapp ors-config.json, else copy it from the source
if [ -d "${catalina_base}/webapps/ors" ]; then
  echo "Tomcat already built: Copying ${ors_base}/ors-conf/ors-config.json to tomcat webapp folder"
  cp -f "${public_ors_config}" "${tomcat_ors_config}"
else
  if [ ! -f "$public_ors_config" ]; then
    echo "No ors-config.json in ors-conf folder. Copying config from ${source_ors_config}"
    mkdir -p "${ors_base}"/ors-conf
    cp -f "${source_ors_config}" "${public_ors_config}"
  else
    echo "ors-config.json exists in ors-conf folder. Copying config to ${source_ors_config}"
    cp -f "${public_ors_config}" "${source_ors_config}"
  fi
  echo "### Package openrouteservice and deploy to Tomcat ###"
  cp -f "${ors_base}"/ors-core/ors.war "${catalina_base}"/webapps/ors.war
fi

# so docker can stop the process gracefully
exec "${catalina_base}"/bin/catalina.sh run
