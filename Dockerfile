FROM docker.io/maven:3.9.16-amazoncorretto-21-alpine@sha256:2d12b180966b0b68684a5e347a9ed287f06a65dbebf3388635b4ebbed0c5097c AS build
# ============================================================================
# Build stage for Java-based ORS application
# This stage is responsible for compiling and packaging the Java-based OpenRouteService (ORS) application.
# ============================================================================
ARG DEBIAN_FRONTEND=noninteractive

# hadolint ignore=DL3002
USER root

WORKDIR /tmp/ors

COPY ors-api/pom.xml /tmp/ors/ors-api/pom.xml
COPY ors-engine/pom.xml /tmp/ors/ors-engine/pom.xml
COPY pom.xml /tmp/ors/pom.xml
COPY ors-report-aggregation/pom.xml /tmp/ors/ors-report-aggregation/pom.xml
COPY ors-test-scenarios/pom.xml /tmp/ors/ors-test-scenarios/pom.xml
COPY ors-benchmark/pom.xml /tmp/ors/ors-benchmark/pom.xml
COPY mvnw /tmp/ors/mvnw
COPY .mvn /tmp/ors/.mvn

# Download dependencies
ARG MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository"
ENV MAVEN_OPTS="${MAVEN_OPTS}"
RUN ./mvnw -pl 'ors-api,ors-engine' -q \
    dependency:resolve dependency:resolve-plugins -Dmaven.test.skip=true > /dev/null || true

COPY ors-api /tmp/ors/ors-api
COPY ors-engine /tmp/ors/ors-engine

# Build the project
RUN ./mvnw -pl 'ors-api,ors-engine' \
    -q clean package -DskipTests -Dmaven.test.skip=true

FROM docker.io/golang:1.26.6-alpine3.24@sha256:3889b425f035be855a72fb4755265311293b6d414521f0a519d819df32222d83 AS build-go
# ============================================================================
# Build stage for Go-based tools
# This stage is dedicated to building Go-based tools required in later stages.
# ============================================================================

RUN GO111MODULE=on go install github.com/mikefarah/yq/v4@v4.53.3

FROM docker.io/amazoncorretto:21.0.12-alpine3.24@sha256:58c1d555f4ff3be0cfe90d3b4d1762bde080b57afbb71d48657b9d22748cad5b AS base
# ============================================================================
# Base image stage: common setup for all runtime stages
# This stage sets up the foundational environment for running the OpenRouteService (ORS) application.
# ============================================================================

ARG UID=1000
ARG GID=1000
ARG ORS_HOME=/home/ors

# Setup user and directory structure
RUN addgroup ors -g ${GID} && \
    adduser -D -u ${UID} --system -G ors ors && \
    mkdir -p ${ORS_HOME}/logs ${ORS_HOME}/files ${ORS_HOME}/graphs ${ORS_HOME}/elevation_cache ${ORS_HOME}/app && \
    chown -R ors:0 ${ORS_HOME} && \
    chmod -R u+rwX,g=u ${ORS_HOME}

# Set the default language
ENV LANG='en_US' LANGUAGE='en_US' LC_ALL='en_US' \
    ORS_HOME=${ORS_HOME}

WORKDIR ${ORS_HOME}

# Expose port
EXPOSE 8082

HEALTHCHECK --start-period=60s --interval=10s --timeout=2s CMD ["sh", "-c", "wget --quiet --tries=1 --spider http://localhost:8082/ors/v2/health || exit 1"]

LABEL org.opencontainers.image.source="https://github.com/GIScience/openrouteservice"
LABEL org.opencontainers.image.licenses="LGPL-3.0-only"
LABEL org.opencontainers.image.title="openrouteservice"
LABEL org.opencontainers.image.description="Open-source route planning service based on OpenStreetMap data"
LABEL org.opencontainers.image.documentation="https://giscience.github.io/openrouteservice"

