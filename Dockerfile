FROM openjdk:11-jdk

ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"

ARG ORS_CONFIG=./openrouteservice/src/main/resources/ors-config-sample.json
ARG OSM_FILE=./openrouteservice/src/main/files/heidelberg.osm.gz
ENV BUILD_GRAPHS="False"
ARG VAR_USE_PREBUILT="False"
ENV USE_PREBUILT=$VAR_USE_PREBUILT
ARG UID=1000
ARG TOMCAT_VERSION=8.5.69

# Create user
RUN useradd -u $UID -md /ors-core ors

# Create directories
RUN mkdir -p /usr/local/tomcat /ors-conf /var/log/ors && \
    chown ors:ors /usr/local/tomcat /ors-conf /var/log/ors

# Install dependencies and locales
RUN apt-get update -qq && \
    apt-get install -qq -y locales nano maven moreutils jq md5deep && \
    rm -rf /var/lib/apt/lists/* && \
    locale-gen en_US.UTF-8

USER ors:ors
WORKDIR /ors-core

COPY --chown=ors:ors openrouteservice /ors-core/openrouteservice
COPY --chown=ors:ors $OSM_FILE /ors-core/data/osm_file.pbf
COPY --chown=ors:ors $ORS_CONFIG /ors-core/openrouteservice/src/main/resources/ors-config-sample.json
COPY --chown=ors:ors ./docker-entrypoint.sh /ors-core/docker-entrypoint.sh

# Install tomcat
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -O /tmp/tomcat.tar.gz && \
    cd /tmp && \
    tar xvfz tomcat.tar.gz && \
    cp -R /tmp/apache-tomcat-${TOMCAT_VERSION}/* /usr/local/tomcat/ && \
    rm -r /tmp/tomcat.tar.gz /tmp/apache-tomcat-${TOMCAT_VERSION}

# Configure ors config
RUN cp /ors-core/openrouteservice/src/main/resources/ors-config-sample.json /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    # Replace paths in ors-config.json to match docker setup
    jq '.ors.services.routing.sources[0] = "data/osm_file.pbf"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.profiles.default_params.elevation_cache_path = "data/elevation_cache"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    jq '.ors.services.routing.profiles.default_params.graphs_root_path = "data/graphs"' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \
    # init_threads = 1, > 1 been reported some issues
    jq '.ors.services.routing.init_threads = 1' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json && \

    # Delete all profiles but car
    jq 'del(.ors.services.routing.profiles.active[1,2,3,4,5,6,7,8])' /ors-core/openrouteservice/src/main/resources/ors-config.json |sponge /ors-core/openrouteservice/src/main/resources/ors-config.json

# Make all directories writable, to allow the usage of other uids via "docker run -u"
RUN chmod -R go+rwX /ors-core /ors-conf /usr/local/tomcat /var/log/ors

# Define volumes
VOLUME ["/ors-core/data/graphs", "/ors-core/data/elevation_cache", "/ors-conf", "/usr/local/tomcat/logs", "/var/log/ors", "/ors-core/data/pre-built"]

# Start the container
EXPOSE 8080
ENTRYPOINT ["/bin/bash", "/ors-core/docker-entrypoint.sh"]
