FROM openjdk:11-jdk

ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"

ARG APP_CONFIG=./openrouteservice/src/main/resources/app.config.sample
ARG OSM_FILE=./openrouteservice/src/main/files/heidelberg.osm.gz

WORKDIR /ors-core

COPY openrouteservice /ors-core/openrouteservice
COPY $OSM_FILE /ors-core/data/osm_file.pbf
COPY $APP_CONFIG /ors-core/openrouteservice/src/main/resources/app.config.sample

# Install tomcat
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.39/bin/apache-tomcat-8.5.39.tar.gz -O /tmp/tomcat.tar.gz && \
    cd /tmp && \
    tar xvfz tomcat.tar.gz && \
    mkdir /usr/local/tomcat /ors-conf && \
    cp -R /tmp/apache-tomcat-8.5.39/* /usr/local/tomcat/ && \
    # Install dependencies and locales
    apt-get update -qq && apt-get install -qq -y locales nano maven moreutils jq && \
    locale-gen en_US.UTF-8 && \
    # Rename to app.config
    cp /ors-core/openrouteservice/src/main/resources/app.config.sample /ors-core/openrouteservice/src/main/resources/app.config && \
    # Replace paths in app.config to match docker setup
    jq '.ors.services.routing.sources[0] = "data/osm_file.pbf"' /ors-core/openrouteservice/src/main/resources/app.config |sponge /ors-core/openrouteservice/src/main/resources/app.config && \
    jq '.ors.services.routing.profiles.default_params.elevation_cache_path = "data/elevation_cache"' /ors-core/openrouteservice/src/main/resources/app.config |sponge /ors-core/openrouteservice/src/main/resources/app.config && \
    jq '.ors.services.routing.profiles.default_params.graphs_root_path = "data/graphs"' /ors-core/openrouteservice/src/main/resources/app.config |sponge /ors-core/openrouteservice/src/main/resources/app.config && \
    # init_threads = 1, > 1 been reported some issues
    jq '.ors.services.routing.init_threads = 1' /ors-core/openrouteservice/src/main/resources/app.config |sponge /ors-core/openrouteservice/src/main/resources/app.config && \
    # Delete all profiles but car
    jq 'del(.ors.services.routing.profiles.active[1,2,3,4,5,6,7,8])' /ors-core/openrouteservice/src/main/resources/app.config |sponge /ors-core/openrouteservice/src/main/resources/app.config

COPY ./docker-entrypoint.sh /docker-entrypoint.sh

# Start the container
EXPOSE 8080
ENTRYPOINT ["/bin/bash", "/docker-entrypoint.sh"]
