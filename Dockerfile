# Image is reused in the workflow builds for main and the latest version
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
ARG TOMCAT_MAJOR=10
ARG TOMCAT_VERSION=10.1.11

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

WORKDIR /ors-core

COPY ors-api /ors-core/ors-api
COPY ors-engine /ors-core/ors-engine
COPY pom.xml /ors-core/pom.xml
COPY ors-report-aggregation /ors-core/ors-report-aggregation

# Build the project and ignore the report aggregation module as not needed for the API build war
RUN mvn package -DskipTests -P buildWar

# build final image, just copying stuff inside
FROM amazoncorretto:17.0.7-alpine3.17 as publish

# Build ARGS
ARG UID=1000
ARG GID=1000
ARG OSM_FILE=./ors-api/src/test/files/heidelberg.osm.gz
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
    mkdir -p ${BASE_FOLDER}/ors-core/logs ${BASE_FOLDER}/ors-conf ${BASE_FOLDER}/ors-core/data ${BASE_FOLDER}/tomcat/logs &&  \
    chown -R ors ${BASE_FOLDER}/tomcat ${BASE_FOLDER}/ors-core/logs ${BASE_FOLDER}/ors-conf ${BASE_FOLDER}/ors-core/data ${BASE_FOLDER}/tomcat/logs

WORKDIR ${BASE_FOLDER}

# Copy over the needed bits and pieces from the other stages.
COPY --chown=ors:ors --from=tomcat /tmp/tomcat ${BASE_FOLDER}/tomcat
COPY --chown=ors:ors --from=build /ors-core/ors-api/target/ors.war ${BASE_FOLDER}/tomcat/webapps/ors.war
COPY --chown=ors:ors ./docker-entrypoint.sh ${BASE_FOLDER}/docker-entrypoint.sh
COPY --chown=ors:ors ./ors-config.yml ${BASE_FOLDER}/tmp/ors-config.yml
COPY --chown=ors:ors ./$OSM_FILE ${BASE_FOLDER}/tmp/osm_file.pbf

USER ${UID}:${GID}

# Rewrite the '    source_file:  ors-api/src/test/files/heidelberg.osm.gz' line in the config file to '    source_file:  ${BASE_FOLDER}/ors-core/data/osm_file.pbf'
RUN sed -i "s|    source_file:  ors-api/src/test/files/heidelberg.osm.gz|    source_file:  ${BASE_FOLDER}/ors-core/data/osm_file.pbf|g" ${BASE_FOLDER}/tmp/ors-config.yml
# Rewrite the '#    graphs_root_path: ./graphs' line in the config file to '    graphs_root_path: ${BASE_FOLDER}/ors-core/data/graphs'
RUN sed -i "s|#    graphs_root_path: ./graphs|    graphs_root_path: ${BASE_FOLDER}/ors-core/data/graphs|g" ${BASE_FOLDER}/tmp/ors-config.yml
# Rewrite the '#    elevation:' line in the config file to '    elevation:'
RUN sed -i "s|#    elevation:|    elevation:|g" ${BASE_FOLDER}/tmp/ors-config.yml
# Rewrite the '#      cache_path: ./elevation_cache' line in the config file to '      cache_path: ${BASE_FOLDER}/ors-core/data/elevation_cache'
RUN sed -i "s|#      cache_path: ./elevation_cache|      cache_path: ${BASE_FOLDER}/ors-core/data/elevation_cache|g" ${BASE_FOLDER}/tmp/ors-config.yml

ENV BUILD_GRAPHS="False"
ENV ORS_CONFIG_LOCATION=ors-conf/ors-config.yml

# Start the container
ENTRYPOINT ["/home/ors/docker-entrypoint.sh"]
CMD ["/home/ors"]