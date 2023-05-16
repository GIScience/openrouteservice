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
ARG TOMCAT_MAJOR=9
ARG TOMCAT_VERSION=9.0.75

# hadolint ignore=DL3002
USER root

WORKDIR /tmp
# Prepare tomcat
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-${TOMCAT_MAJOR}/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -O /tmp/tomcat.tar.gz && \
    tar xf tomcat.tar.gz && \
    mv /tmp/apache-tomcat-${TOMCAT_VERSION}/ /tmp/tomcat && \
    echo "org.apache.catalina.level = WARNING" >> /tmp/tomcat/conf/logging.properties


FROM base as build

# hadolint ignore=DL3002
USER root

ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"

ARG ORS_CONFIG=ors-api/src/main/resources/ors-config-sample.json
ARG CONFIG_PATH=/ors-core/ors-api/src/main/resources/ors-config.json

WORKDIR /ors-core

COPY ors-api /ors-core/ors-api
COPY ors-routing /ors-core/ors-routing
COPY pom.xml /ors-core/pom.xml
COPY $ORS_CONFIG $CONFIG_PATH

# Configure ors config:
# Fist set pipefail to -c to allow intermediate pipes to throw errors
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
# - Replace paths in ors-config.json to match docker setup
# - init_threads = 1, > 1 been reported some issues
# - Delete all profiles but car
RUN jq '.ors.services.routing.sources[0] = "/home/ors/ors-core/data/osm_file.pbf"' $CONFIG_PATH | sponge $CONFIG_PATH && \
    jq '.ors.logging.location = "/home/ors/ors-core/logs/ors"' $CONFIG_PATH | sponge $CONFIG_PATH && \
    jq '.ors.services.routing.profiles.default_params.elevation_cache_path = "/home/ors/ors-core/data/elevation_cache"' $CONFIG_PATH | sponge $CONFIG_PATH && \
    jq '.ors.services.routing.profiles.default_params.graphs_root_path = "/home/ors/ors-core/data/graphs"' $CONFIG_PATH | sponge $CONFIG_PATH && \
    jq '.ors.services.routing.init_threads = 1' $CONFIG_PATH | sponge $CONFIG_PATH && \
    jq 'del(.ors.services.routing.profiles.active[1,2,3,4,5,6,7,8])' $CONFIG_PATH | sponge $CONFIG_PATH

RUN mvn package -DskipTests

# build final image, just copying stuff inside
FROM amazoncorretto:17.0.7-alpine3.17 as publish

# Build ARGS
ARG UID=1000
ARG GID=1000
ARG OSM_FILE=./ors-routing/src/main/files/heidelberg.osm.gz
ARG BASE_FOLDER=/home/ors

# Runtime ENVs for tomcat
ENV CATALINA_BASE=${BASE_FOLDER}/tomcat
ENV CATALINA_HOME=${BASE_FOLDER}/tomcat
ENV CATALINA_PID=${BASE_FOLDER}/tomcat/temp/tomcat.pid

# Set the default language
ENV LANG='en_US' LANGUAGE='en_US' LC_ALL='en_US'

# Setup the target system with the right user and folders.
RUN apk add --no-cache bash=~'5' openssl=~'3' && \
    addgroup -g ${GID} ors && \
    adduser -D -h ${BASE_FOLDER} -u ${UID} -G ors ors &&  \
    mkdir -p ${BASE_FOLDER}/ors-core/logs/ors ${BASE_FOLDER}/ors-conf ${BASE_FOLDER}/tomcat/logs &&  \
    chown -R ors ${BASE_FOLDER}/tomcat ${BASE_FOLDER}/ors-core/logs/ors ${BASE_FOLDER}/ors-conf ${BASE_FOLDER}/tomcat/logs

WORKDIR ${BASE_FOLDER}

# Copy over the needed bits and pieces from the other stages.
COPY --chown=ors:ors --from=build /ors-core/ors-api/target/ors.war ${BASE_FOLDER}/ors-core/ors.war
COPY --chown=ors:ors --from=build $CONFIG_PATH ${BASE_FOLDER}/ors-conf/ors-config.json
COPY --chown=ors:ors --from=tomcat /tmp/tomcat ${BASE_FOLDER}/tomcat
COPY --chown=ors:ors --from=build /ors-core/openrouteservice/src/main/resources/log4j.properties ${BASE_FOLDER}/tomcat/lib/log4j.properties
COPY --chown=ors:ors ./docker-entrypoint.sh ${BASE_FOLDER}/ors-core/docker-entrypoint.sh
COPY --chown=ors:ors ./$OSM_FILE ${BASE_FOLDER}/ors-core/data/osm_file.pbf

USER ${UID}:${GID}

ENV BUILD_GRAPHS="False"

# Start the container
ENTRYPOINT ["/home/ors/ors-core/docker-entrypoint.sh"]
CMD ["/home/ors"]
