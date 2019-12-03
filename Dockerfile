FROM openjdk:8-jdk

ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"

ARG APP_CONFIG=./docker/conf/app.config.sample
ARG OSM_FILE=./docker/data/heidelberg.osm.gz
# Install tomcat
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v8.0.32/bin/apache-tomcat-8.0.32.tar.gz -O /tmp/tomcat.tar.gz

# Install required deps
RUN apt-get update -qq && apt-get install -qq -y locales wget nano maven

# Set the locale
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN mkdir /ors-core
# Copy ors sources
COPY openrouteservice /ors-core/openrouteservice

# Copy osm data file, config and cache if provided (ors will download otherwise)
COPY $OSM_FILE /ors-core/data/osm_file.pbf
COPY $APP_CONFIG /ors-core/openrouteservice/src/main/resources/app.config

WORKDIR /ors-core

# Build openrouteservice
RUN mvn -q -f ./openrouteservice/pom.xml package -DskipTests

RUN cd /tmp && tar xvfz tomcat.tar.gz && mkdir /usr/local/tomcat && cp -R /tmp/apache-tomcat-8.0.32/* /usr/local/tomcat/

# Copy ors app into tomcat webapps
RUN cp -f /ors-core/openrouteservice/target/*.war /usr/local/tomcat/webapps/ors.war

COPY ./docker-entrypoint.sh /docker-entrypoint.sh

# Start the container
EXPOSE 8080
ENTRYPOINT ["/bin/bash", "/docker-entrypoint.sh"]
