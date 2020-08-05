#!/usr/bin/env bash

graphs=/ors-core/data/graphs
tomcat_appconfig=/usr/local/tomcat/webapps/ors/WEB-INF/classes/app.config
source_appconfig=/ors-core/openrouteservice/src/main/resources/app.config

if [ -z "${CATALINA_OPTS}" ]; then
	export CATALINA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost"
fi

if [ -z "${JAVA_OPTS}" ]; then
	export JAVA_OPTS="-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g"
fi

echo "CATALINA_OPTS=\"$CATALINA_OPTS\"" > /usr/local/tomcat/bin/setenv.sh
echo "JAVA_OPTS=\"$JAVA_OPTS\"" >> /usr/local/tomcat/bin/setenv.sh

if [ "${BUILD_GRAPHS}" = "True" ]; then
  rm -rf ${graphs}/*
fi

# if Tomcat built before, copy the mounted app.config to the Tomcat webapp app.config, else copy it from the source
if [ -d "/usr/local/tomcat/webapps/ors" ]; then
	cp -f /ors-conf/app.config $tomcat_appconfig
else
	if [ ! -f /ors-conf/app.config ]; then
		cp -f $source_appconfig /ors-conf/app.config
	fi
	echo "### Package openrouteservice and deploy to Tomcat ###"
	mvn -q -f /ors-core/openrouteservice/pom.xml package -DskipTests && \
	cp -f /ors-core/openrouteservice/target/*.war /usr/local/tomcat/webapps/ors.war
fi

/usr/local/tomcat/bin/catalina.sh run

# Keep docker running easy
exec "$@"
