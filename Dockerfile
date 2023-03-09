# Image is reused in the workflow builds for master and the latest version
FROM maven:3.8-jdk-11-slim as base

ARG DEBIAN_FRONTEND=noninteractive


# hadolint ignore=DL3002
USER root

# Install dependencies and locales
RUN apt-get update -qq && \
    apt-get install -qq -y --no-install-recommends nano=5.4-2+deb11u2 moreutils=0.65-1 jq=1.6-2.1 wget=1.21-1+deb11u1 && \
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
FROM adoptopenjdk/openjdk11:jre-11.0.18_10-alpine as publish

# Build ARGS
ARG UID=1000
ARG GID=1000
ARG OSM_FILE=./openrouteservice/src/main/files/heidelberg.osm.gz
ARG BASE_FOLDER=/home/ors

# Runtime ENVs for tomcat
ENV CATALINA_BASE=${BASE_FOLDER}/tomcat
ENV CATALINA_HOME=${BASE_FOLDER}/tomcat
ENV CATALINA_PID=${BASE_FOLDER}/tomcat/temp/tomcat.pid


ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN apk add --no-cache bash='5.1.16-r0'

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Compile en_US.UTF-8 for alpine
# hadolint ignore=DL3019,SC2086
RUN ln -svf /usr/glibc-compat/lib/ld-2.31.so /usr/glibc-compat/lib/ld-linux-x86-64.so.2 && \
    apk add --no-cache --virtual .build-deps curl='7.79.1-r5' binutils='2.35.2-r2' && \
    GLIBC_VER="2.29-r0" && \
    ALPINE_GLIBC_REPO="https://github.com/sgerrand/alpine-pkg-glibc/releases/download" && \
    GCC_LIBS_URL="https://archive.archlinux.org/packages/g/gcc-libs/gcc-libs-9.1.0-2-x86_64.pkg.tar.xz" && \
    GCC_LIBS_SHA256="91dba90f3c20d32fcf7f1dbe91523653018aa0b8d2230b00f822f6722804cf08" && \
    ZLIB_URL="https://archive.archlinux.org/packages/z/zlib/zlib-1%3A1.2.11-3-x86_64.pkg.tar.xz" && \
    ZLIB_SHA256=17aede0b9f8baa789c5aa3f358fbf8c68a5f1228c5e6cba1a5dd34102ef4d4e5 && \
    curl -LfsS https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub -o /etc/apk/keys/sgerrand.rsa.pub && \
    SGERRAND_RSA_SHA256="823b54589c93b02497f1ba4dc622eaef9c813e6b0f0ebbb2f771e32adf9f4ef2" && \
    echo "${SGERRAND_RSA_SHA256} */etc/apk/keys/sgerrand.rsa.pub" | sha256sum -c - && \
    curl -LfsS ${ALPINE_GLIBC_REPO}/${GLIBC_VER}/glibc-${GLIBC_VER}.apk > /tmp/glibc-${GLIBC_VER}.apk && \
    apk add /tmp/glibc-${GLIBC_VER}.apk && \
    curl -LfsS ${ALPINE_GLIBC_REPO}/${GLIBC_VER}/glibc-bin-${GLIBC_VER}.apk > /tmp/glibc-bin-${GLIBC_VER}.apk && \
    apk add /tmp/glibc-bin-${GLIBC_VER}.apk && \
    curl -Ls ${ALPINE_GLIBC_REPO}/${GLIBC_VER}/glibc-i18n-${GLIBC_VER}.apk > /tmp/glibc-i18n-${GLIBC_VER}.apk && \
    apk add /tmp/glibc-i18n-${GLIBC_VER}.apk && \
    /usr/glibc-compat/bin/localedef --force --inputfile POSIX --charmap UTF-8 "$LANG" || true && \
    echo "export LANG=$LANG" > /etc/profile.d/locale.sh && \
    curl -LfsS ${GCC_LIBS_URL} -o /tmp/gcc-libs.tar.xz && \
    echo "${GCC_LIBS_SHA256} */tmp/gcc-libs.tar.xz" | sha256sum -c - && \
    mkdir /tmp/gcc && \
    tar -xf /tmp/gcc-libs.tar.xz -C /tmp/gcc && \
    mv /tmp/gcc/usr/lib/libgcc* /tmp/gcc/usr/lib/libstdc++* /usr/glibc-compat/lib && \
    strip /usr/glibc-compat/lib/libgcc_s.so.* /usr/glibc-compat/lib/libstdc++.so* && \
    curl -LfsS ${ZLIB_URL} -o /tmp/libz.tar.xz && \
    echo "${ZLIB_SHA256} */tmp/libz.tar.xz" | sha256sum -c - && \
    mkdir /tmp/libz && \
    tar -xf /tmp/libz.tar.xz -C /tmp/libz && \
    mv /tmp/libz/usr/lib/libz.so* /usr/glibc-compat/lib && \
    apk del --purge .build-deps glibc-i18n && \
    apk add --no-cache openssl='1.1.1t-r0' && \
    rm -rf /tmp/*.apk /tmp/gcc /tmp/gcc-libs.tar.xz /tmp/libz /tmp/libz.tar.xz /var/cache/apk/* && \
    addgroup -g ${GID} ors && \
    adduser -D -h ${BASE_FOLDER} -u ${UID} -G ors ors &&  \
    mkdir -p ${BASE_FOLDER}/ors-core/logs/ors ${BASE_FOLDER}/ors-conf ${BASE_FOLDER}/tomcat/logs &&  \
    chown -R ors ${BASE_FOLDER}/tomcat ${BASE_FOLDER}/ors-core/logs/ors ${BASE_FOLDER}/ors-conf ${BASE_FOLDER}/tomcat/logs
WORKDIR ${BASE_FOLDER}

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
