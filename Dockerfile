FROM openjdk:8-jdk

ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"

ARG APP_CONFIG=docker/conf/app.config.sample
ARG OSM_FILE=docker/data/heidelberg.osm.gz
ARG JAVA_OPTS
ARG CATALINA_OPTS

# Install required deps
RUN apt-get update -qq
RUN apt-get install -qq -y locales wget nano maven

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

# Install tomcat
RUN mkdir /usr/local/tomcat
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v8.0.32/bin/apache-tomcat-8.0.32.tar.gz -O /tmp/tomcat.tar.gz

RUN cd /tmp && tar xvfz tomcat.tar.gz
RUN cp -R /tmp/apache-tomcat-8.0.32/* /usr/local/tomcat/

# Add tomcat custom settings if provided
RUN touch /usr/local/tomcat/bin/setenv.sh
RUN echo "CATALINA_OPTS=\"$CATALINA_OPTS\"" >> /usr/local/tomcat/bin/setenv.sh
RUN echo "JAVA_OPTS=\"$JAVA_OPTS\"" >> /usr/local/tomcat/bin/setenv.sh

# Copy ors app into tomcat webapps
RUN cp /ors-core/openrouteservice/target/*.war /usr/local/tomcat/webapps/ors.war

# Start the container
EXPOSE 8080
CMD /usr/local/tomcat/bin/catalina.sh run

