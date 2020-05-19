# ORS multi stage build
# 1. Stage: Build ORS
FROM openjdk:8-jdk as build
ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"

ARG APP_CONFIG=./docker/conf/app.config.sample

WORKDIR /ors-core

COPY openrouteservice /ors-core/openrouteservice
# Clean old config file
RUN rm -f /ors-cored/openrouteservice/src/main/resources/app.config
# Copy clean config file
COPY $APP_CONFIG ./openrouteservice/src/main/resources/app.config

# Install tomcat
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v8.0.32/bin/apache-tomcat-8.0.32.tar.gz -O /tmp/tomcat.tar.gz && \
    cd /tmp && \
    tar xvfz tomcat.tar.gz && \
    mkdir /usr/local/tomcat && \
    cp -R /tmp/apache-tomcat-8.0.32/* /usr/local/tomcat/ && \
    cd /ors-core && \
    apt-get update -qq && apt-get install -qq -y locales nano maven && \
    locale-gen en_US.UTF-8 && \
    mvn -q -f /ors-core/openrouteservice/pom.xml package -DskipTests && \
    cp -f /ors-core/openrouteservice/target/*.war /usr/local/tomcat/webapps/ors.war && \
    mkdir /ors-conf

# 2. Stage: Copy the app only
FROM openjdk:8-jdk

ARG OSM_FILE=./docker/data/heidelberg.osm.gz
COPY $OSM_FILE /ors-core/data/osm_file.pbf

RUN apt-get update -qq && apt-get install -qq -y locales nano maven && \
    mkdir -p  /usr/local/tomcat/
COPY --from=build /usr/local/tomcat/ /usr/local/tomcat/
COPY --from=build /ors-core/openrouteservice/src/main/resources/app.config /ors-core/openrouteservice/src/main/resources/app.config
COPY ./docker-entrypoint.sh /docker-entrypoint.sh

WORKDIR /ors-core

# Start the container
EXPOSE 8080
ENTRYPOINT ["/bin/bash", "/docker-entrypoint.sh"]
