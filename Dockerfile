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
RUN mvn -q clean package -DskipTests

# Copy the example config files to the build folder
COPY ./ors-config.yml /tmp/ors/example-ors-config.yml
COPY ./ors-config.env /tmp/ors/example-ors-config.env
# Rewrite the example config to use the right files in the container
RUN sed -i "/ors.engine.source_file=.*/s/.*/ors.engine.source_file=\/home\/ors\/files\/example-heidelberg.osm.gz/" "/tmp/ors/example-ors-config.env" && \
        sed -i "/    source_file:.*/s/.*/    source_file: \/home\/ors\/files\/example-heidelberg.osm.gz/" "/tmp/ors/example-ors-config.yml"

# build final image, just copying stuff inside
FROM docker.io/amazoncorretto:21.0.2-alpine3.19 AS publish

# Build ARGS
ARG UID=1000
ARG GID=1000
ARG OSM_FILE=./ors-api/src/test/files/heidelberg.osm.gz
ARG ORS_HOME=/home/ors

# Set the default language
ENV LANG='en_US' LANGUAGE='en_US' LC_ALL='en_US'

# Setup the target system with the right user and folders.
RUN apk update && apk add --no-cache bash openssl yq jq curl && \
    addgroup ors -g ${GID} && \
    mkdir -p ${ORS_HOME}/logs ${ORS_HOME}/files ${ORS_HOME}/graphs ${ORS_HOME}/elevation_cache  && \
    adduser -D -h ${ORS_HOME} -u ${UID} --system -G ors ors  && \
    chown ors:ors ${ORS_HOME} \
    # Give all permissions to the user
    && chmod -R 777 ${ORS_HOME}

# Copy over the needed bits and pieces from the other stages.
COPY --chown=ors:ors --from=build /tmp/ors/ors-api/target/ors.jar /ors.jar
COPY --chown=ors:ors --from=build /tmp/ors/example-ors-config.yml /example-ors-config.yml
COPY --chown=ors:ors --from=build /tmp/ors/example-ors-config.env /example-ors-config.env
COPY --chown=ors:ors ./$OSM_FILE /heidelberg.osm.gz
COPY --chown=ors:ors ./docker-entrypoint.sh /entrypoint.sh


ENV BUILD_GRAPHS="False"
ENV REBUILD_GRAPHS="False"
# Set the ARG to an ENV. Else it will be lost.
ENV ORS_HOME=${ORS_HOME}

WORKDIR ${ORS_HOME}
# Start the container
ENTRYPOINT ["/entrypoint.sh"]
