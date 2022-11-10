#!/usr/bin/env bash

function pbf_extractor() {
  # $1 Input PBF file
  # $2 Output PBF file
  # $3 BBOX
  local input_file=$1
  local output_file=$2
  local bbox=$3
  echo "Cutting the PBF file by bounding box: ${bbox}. This may take some time."
  rm -f "${output_file}"
  osmium extract --bbox "${BBOX}" "${input_file}" --output "${output_file}"
}

graphs=/ors-core/data/graphs
tomcat_ors_config=/usr/local/tomcat/webapps/ors/WEB-INF/classes/ors-config.json
source_ors_config=/ors-core/openrouteservice/src/main/resources/ors-config.json
BBOX="${BBOX:-False}"
bbox_file="${graphs}/bbox.txt"
user_osm_file=/ors-core/osm_file.pbf
osm_file_location=/ors-core/data/osm_file.pbf

if [ -z "${CATALINA_OPTS}" ]; then
  export CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost"
fi

if [ -z "${JAVA_OPTS}" ]; then
  export JAVA_OPTS="-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g"
fi

echo "CATALINA_OPTS=\"$CATALINA_OPTS\"" >/usr/local/tomcat/bin/setenv.sh
echo "JAVA_OPTS=\"$JAVA_OPTS\"" >>/usr/local/tomcat/bin/setenv.sh

if [ "${BUILD_GRAPHS}" = "True" ]; then
  rm -rf ${graphs:?}/*
fi

if [[ -f ${user_osm_file} ]]; then
  echo "Custom PBF file found."
  old_bbox=False
  if [[ -f ${bbox_file} ]]; then
    # Find old bbox
    old_bbox=$(head -n 1 ${bbox_file})
  fi
  # Check if graph folder is empty
  subdircount=$(find ${graphs} -maxdepth 1 -type d | wc -l)
  if [[ "$subdircount" -eq 1 ]]; then
    # If graph folder is empty
    if [ "${BBOX}" != "False" ]; then
      pbf_extractor ${user_osm_file} ${osm_file_location} "${BBOX}"
      echo "${BBOX}" >${bbox_file}
    elif [[ "$subdircount" -eq 1 ]]; then
      echo "Using the raw custom PBF file."
      rm -f ${osm_file_location}
      cp ${user_osm_file} ${osm_file_location}
    fi
  else
    # Graph folder is not empty
    if [ "${BBOX}" != "False" ] && [ "${BBOX}" != "${old_bbox}" ]; then
      # Exception to rebuild graph if bbox changed.
      echo "The BBOX changed from '${old_bbox}' to '${BBOX}'. Extracting new area."
      pbf_extractor ${user_osm_file} ${osm_file_location} "${BBOX}"
      # Remove graphs folder to trigger rebuild.
      rm -rf ${graphs:?}/*
      echo "${BBOX}" >${bbox_file}
    else
      echo "Detected a custom PBF file. In order to rebuild the graph set BUILD_GRAPHS to True."
    fi
  fi
else
  echo "No custom PBF file given. Starting with the default PBF file."
fi

echo "### openrouteservice configuration ###"
# if Tomcat built before, copy the mounted ors-config.json to the Tomcat webapp ors-config.json, else copy it from the source
if [ -d "/usr/local/tomcat/webapps/ors" ]; then
  echo "Tomcat already built: Copying /ors-conf/ors-config.json to tomcat webapp folder"
  cp -f /ors-conf/ors-config.json $tomcat_ors_config
else
  if [ ! -f /ors-conf/ors-config.json ]; then
    echo "No ors-config.json in ors-conf folder. Copy config from ${source_ors_config}"
    cp -f $source_ors_config /ors-conf/ors-config.json
  else
    echo "ors-config.json exists in ors-conf folder. Copy config to ${source_ors_config}"
    cp -f /ors-conf/ors-config.json $source_ors_config
  fi
  # Always set osm_file.pbf as the osm file for the first start.
  jq '.ors.services.routing.sources[0] = "data/osm_file.pbf"' /ors-conf/ors-config.json | sponge /ors-conf/ors-config.json
  echo "### Package openrouteservice and deploy to Tomcat ###"
  mvn -T 1.5C -q -f /ors-core/openrouteservice/pom.xml package -DskipTests &&
    cp -f /ors-core/openrouteservice/target/*.war /usr/local/tomcat/webapps/ors.war
fi

/usr/local/tomcat/bin/catalina.sh run

# Keep docker running easy
exec "$@"
