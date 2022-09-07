#!/usr/bin/env bash

graphs=/ors-core/data/graphs
tomcat_ors_config=/usr/local/tomcat/webapps/ors/WEB-INF/classes/ors-config.json
source_ors_config=/ors-core/openrouteservice/src/main/resources/ors-config.json
graphs_tar=/ors-core/data/pre-built/graph.tar.xz
graphs_md5=/ors-core/data/pre-built/graphs.md5
ors_war=/ors-core/data/pre-built/ors.war
do_extract_graphs=False

##### Catalina and Java options -----------------------------------------------------------

if [ -z "${CATALINA_OPTS}" ]; then
	export CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost"
fi

if [ -z "${JAVA_OPTS}" ]; then
	export JAVA_OPTS="-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g"
fi

echo "CATALINA_OPTS=\"$CATALINA_OPTS\"" > /usr/local/tomcat/bin/setenv.sh
echo "JAVA_OPTS=\"$JAVA_OPTS\"" >> /usr/local/tomcat/bin/setenv.sh

##### Load pre-built graphs ----------------------------------------------------------------

if [ "${BUILD_GRAPHS}" = "True" ] && [ "${USE_PREBUILT}" = "True" ] ; then
	echo "Variables BUILD_GRAPHS and USE_PREBUILT in docker-compose.yml cannot both be set to 'True'."
	exit 1
fi

if [ "${BUILD_GRAPHS}" = "True" ] ; then
  echo "### New graphs will be built. Old ones are deleted. ###"
  rm -rf ${graphs}/*
  rm -rf ${graphs_md5}
  rm -rf /usr/local/tomcat/webapps/ors/*
  rm -f /usr/local/tomcat/webapps/ors.war
elif [ "${USE_PREBUILT}" = "True" ]; then
  echo "### Loading pre-built graphs ###"
  # Check if graphs.tar.xz exists, if not exit.
	if [ -f "${graphs_tar}" ]; then
	  echo "Found pre-built graphs: "${graphs_tar}""
  else
    echo ""${graphs_tar}" not found. Please add it to the directory /pre-built."
    exit 1
  fi
  # Check if graphs directory is empty
	subdircount=$(find ${graphs} -maxdepth 1 -type d | wc -l)
	if [[ "$subdircount" -eq 1 ]]; then
	  echo "Directory 'graphs' is empty."
	  do_extract_graphs=True
	else
    # Check if md5sum file exists
    if [ -f "${graphs_md5}" ]; then
      echo "Found MD5 sum of graphs: "${graphs_md5}". Checking whether graphs are up-to-date..."
      # check = md5deep -X directory_hash.md5 -r /path/to/your/second/direcotory
      if md5deep -X ${graphs_md5} -r ${graphs} ; then
        echo "Current graphs are up-to-date. Nothing to do."
        do_extract_graphs=False
      else
        echo "Current graphs are out of date. "${graphs_md5}" will be extracted."
        do_extract_graphs=True
      fi
    else
        echo "No valid md5 sum of graphs folder found. "${graphs_tar}" will be extracted."
        do_extract_graphs=True
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
    md5deep -rel "${graphs}/" > "${graphs_md5}"
  fi
fi


##### Load pre-built ors.war ----------------------------------------------------------------

if [ "${USE_PREBUILT}" = "True" ] ; then
  echo "### Loading pre-built ors.war file ###"
  # Check if ors.war file exists, if not exit.
  if [ -f "${graphs_tar}" ]; then
    echo "Found "${ors_war}""
  else
    echo ""${ors_war}" file not found. Please add it to the ./pre-built volume."
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

  # Search for config file in ors.war
  rm -f ./ors-config.json
  unzip -u -o -j ${ors_war} "*ors-config.json*"
  if [ ! -f "./ors-config.json" ]; then #/usr/local/tomcat/webapps/ors/WEB-INF/classes
    rm -f ./app.config
    unzip -u -o -j ${ors_war} "*app.config*"
    if [ ! -f "./app.config" ]; then
      echo "No valid configuration file found. Please make sure that the ors.war file contains a valid ors-config.json or app.config file."
      exit 1
    else
      echo "Found app.config file."
      mv -f ./app.config ./ors-config.json
    fi
  else
    echo "Found ors.config.json file."
  fi

  # Replace paths in ors-config.json to match docker setup
  jq '.ors.services.routing.profiles.default_params.elevation_cache_path = "data/elevation_cache"' ./ors-config.json |sponge ./ors-config.json
  jq '.ors.services.routing.profiles.default_params.graphs_root_path = "data/graphs"' ./ors-config.json |sponge ./ors-config.json
  # init_threads = 1, > 1 been reported some issues
  jq '.ors.services.routing.init_threads = 1' ./ors-config.json |sponge ./ors-config.json
  # Delete all profiles but car
  jq 'del(.ors.services.routing.profiles.active[1,2,3,4,5,6,7,8])' ./ors-config.json |sponge ./ors-config.json
  cp -f ./ors-config.json ${tomcat_ors_config}
fi

#### Compile ors if necessary  -------------------------------------------------------------

echo "### Openrouteservice configuration ###"

# if Tomcat built before, copy the mounted ors-config.json to the Tomcat webapp ors-config.json, else copy it from the source
if [ -d "/usr/local/tomcat/webapps/ors" ]; then
  echo "Tomcat already built: Copying /ors-conf/ors-config.json to tomcat webapp folder"
  if [ -f /ors-conf/ors-config.json ]; then
    cp -f /ors-conf/ors-config.json ${tomcat_ors_config}
  fi
else
  if [ ! -f /ors-conf/ors-config.json ]; then
    echo "No ors-config.json in ors-conf folder. Copy config from ${source_ors_config}"
    cp -f $source_ors_config /ors-conf/ors-config.json
  else
    echo "ors-config.json exists in ors-conf folder. Copy config to ${source_ors_config}"
    cp -f /ors-conf/ors-config.json $source_ors_config
  fi
  echo "### Packaging openrouteservice and deploying to Tomcat ###"
  mvn -q -f /ors-core/openrouteservice/pom.xml package -DskipTests && \
  cp -f /ors-core/openrouteservice/target/*.war /usr/local/tomcat/webapps/ors.war
fi

# Run tomcat
/usr/local/tomcat/bin/catalina.sh run

# Keep docker running easy
exec "$@"
