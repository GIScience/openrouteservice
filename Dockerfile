FROM docker.io/maven:3.9.16-amazoncorretto-25@sha256:98295c180adc4b5c0a52b830e00c387c862d5827d395cd7737d8205170428785 AS build
# ============================================================================
# Build stage for Java-based ORS application
# This stage is responsible for compiling and packaging the Java-based OpenRouteService (ORS) application.
# Deliberately the glibc variant (Amazon Linux) rather than -alpine: this stage
# also produces the jlink runtime that the `slim` stage copies into a glibc
# distroless base. A runtime linked against musl would not run there.
# ============================================================================
ARG DEBIAN_FRONTEND=noninteractive

# hadolint ignore=DL3002
USER root

# The `slim` stage has no JDK of its own -- its base ships the native libraries a
# JVM needs but no JVM -- so the runtime is built here and copied in. binutils
# (objcopy) is required by jlink's --strip-debug. This stage is discarded before
# the final image, so neither reaches its CVE surface.
#
# tar comes with them because this image is Amazon Linux minimal and ships
# without it, while the -alpine variant this replaced had busybox tar. The Maven
# wrapper untars the Maven distribution it downloads, so without tar the build
# fails at ./mvnw with "failed to untar" rather than anything about tar itself.
#
# /empty-tmp exists because the distroless base ships no /tmp at all and COPY
# cannot create a directory with the sticky bit set from nothing.
RUN dnf install -y binutils tar && \
    jlink --add-modules java.se,jdk.unsupported,jdk.crypto.ec \
    --strip-debug --no-man-pages --no-header-files --compress=zip-6 \
    --output /javaruntime && \
    mkdir -m 1777 /empty-tmp && \
    dnf clean all

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

FROM docker.io/amazoncorretto:25.0.4-alpine3.24@sha256:2ad5f5cf03a3970f2478b130dc28f51b179ce13c58154fe3ec1a6fdeb3b86e3a AS base
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

FROM gcr.io/distroless/cc-debian13:nonroot@sha256:c31ff9abcb1910f3ab25c7957bdaf0bfe12a01eb546e8df2282f1c8f682b606c AS slim
# ============================================================================
# K8s-ready image stage
# This stage is optimized for Kubernetes and container deployments with:
# - Java as PID 1 for proper signal handling
# - Direct Logging to STDOUT/STDERR
# - Non-root execution
# - Absolute minimal footprint
# - No config presets or example data
# - Distroless, cosign-attested Debian 13 base: no shell, no package manager,
#   so nothing here can RUN; everything arrives via COPY.
#
# cc, not java-base. java-base is the usual choice for a Java image, but it
# exists to carry the JDK's font and imaging stack -- fontconfig, freetype,
# dejavu, libjpeg, liblcms2, libpng -- and this image has no way to reach it:
# gt-swing and gt-render are no longer on the classpath, and the only java.awt
# openrouteservice imports is java.awt.geom.Point2D and Line2D, both pure Java.
# Dropping those packages removes 26 of the 42 findings Trivy reports against
# java-base, at the same size and with 0 CRITICAL/HIGH either way.
#
# That the font path is broken here rather than merely unused is the point, not
# an oversight: the same jlink runtime that prints "FONT OK" on java-base dies
# in sun.java2d.SunGraphics2D.getFontMetrics on cc. Nothing may reintroduce a
# dependency that renders text or images without also changing this line, and
# the failure is immediate and obvious rather than subtle.
#
# cc is `base` plus libstdc++ and libgcc, which is exactly what libjvm.so needs
# and the reason plain `base` will not do. Going lower means transplanting
# libstdc++ by hand, which costs the scannability this base was chosen for and
# ages silently outside Renovate's reach. cc still provides ca-certificates,
# tzdata, zlib and netbase, so HTTPS, time zones and JAR reading all work.
#
# Alpine is not an option: smaller, but currently shipping two HIGH CVEs
# (CVE-2026-14456 in libssl3/libcrypto3) that the CRITICAL/HIGH gate in
# vulnerability-scanning.yml would fail on, and it would force the jlink runtime
# onto musl. SUSE BCI, Chainguard and Temurin-on-Ubuntu are out for a different
# reason: devguard-scanner imports 13 of 46 OSV ecosystems, and SUSE, Wolfi and
# Ubuntu are not among them, so the openCode pipeline could not scan them.
# ============================================================================

