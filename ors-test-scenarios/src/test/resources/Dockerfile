# Use this Dockerfile for custom testing with the following command from the repo root.
# Any volume mount is an example and optional.
# To use other container paths please temporary rewrite the setenv.sh section in this Dockerfile
# docker build --tag ors-test-scenarios:latest -f ors-test-scenarios/src/test/resources/ubuntu-jammy-tomcat-war/Dockerfile .
#  docker run -d -it --name bkg-test-war \
#  -v /your-pbf-file.osm.pbf:/home/ors/openrouteservice/files/heidelberg.test.pbf \
#  -v /your/graph/public-transport:/home/ors/openrouteservice/graphs/public-transport \
#  -v /your/modified/ors.war:/usr/local/tomcat/webapps/ors.war \
#  -p 8082:8080 ors-test-scenarios:latest
FROM docker.io/maven:3.9.9-amazoncorretto-21-alpine AS builder

RUN apk add --no-cache bash yq

ARG CONTAINER_BUILD_DIR=/build

# Set the working directory
WORKDIR $CONTAINER_BUILD_DIR

# Copy pom.xml files
COPY pom.xml $CONTAINER_BUILD_DIR/pom.xml
COPY ors-api/pom.xml $CONTAINER_BUILD_DIR/ors-api/pom.xml
COPY ors-engine/pom.xml $CONTAINER_BUILD_DIR/ors-engine/pom.xml
COPY ors-report-aggregation/pom.xml $CONTAINER_BUILD_DIR/ors-report-aggregation/pom.xml

# Cache the dependencies to speed up the build process
RUN mvn dependency:go-offline -B -q

# Copy project files
COPY ors-api $CONTAINER_BUILD_DIR/ors-api
COPY ors-engine $CONTAINER_BUILD_DIR/ors-engine
COPY ors-report-aggregation $CONTAINER_BUILD_DIR/ors-report-aggregation

# Build the projects war and jar files
RUN mvn clean package -q -DskipTests -PbuildWar -T$(nproc)
RUN mvn package -q -DskipTests -PbuildFatJar -T$(nproc)

# Prepare the config file
COPY ors-config.yml $CONTAINER_BUILD_DIR/ors-config.yml

RUN yq -i '\
    .server.port = 8080 | \
    .logging.file.name = "/home/ors/openrouteservice/logs/ors.log" | \
    .logging.level.org.heigit = "INFO" | \
    .ors.engine.graphs_data_access = "MMAP" | \
    .ors.engine.elevation.cache_path = "/home/ors/openrouteservice/elevation_cache" | \
    .ors.engine.profile_default.graph_path = "/home/ors/openrouteservice/graphs" | \
    .ors.engine.profile.public-transport.gtfs_file = "/home/ors/openrouteservice/files/vrn_gtfs_cut.zip" | \
    .ors.engine.profile_default.source_file = "/home/ors/openrouteservice/files/heidelberg.test.pbf"\
    ' $CONTAINER_BUILD_DIR/ors-config.yml
FROM docker.io/amazoncorretto:21.0.4-alpine3.20 AS ors-test-scenarios-jar

RUN apk add --no-cache bash

ARG CONTAINER_WORK_DIR=/home/ors/openrouteservice

WORKDIR $CONTAINER_WORK_DIR

COPY --from=builder /build/ors-api/target/ors.jar "$CONTAINER_WORK_DIR"/ors.jar
COPY --from=builder /build/ors-config.yml "$CONTAINER_WORK_DIR"/ors-config.yml
COPY ors-api/src/test/files "$CONTAINER_WORK_DIR"/files
COPY ors-api/src/test/files/elevation "$CONTAINER_WORK_DIR"/elevation_cache


RUN mkdir -p graphs /home/ors/graph_repo "$CONTAINER_WORK_DIR" && \
    chmod -R 770 "$CONTAINER_WORK_DIR"


ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "ors.jar" ]
FROM docker.io/tomcat:10.1.30-jdk21-temurin-jammy AS ors-test-scenarios-war

ARG CONTAINER_WORK_DIR=/home/ors/openrouteservice

# Create necessary directories and copy test files
RUN mkdir -p /usr/local/tomcat/logs /usr/local/tomcat/temp /usr/local/tomcat/conf && \
    mkdir -p /home/ors/openrouteservice/logs /home/ors/openrouteservice/files /home/ors/openrouteservice/graphs /home/ors/openrouteservice/elevation_cache

# Copy the test files
COPY --from=builder /build/ors-api/target/ors.war /usr/local/tomcat/webapps/ors.war
COPY --from=builder /build/ors-config.yml /home/ors/openrouteservice/ors-config.yml
COPY ors-api/src/test/files /home/ors/openrouteservice/files
COPY ors-api/src/test/files/elevation /home/ors/openrouteservice/elevation_cache

RUN echo 'export CATALINA_OPTS="$CATALINA_OPTS -Xms225M -Xmx1500M -server -XX:+UseParallelGC"' > /usr/local/tomcat/bin/setenv.sh && \
    echo 'export JAVA_OPTS="$JAVA_OPTS \
    -Dors.config.location=/home/ors/openrouteservice/ors-config.yml"' >> /usr/local/tomcat/bin/setenv.sh

# Set the working directory to Tomcat
WORKDIR /usr/local/tomcat

EXPOSE 8080

# Command to run Tomcat
CMD ["catalina.sh", "run"]

