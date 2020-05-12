FROM openjdk:8-jdk

ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"

ARG APP_CONFIG=./docker/conf/app.config.sample
ARG OSM_FILE=./docker/data/heidelberg.osm.gz

WORKDIR /ors-core

COPY openrouteservice /ors-core/openrouteservice
COPY $OSM_FILE /ors-core/data/osm_file.pbf
COPY $APP_CONFIG /ors-core/openrouteservice/src/main/resources/app.config

# Install tomcat
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v8.0.32/bin/apache-tomcat-8.0.32.tar.gz -O /tmp/tomcat.tar.gz && \
    cd /tmp && \
    tar xvfz tomcat.tar.gz && \
    mkdir /usr/local/tomcat && \
    cp -R /tmp/apache-tomcat-8.0.32/* /usr/local/tomcat/ && \
    cd /ors-core && \
    apt-get update -qq && apt-get install -qq -y locales nano maven && \
    locale-gen en_US.UTF-8 && \
    mvn -q -f /ors-core/openrouteservice/pom.xml package -DskipTests && \
    cp -f /ors-core/openrouteservice/target/*.war /usr/local/tomcat/webapps/ors.war && \
    mkdir /ors-conf

COPY ./docker-entrypoint.sh /docker-entrypoint.sh

# Start the container
EXPOSE 8080
ENTRYPOINT ["/bin/bash", "/docker-entrypoint.sh"]
