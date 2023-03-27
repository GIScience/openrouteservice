# Image is reused in the workflow builds for master and the latest version
FROM maven:3.8-openjdk-17-slim as base

ARG DEBIAN_FRONTEND=noninteractive


# hadolint ignore=DL3002
USER root

# Install dependencies and locales
# hadolint ignore=DL3008
RUN apt-get update -qq && \
    apt-get install -qq -y --no-install-recommends nano moreutils jq wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

FROM base as tomcat
ARG TOMCAT_VERSION=8.5.69

# hadolint ignore=DL3002
USER root

WORKDIR /tmp
# Prepare tomcat
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -O /tmp/tomcat.tar.gz && \
    tar xf tomcat.tar.gz && \
    mv /tmp/apache-tomcat-${TOMCAT_VERSION}/ /tmp/tomcat


FROM base as build

# hadolint ignore=DL3002
USER root

ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"

ARG ORS_CONFIG=openrouteservice/src/main/resources/ors-config-sample.json

WORKDIR /ors-core

COPY openrouteservice/src /ors-core/openrouteservice/src
COPY openrouteservice/WebContent /ors-core/openrouteservice/WebContent
COPY openrouteservice/pom.xml /ors-core/openrouteservice/pom.xml
COPY $ORS_CONFIG /ors-core/openrouteservice/src/main/resources/ors-config-sample.json

# Configure ors config:
# Fist set pipefail to -c to allow intermediate pipes to throw errors
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# - Replace paths in ors-config.json to match docker setup
# - init_threads = 1, > 1 been reported some issues
# - Delete all profiles but car
RUN cp /ors-core/openrouteservice/src/main/resources/ors-config-sample.json /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.sources[0] = "/home/ors/ors-core/data/osm_file.pbf"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.logging.location = "/home/ors/ors-core/logs/ors"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.profiles.default_params.elevation_cache_path = "/home/ors/ors-core/data/elevation_cache"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.profiles.default_params.graphs_root_path = "/home/ors/ors-core/data/graphs"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.init_threads = 1' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq 'del(.ors.services.routing.profiles.active[1,2,3,4,5,6,7,8])' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json

RUN mvn -f /ors-core/openrouteservice/pom.xml package -DskipTests

# build final image, just copying stuff inside
FROM eclipse-temurin:17.0.6_10-jre-alpine as publish

# Build ARGS
ARG UID=1000
ARG GID=1000
ARG OSM_FILE=./openrouteservice/src/main/files/heidelberg.osm.gz
ARG BASE_FOLDER=/home/ors

# Runtime ENVs for tomcat
ENV CATALINA_BASE=${BASE_FOLDER}/tomcat
ENV CATALINA_HOME=${BASE_FOLDER}/tomcat
ENV CATALINA_PID=${BASE_FOLDER}/tomcat/temp/tomcat.pid

# Set the default language
ENV LANG='en_US' LANGUAGE='en_US' LC_ALL='en_US'

# Setup the target system with the right user and folders.
RUN apk add --no-cache bash=~'5.2' openssl=~'3.0' && \
    addgroup -g ${GID} ors && \
    adduser -D -h ${BASE_FOLDER} -u ${UID} -G ors ors &&  \
    mkdir -p ${BASE_FOLDER}/ors-core/logs/ors ${BASE_FOLDER}/ors-conf ${BASE_FOLDER}/tomcat/logs &&  \
    chown -R ors ${BASE_FOLDER}/tomcat ${BASE_FOLDER}/ors-core/logs/ors ${BASE_FOLDER}/ors-conf ${BASE_FOLDER}/tomcat/logs

WORKDIR ${BASE_FOLDER}

# Copy over the needed bits and pieces from the other stages.
COPY --chown=ors:ors --from=build /ors-core/openrouteservice/target/ors.war ${BASE_FOLDER}/ors-core/ors.war
COPY --chown=ors:ors --from=build /ors-core/openrouteservice/src/main/resources/ors-config.json ${BASE_FOLDER}/ors-core/ors-config.json
COPY --chown=ors:ors --from=tomcat /tmp/tomcat ${BASE_FOLDER}/tomcat
COPY --chown=ors:ors ./docker-entrypoint.sh ${BASE_FOLDER}/ors-core/docker-entrypoint.sh
COPY --chown=ors:ors ./$OSM_FILE ${BASE_FOLDER}/ors-core/data/osm_file.pbf

USER ${UID}:${GID}

ENV BUILD_GRAPHS="False"

# Start the container
ENTRYPOINT ["/home/ors/ors-core/docker-entrypoint.sh"]
CMD ["/home/ors"]