FROM base AS slim
# ============================================================================
# K8s-ready image stage
# This stage is optimized for Kubernetes deployment with:
# - Java as PID 1 for proper signal handling
# - Direct Logging to STDOUT/STDERR
# - Non-root execution
# - Absolute minimal footprint
# - No config presets or example data
# ============================================================================

# Copy JAR from build stage.
# 644, not 750: `java -jar` only reads the archive.
COPY --chown=ors:0 --chmod=644 --from=build /tmp/ors/ors-api/target/ors.jar /ors.jar

# Stdout/stderr only for the slim image. Can be overridden by setting LOGGING_FILE_NAME to a file path in the container.
ENV LOGGING_FILE_NAME=""

# Apache Tomcat hardening: pinned response settings, shorter connector timeout, no Swagger UI or OpenAPI document
ENV SERVER_SERVER_HEADER="" \
    SERVER_ERROR_INCLUDE_STACKTRACE=never \
    SERVER_ERROR_INCLUDE_MESSAGE=never \
    SERVER_ERROR_INCLUDE_EXCEPTION=false \
    SERVER_ERROR_INCLUDE_BINDING_ERRORS=never \
    SERVER_MAX_HTTP_REQUEST_HEADER_SIZE=8KB \
    SERVER_TOMCAT_CONNECTION_TIMEOUT=20s \
    SPRINGDOC_SWAGGER_UI_ENABLED=false \
    SPRINGDOC_API_DOCS_ENABLED=false

# Switch to a non-root user, declared numerically and above 1000.
USER 1001:0

# Run Java jar directly as PID 1
# Configuration via environment variables:
# - JDK_JAVA_OPTIONS: additional JVM options
# - Server settings via Spring properties (e.g., server.port, server.servlet.context-path)
# - Logging via Spring properties (logging.level.*, logging.pattern.*)
ENTRYPOINT ["java", "-jar", "/ors.jar"]

FROM base AS publish
# ============================================================================
# Convenient ORS publish image
# This stage is optimized for easy publishing and self-hosting in non-Kubernetes environments.
# It includes more components and configurations to facilitate quick setup and deployment:
# - Necessary runtime dependencies
# - Example configuration files and data
# - Entrypoint scripts for easy startup
# - Container configuration validations
# - Informative/verbose container logging
# ============================================================================

# Build ARGS
ARG OSM_FILE=./ors-api/src/test/files/heidelberg.test.pbf

# Copy over the needed bits and pieces from the other stages.
COPY --chown=ors:ors --chmod=755 ./$OSM_FILE /heidelberg.test.pbf
COPY --chown=ors:ors --chmod=755 ./docker-entrypoint.sh /entrypoint.sh
COPY --chown=ors:ors --from=build-go /go/bin/yq /bin/yq
# Copy JAR from build stage. Read-only data: docker-entrypoint.sh starts it with
# `java -jar`, never by executing it, so no execute bit is needed here either.
COPY --chown=ors:0 --chmod=644 --from=build /tmp/ors/ors-api/target/ors.jar /ors.jar


# Setup additional packages for publish stage and allow read access to others
RUN apk add --no-cache bash=~5 jq=~1 openssl=~3 && \
    chmod -R o-rwx ${ORS_HOME}

# Copy the example config files to the build folder
COPY --chown=ors:ors --chmod=755 ./ors-config.yml /example-ors-config.yml
COPY --chown=ors:ors --chmod=755 ./ors-config.env /example-ors-config.env

# Rewrite the example config to use the right files in the container
RUN yq -i -p=props -o=props \
    '.ors.engine.profile_default.build.source_file="/home/ors/files/example-heidelberg.test.pbf"' \
    /example-ors-config.env && \
    yq -i e '.ors.engine.profile_default.build.source_file = "/home/ors/files/example-heidelberg.test.pbf"' \
    /example-ors-config.yml

ENV BUILD_GRAPHS="False"
ENV REBUILD_GRAPHS="False"
# Set the ARG to an ENV. Else it will be lost.
ENV ORS_HOME=${ORS_HOME}

WORKDIR ${ORS_HOME}

# Start the container
ENTRYPOINT ["/entrypoint.sh"]
