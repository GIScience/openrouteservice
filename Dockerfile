# Image is reused in the workflow builds for master and the latest version
FROM docker.io/maven:3.8.7-openjdk-18-slim AS build
ARG DEBIAN_FRONTEND=noninteractive

# hadolint ignore=DL3002
USER root

WORKDIR /tmp/ors

COPY ors-api /tmp/ors/ors-api
COPY ors-engine /tmp/ors/ors-engine
COPY pom.xml /tmp/ors/pom.xml
COPY ors-report-aggregation /tmp/ors/ors-report-aggregation

# Build the project
RUN mvn clean package -DskipTests

# build final image, just copying stuff inside
FROM docker.io/amazoncorretto:21.0.2-alpine3.19 AS publish

# Build ARGS
ARG UID=1000
ARG GID=1000
ARG OSM_FILE=./ors-api/src/test/files/heidelberg.osm.gz
ARG BASE_FOLDER=/home/ors

# Set the default language
ENV LANG='en_US' LANGUAGE='en_US' LC_ALL='en_US'

# Setup the target system with the right user and folders.
RUN apk update && apk add --no-cache bash openssl yq jq curl && \
    addgroup ors && \
    mkdir -p ${BASE_FOLDER}/logs ${BASE_FOLDER}/files ${BASE_FOLDER}/graphs ${BASE_FOLDER}/elevation_cache  && \
    adduser -D -h ${BASE_FOLDER} --system -G ors ors  && \
    chown -R ors ${BASE_FOLDER}

WORKDIR ${BASE_FOLDER}

# Copy over the needed bits and pieces from the other stages.
COPY --chown=ors:ors --from=build /tmp/ors/ors-api/target/ors.jar /ors.jar
COPY --chown=ors:ors ./ors-config.yml /example-ors-config.yml
COPY --chown=ors:ors ./ors-config.env /example-ors-config.env
COPY --chown=ors:ors ./$OSM_FILE /heidelberg.osm.gz
COPY ./docker-entrypoint.sh /entrypoint.sh

# Set permissions
RUN chmod +x /ors.jar && chown -R ors:ors /entrypoint.sh && chown -R ors:ors ${BASE_FOLDER} && chown -R ors:ors /example-ors-config.yml && chown -R ors:ors /example-ors-config.env && \
    # Rewrite the example config to use the right files in the container
    sed -i "/ors.engine.source_file=.*/s/.*/ors.engine.source_file=\/heidelberg.osm.gz/" "/example-ors-config.env" && \
    sed -i "/    source_file:.*/s/.*/    source_file: \/heidelberg.osm.gz/" "/example-ors-config.yml"

#USER ${UID}:${GID}

ENV BUILD_GRAPHS="False"
ENV REBUILD_GRAPHS="False"
# Set the ARG to an ENV. Else it will be lost.
ENV BASE_FOLDER=${BASE_FOLDER}

# Start the container
ENTRYPOINT ["/entrypoint.sh"]
