# Image is reused in the workflow builds for master and the latest version
FROM docker.io/maven:3.9.9-amazoncorretto-21-alpine AS build
ARG DEBIAN_FRONTEND=noninteractive

# hadolint ignore=DL3002
USER root

WORKDIR /tmp/ors

COPY ors-api/pom.xml /tmp/ors/ors-api/pom.xml
COPY ors-engine/pom.xml /tmp/ors/ors-engine/pom.xml
COPY pom.xml /tmp/ors/pom.xml
COPY ors-report-aggregation/pom.xml /tmp/ors/ors-report-aggregation/pom.xml
COPY ors-test-scenarios/pom.xml /tmp/ors/ors-test-scenarios/pom.xml

# Build the project
RUN mvn -pl '!ors-test-scenarios,!ors-report-aggregation' -q dependency:go-offline

COPY ors-api /tmp/ors/ors-api
COPY ors-engine /tmp/ors/ors-engine

# Build the project
RUN mvn -pl '!ors-test-scenarios,!ors-report-aggregation' \
    -q clean package -DskipTests -Dmaven.test.skip=true

FROM docker.io/maven:3.9.9-amazoncorretto-21-alpine AS build-go
# Setup the target system with the right user and folders.
RUN apk add --no-cache go && \
    GO111MODULE=on go install github.com/mikefarah/yq/v4@v4.44.5

# build final image, just copying stuff inside
FROM docker.io/amazoncorretto:21.0.4-alpine3.20 AS publish

# Build ARGS
ARG UID=1000
ARG GID=1000
ARG OSM_FILE=./ors-api/src/test/files/heidelberg.test.pbf
ARG ORS_HOME=/home/ors
ARG OSM_URL=https://download.geofabrik.de/south-america/chile-latest.osm.pbf

# Set the default language
ENV LANG='en_US' LANGUAGE='en_US' LC_ALL='en_US'

# Setup the target system with the right user and folders.
RUN apk update && apk add --no-cache bash=~5 jq=~1 openssl=~3 wget=~1.21 && \
    addgroup ors -g ${GID} && \
    mkdir -p ${ORS_HOME}/logs ${ORS_HOME}/files ${ORS_HOME}/graphs ${ORS_HOME}/elevation_cache  && \
    adduser -D -h ${ORS_HOME} -u ${UID} --system -G ors ors  && \
    chown ors:ors ${ORS_HOME} \
    # Give all permissions to the user
    && chmod -R 777 ${ORS_HOME}

# Download Chile OSM file and set up files
RUN mkdir -p ${ORS_HOME}/files && \
    wget -q ${OSM_URL} -O ${ORS_HOME}/files/chile-latest.osm.pbf && \
    chown ors:ors ${ORS_HOME}/files/chile-latest.osm.pbf

# Copy over the needed bits and pieces from the other stages.
COPY --chown=ors:ors --from=build /tmp/ors/ors-api/target/ors.jar /ors.jar
COPY --chown=ors:ors ./docker-entrypoint.sh /entrypoint.sh
COPY --chown=ors:ors --from=build-go /root/go/bin/yq /bin/yq

# Copy the example config files to the build folder
COPY --chown=ors:ors ./ors-config.yml /example-ors-config.yml
COPY --chown=ors:ors ./ors-config.env /example-ors-config.env

# Rewrite the example config to use the right files in the container
RUN yq -i -p=props -o=props \
    '.ors.engine.profile_default.build.source_file="/home/ors/files/chile-latest.osm.pbf"' \
    /example-ors-config.env && \
    yq -i e '.ors.engine.profile_default.build.source_file = "/home/ors/files/chile-latest.osm.pbf"' \
    /example-ors-config.yml

ENV BUILD_GRAPHS="False"
ENV REBUILD_GRAPHS="False"
# Set the ARG to an ENV. Else it will be lost.
ENV ORS_HOME=${ORS_HOME}

WORKDIR ${ORS_HOME}

# Healthcheck
HEALTHCHECK --interval=3s --timeout=2s CMD ["sh", "-c", "wget --quiet --tries=1 --spider http://localhost:8082/ors/v2/health || exit 1"]

# Start the container
ENTRYPOINT ["/entrypoint.sh"]
