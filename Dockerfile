# Image is reused in the workflow builds for master and the latest version
FROM docker.io/maven:3.8-openjdk-17-slim as build
ARG DEBIAN_FRONTEND=noninteractive

# hadolint ignore=DL3002
USER root

WORKDIR /tmp/ors

COPY ors-api /tmp/ors/ors-api
COPY ors-engine /tmp/ors/ors-engine
COPY pom.xml /tmp/ors/pom.xml
COPY ors-report-aggregation /tmp/ors/ors-report-aggregation

# Build the project and ignore the report aggregation module as not needed for the API
RUN mvn clean package -DskipTests -pl '!ors-report-aggregation' -P buildFatJar

# build final image, just copying stuff inside
FROM docker.io/amazoncorretto:17.0.8-alpine3.18 as publish

# Build ARGS
ARG UID=1234
ARG GID=1234
ARG OSM_FILE=./ors-api/src/test/files/heidelberg.osm.gz
ARG BASE_FOLDER=/opt/openrouteservice

# Set the default language
ENV LANG='en_US' LANGUAGE='en_US' LC_ALL='en_US'

# Setup the target system with the right user and folders.
RUN apk add --no-cache bash=~'5' openssl=~'3' && \
    addgroup -g ${GID} ors && \
    adduser -D -h ${BASE_FOLDER} -u ${UID} -G ors ors &&  \
    mkdir -p ${BASE_FOLDER}/logs ${BASE_FOLDER}/conf ${BASE_FOLDER}/data ${BASE_FOLDER}/graphs && \
    chown -R ors ${BASE_FOLDER}

WORKDIR ${BASE_FOLDER}

# Copy over the needed bits and pieces from the other stages.
COPY ./docker-entrypoint.sh /entrypoint.sh
COPY --chown=ors:ors --from=build /tmp/ors/ors-api/target/ors.jar ${BASE_FOLDER}/lib/ors.jar
COPY --chown=ors:ors ./ors-api/ors-config.yml ${BASE_FOLDER}/config/example-ors-config.yml
COPY --chown=ors:ors ./$OSM_FILE ${BASE_FOLDER}/files/example_osm_file.pbf

# Install libxext, clean up and set permissions
RUN apk add --no-cache libxext=~'1.3' && \
    rm -rf /var/cache/apk/* && \
    chmod +x ${BASE_FOLDER}/lib/ors.jar && chown -R ors:ors ${BASE_FOLDER}

USER ors

ENV BUILD_GRAPHS="False"
ENV BASE_FOLDER=${BASE_FOLDER}
ENV ORS_CONFIG_LOCATION=${BASE_FOLDER}/config/example-ors-config.yml

# Start the container
ENTRYPOINT ["/entrypoint.sh"]
