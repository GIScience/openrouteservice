FROM maven:3.8-jdk-11-slim as build

ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"

ARG ORS_CONFIG=./openrouteservice/src/main/resources/ors-config-sample.json
ARG OSM_FILE=./openrouteservice/src/main/files/heidelberg.osm.gz
ENV BUILD_GRAPHS="False"
ARG TOMCAT_VERSION=8.5.69

WORKDIR /ors-core

# Install dependencies and locales
RUN apt-get update -qq && \
    apt-get install -qq -y nano moreutils jq wget

COPY openrouteservice /ors-core/openrouteservice
COPY $OSM_FILE /ors-core/data/osm_file.pbf
COPY $ORS_CONFIG /ors-core/openrouteservice/src/main/resources/ors-config-sample.json

# Install tomcat
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -O /tmp/tomcat.tar.gz && \
    cd /tmp && \
    tar xvfz tomcat.tar.gz && \
    mkdir /usr/local/tomcat && \
    cp -R /tmp/apache-tomcat-${TOMCAT_VERSION}/* /usr/local/tomcat/ && \
    rm -r /tmp/tomcat.tar.gz /tmp/apache-tomcat-${TOMCAT_VERSION}

# Configure ors config:
# - Replace paths in ors-config.json to match docker setup
# - init_threads = 1, > 1 been reported some issues
# - Delete all profiles but car
RUN cp /ors-core/openrouteservice/src/main/resources/ors-config-sample.json /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.sources[0] = "data/osm_file.pbf"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.profiles.default_params.elevation_cache_path = "data/elevation_cache"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.profiles.default_params.graphs_root_path = "data/graphs"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.init_threads = 1' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq 'del(.ors.services.routing.profiles.active[1,2,3,4,5,6,7,8])' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json

RUN mvn -f /ors-core/openrouteservice/pom.xml package -DskipTests

# build final image, just copying stuff inside
FROM openjdk:11.0-jre-slim

WORKDIR /ors-core

COPY --from=build /ors-core/openrouteservice/target/ors.war .
COPY --from=build /ors-core/openrouteservice/src/main/resources/ors-config.json .
COPY --from=build /usr/local/tomcat /usr/local/tomcat
COPY ./docker-entrypoint.sh .

# Start the container
EXPOSE 8080
CMD ["bash", "-c", "/ors-core/docker-entrypoint.sh"]