ARG ORS_HOME=/home/ors

COPY --from=build --chown=1001:0 /javaruntime /opt/java
# HEALTHCHECK probe: 71 KB of statically linked C, the whole binary being one
# HTTP request with Docker's exit-code contract (0 on 2xx, 1 otherwise, single
# attempt). Preferred over copying curl or busybox in, which drag a general
# purpose download tool or an entire shell back into a deliberately shell-less
# base. Pinned to the index digest, which covers amd64 and arm64, so the copy
# resolves per target platform like every FROM above.
COPY --from=ghcr.io/tarampampam/microcheck:1.4.0@sha256:c9f79cd408626de7c10f2d487d67339f49adf0ba61dde96ede65343269db1f85 \
    --chown=1001:0 --chmod=755 /bin/httpcheck /usr/bin/httpcheck
COPY --from=base --chown=1001:0 ${ORS_HOME} ${ORS_HOME}
COPY --chown=1001:0 --chmod=644 --from=build /tmp/ors/ors-api/target/ors.jar /ors.jar
# The base image ships no /tmp at all, and BuildKit's COPY can't create one
# with the sticky bit set via --chmod on a fresh directory that has no source
# counterpart, so it's built once in `build` (which has a shell) and copied in.
# Needed for XDG_CACHE_HOME below and as a generic scratch/temp location.
COPY --from=build --chown=1001:0 --chmod=1777 /empty-tmp /tmp

ENV PATH="/opt/java/bin:${PATH}" \
    LANG='en_US' LANGUAGE='en_US' LC_ALL='en_US' \
    ORS_HOME=${ORS_HOME} \
    LOGGING_FILE_NAME="" \
    XDG_CACHE_HOME=/tmp

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

WORKDIR ${ORS_HOME}

EXPOSE 8082

# Exec form, no shell in this base. --port-env points the probe at SERVER_PORT,
# the variable Spring Boot binds to server.port, so `-e SERVER_PORT=<port>`
# retargets the application and its probe together. The request timeout stays at
# httpcheck's own 5s default and is overridable via ORS_HEALTHCHECK_TIMEOUT --
# passing -t here would silently win over the env variable and make it dead.
# Docker's --timeout is the outer bound and needs headroom above that 5s.
# The URL path is fixed: httpcheck takes it positionally and the exec form
# expands no variables, so overriding it means `docker run --health-cmd` (or a
# Kubernetes httpGet probe, which ignores HEALTHCHECK altogether).
HEALTHCHECK --start-period=60s --interval=30s --timeout=8s CMD ["/usr/bin/httpcheck", \
    "--port-env", "SERVER_PORT", "--timeout-env", "ORS_HEALTHCHECK_TIMEOUT", \
    "--connect-timeout", "1", "http://localhost:8082/ors/v2/health"]

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
#
# This stage deliberately has no USER instruction and therefore runs as root.
# That is a decision, not an oversight, and image linters will flag it
# (CIS-DI-0001, "Create a user for the container"). The reasoning:
#
# docker-entrypoint.sh does not drop privileges -- it runs as whatever user
# Docker was given -- and it chowns ORS_HOME to that user during startup. Root
# is what makes that chown succeed against a freshly created bind mount, which
# is the normal self-hosting case. Measured against the published image:
#
#   --user 1000:1000, no volume            ORS_HOME writable, starts
#   --user 1000:1000, root-owned volume    "ORS_HOME is not writable", exits
#   default (root),   root-owned volume    ORS_HOME writable, starts
#
# So pinning a USER here would break `-v /some/host/dir:/home/ors` for every
# self-hoster who had not chowned the directory first, to defend a boundary
# that a self-hosted single-tenant container does not really have. Anyone who
# wants a non-root container can pass --user, provided they own the volume.
#
# The `slim` stage is the opposite case and does pin USER 1001:0: it is the
# Kubernetes-facing image, it has no entrypoint script, and it never chowns
# anything, so it has no reason to start as root.
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
