#!/usr/bin/env bash

graphs=/ors-core/data/graphs
tomcat_ors_config=/usr/local/tomcat/webapps/ors/WEB-INF/classes/ors-config.json
source_ors_config=/ors-core/openrouteservice/src/main/resources/ors-config.json
graphs_tar=/ors-core/data/pre-built/graphs.tar.xz
graphs_md5=/ors-core/data/pre-built/graphs.md5
ors_war=/ors-core/data/pre-built/ors.war
do_extract_graphs=False

if [ -z "${CATALINA_OPTS}" ]; then
	export CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost"
fi

if [ -z "${JAVA_OPTS}" ]; then
	export JAVA_OPTS="-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g"
fi

echo "CATALINA_OPTS=\"$CATALINA_OPTS\"" > /usr/local/tomcat/bin/setenv.sh
echo "JAVA_OPTS=\"$JAVA_OPTS\"" >> /usr/local/tomcat/bin/setenv.sh

##### Load pre-built graphs ----------------------------------------------------------------

if [ "${BUILD_GRAPHS}" = "True" ] && [ "${LOAD_GRAPHS}" = "True" ] ; then
	echo "Variables BUILD_GRAPHS and LOAD_GRAPHS in docker-compose.yml cannot both be set to 'True'."
	exit 1
fi

if [ "${BUILD_GRAPHS}" = "True" ] ; then
  rm -rf ${graphs}/*
elif [ "${LOAD_GRAPHS}" = "True" ]; then
  # Check if compressed graphs (graphs.tar.xz) exists, if not exit.
	if [ -f "${graphs_tar}" ]; then
	  echo "Found "${graphs_tar}""
  else
    echo ""${graphs_tar}" not found. Please add it to the directory /pre-built."
    exit 1
  fi
  # Check if graphs directory is empty
	subdircount=$(find ${graphs} -maxdepth 1 -type d | wc -l)
	echo $subdircount
	if [[ "$subdircount" -eq 1 ]]; then
	  echo "Directory 'graphs' is empty."
	  do_extract_graphs=True
	else
    # Check if md5sum file exists
    if [ -f "${graphs_md5}" ]; then
      echo "MD5 sum found. Checking for validity of existing graphs."
      if md5sum -c ${graphs_md5}; then
        echo "Current graphs are up-to-date. Nothing to do."
        do_extract_graphs=False
      else
        echo "Current graphs are out of date. Starting fresh."
        do_extract_graphs=True
      fi
    else
        echo "No valid md5 sum. Starting fresh."
        do_extract_graphs=True
    fi
  fi
fi

# Extract graphs if necessary
if [ "${do_extract_graphs}" = "True" ] ; then
  echo "Extracting graphs. This may take a while."
  rm -rf ${graphs}/*
  rm -rf ${graphs_md5}
  tar -xf ${graphs_tar} -C ${graphs}
  mv -f /ors-core/data/graphs/graphs/* ${graphs}
  rm -rf /ors-core/data/graphs/graphs
  md5sum ${graphs_tar} > ${graphs_md5}
fi

##### Load pre-built ors.war ----------------------------------------------------------------

echo "### Load pre-compiled ors.war file ###"

if [ "${LOAD_ORS_WAR}" = "True" ] ; then
  # Check if ors.war file exists, if not exit.
  if [ -f "${graphs_tar}" ]; then
    echo "Found "${ors_war}""
  else
    echo ""${ors_war}" not found. Please add it to the directory /pre-built."
    exit 1
  fi
  echo "Extracting ors.war file"
  # Delete old files
  rm -rf /usr/local/tomcat/webapps/ors/*
  rm -f /usr/local/tomcat/webapps/ors.war
  # Copy ors.war
  cp ${ors_war} /usr/local/tomcat/webapps
  # Extract ors.war file
  mkdir /usr/local/tomcat/webapps/ors
  unzip -q ${ors_war} -d /usr/local/tomcat/webapps/ors
  cp ${tomcat_ors_config} /ors-conf/ors-config-from-ors-war.json
  # Replace paths in ors-config.json to match docker setup
  jq '.ors.services.routing.sources[0] = "data/osm_file.pbf"' ${tomcat_ors_config} |sponge ${tomcat_ors_config}
  jq '.ors.services.routing.profiles.default_params.elevation_cache_path = "data/elevation_cache"' ${tomcat_ors_config} |sponge ${tomcat_ors_config}
  jq '.ors.services.routing.profiles.default_params.graphs_root_path = "data/graphs"' ${tomcat_ors_config} |sponge ${tomcat_ors_config}
  # init_threads = 1, > 1 been reported some issues
  jq '.ors.services.routing.init_threads = 1' ${tomcat_ors_config} |sponge ${tomcat_ors_config}
  # Delete all profiles but car
  #jq 'del(.ors.services.routing.profiles.active[1,2,3,4,5,6,7,8])' ${tomcat_ors_config} |sponge ${tomcat_ors_config}
  cp ${tomcat_ors_config} /ors-conf/ors-config-adjusted.json
fi

#### Compile ors if necessary  -------------------------------------------------------------

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
  echo "### Package openrouteservice and deploy to Tomcat ###"
  mvn -q -f /ors-core/openrouteservice/pom.xml package -DskipTests && \
  cp -f /ors-core/openrouteservice/target/*.war /usr/local/tomcat/webapps/ors.war
fi

# Run tomcat
/usr/local/tomcat/bin/catalina.sh run

# Keep docker running easy
exec "$@"
