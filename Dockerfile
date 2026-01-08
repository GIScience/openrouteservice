FROM docker.io/maven:3.9.11-amazoncorretto-21-alpine AS build
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

FROM docker.io/golang:1.25.4-alpine3.22 AS build-go
# ============================================================================
# Build stage for Go-based tools
# This stage is dedicated to building Go-based tools required in later stages.
# ============================================================================

RUN GO111MODULE=on go install github.com/mikefarah/yq/v4@v4.48.1

FROM docker.io/amazoncorretto:21.0.9-alpine3.22 AS base
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
    chown -R ors:ors ${ORS_HOME} && \
    chmod -R u+rwX,g+rwX ${ORS_HOME}

# Set the default language
ENV LANG='en_US' LANGUAGE='en_US' LC_ALL='en_US' \
    ORS_HOME=${ORS_HOME}

WORKDIR ${ORS_HOME}

# Expose port
EXPOSE 8082

HEALTHCHECK --interval=3s --timeout=2s CMD ["sh", "-c", "wget --quiet --tries=1 --spider http://localhost:8082/ors/v2/health || exit 1"]

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

# Copy JAR from build stage
COPY --chown=ors:ors --chmod=750 --from=build /tmp/ors/ors-api/target/ors.jar /ors.jar

# Switch to non-root user
USER ors

# Run Java jar directly as PID 1
# Configuration via environment variables:
# - JAVA_OPTS: additional JVM options
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
# Copy JAR from build stage with broader permissions
COPY --chown=ors:ors --chmod=755 --from=build /tmp/ors/ors-api/target/ors.jar /ors.jar


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
