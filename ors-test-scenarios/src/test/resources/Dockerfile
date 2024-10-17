# Use this Dockerfile for custom testing with the following command from the repo root.
# Any volume mount is an example and optional.
# This Dockerfile constists of multiple targets: ors-test-scenarios-maven, ors-test-scenarios-jar, ors-test-scenarios-war
# To use the targets, you need to build the desired Dockerfile target.
# export ORS_DOCKERFILE_TARGET=ors-test-scenarios-maven
# docker build --target $ORS_DOCKERFILE_TARGET --tag $ORS_DOCKERFILE_TARGET:latest -f ors-test-scenarios/src/test/resources/Dockerfile .
# You can now run the container with the following command:
# docker run -d -it -e 'logging.level.org.heigit=DEBUG' --name $ORS_DOCKERFILE_TARGET -p 8082:8080 $ORS_DOCKERFILE_TARGET:latestL
# To customize the container you can mount custom elements and files. E.g.:
# docker run -d -it --name ors-test-scenarios-war \
#  -v /your-pbf-file.osm.pbf:/home/ors/openrouteservice/files/heidelberg.test.pbf \
#  -v /your/graph/public-transport:/home/ors/openrouteservice/graphs/public-transport \
#  -v /your/modified/ors.war:/usr/local/tomcat/webapps/ors.war \
#  -p 8082:8080 ors-test-scenarios:latest

ARG CONTAINER_BUILD_DIR=/build
ARG CONTAINER_WORK_DIR=/home/ors/openrouteservice

FROM docker.io/maven:3.9.9-amazoncorretto-21-alpine AS builder

RUN apk add --no-cache bash yq

ARG CONTAINER_BUILD_DIR

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
RUN mvn clean package -q -DskipTests -Dmaven.test.skip=true -PbuildWar -T 1C
RUN mvn package -q -DskipTests -Dmaven.test.skip=true -PbuildFatJar -T 1C

# Prepare the config file
COPY ors-config.yml $CONTAINER_BUILD_DIR/ors-config.yml

RUN yq -i '\
    .server.port = 8080 | \
    .logging.file.name = "/home/ors/openrouteservice/logs/ors.log" | \
    .logging.level.org.heigit = "INFO" | \
    .ors.engine.graphs_data_access = "MMAP" | \
    .ors.engine.elevation.cache_path = "/home/ors/openrouteservice/elevation_cache" | \
    .ors.engine.profile_default.graph_path = "/home/ors/openrouteservice/graphs" | \
    .ors.engine.profiles.public-transport.gtfs_file = "/home/ors/openrouteservice/files/vrn_gtfs_cut.zip" | \
    .ors.engine.profile_default.source_file = "/home/ors/openrouteservice/files/heidelberg.test.pbf"\
    ' $CONTAINER_BUILD_DIR/ors-config.yml

FROM builder AS ors-test-scenarios-maven-bare


ARG CONTAINER_WORK_DIR
ARG CONTAINER_BUILD_DIR

WORKDIR $CONTAINER_WORK_DIR

RUN mkdir -p $CONTAINER_WORK_DIR/logs $CONTAINER_WORK_DIR/files $CONTAINER_WORK_DIR/graphs $CONTAINER_WORK_DIR/elevation_cache \
    $CONTAINER_WORK_DIR/graph_repo

COPY --from=builder $CONTAINER_BUILD_DIR/ "$CONTAINER_WORK_DIR"/
COPY ors-api/src/test/files "$CONTAINER_WORK_DIR"/files
COPY ors-api/src/test/files/elevation "$CONTAINER_WORK_DIR"/elevation_cache
RUN mv $CONTAINER_WORK_DIR/ors-config.yml $CONTAINER_WORK_DIR/ors-config.yml.deactivated && \
    # Cache the dependencies to speed up the runtime process
    mvn install -q -o -DskipTests -Dmaven.test.skip=true -T 1C

EXPOSE 8080


FROM docker.io/amazoncorretto:21.0.4-alpine3.20 AS ors-test-scenarios-jar-bare

RUN apk add --no-cache bash yq

ARG CONTAINER_WORK_DIR
ARG CONTAINER_BUILD_DIR

WORKDIR $CONTAINER_WORK_DIR

COPY --from=builder $CONTAINER_BUILD_DIR/ors-api/target/ors.jar "$CONTAINER_WORK_DIR"/ors.jar
COPY --from=builder $CONTAINER_BUILD_DIR/ors-config.yml "$CONTAINER_WORK_DIR"/ors-config.yml.deactivated
COPY ors-api/src/test/files "$CONTAINER_WORK_DIR"/files
COPY ors-api/src/test/files/elevation "$CONTAINER_WORK_DIR"/elevation_cache


RUN mkdir -p $CONTAINER_WORK_DIR/logs $CONTAINER_WORK_DIR/files $CONTAINER_WORK_DIR/graphs $CONTAINER_WORK_DIR/elevation_cache \
    $CONTAINER_WORK_DIR/graph_repo


ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

EXPOSE 8080

FROM docker.io/tomcat:10.1.30-jdk21-temurin-jammy AS ors-test-scenarios-war-bare

RUN apt-get update

ARG CONTAINER_WORK_DIR
ARG CONTAINER_BUILD_DIR

# Create necessary directories and copy test files
RUN mkdir -p $CONTAINER_WORK_DIR/logs $CONTAINER_WORK_DIR/files $CONTAINER_WORK_DIR/graphs $CONTAINER_WORK_DIR/elevation_cache \
    $CONTAINER_WORK_DIR/graph_repo

# Copy the test files
COPY --from=builder $CONTAINER_BUILD_DIR/ors-api/target/ors.war /usr/local/tomcat/webapps/ors.war
COPY --from=builder $CONTAINER_BUILD_DIR/ors-config.yml /home/ors/openrouteservice/ors-config.yml.deactivated
COPY ors-api/src/test/files /home/ors/openrouteservice/files
COPY ors-api/src/test/files/elevation /home/ors/openrouteservice/elevation_cache

RUN echo 'export CATALINA_OPTS="$CATALINA_OPTS -server -XX:+UseParallelGC"' > /usr/local/tomcat/bin/setenv.sh && \
    echo 'export JAVA_OPTS="$JAVA_OPTS \
    -Dors.config.location=/home/ors/openrouteservice/ors-config.yml"' >> /usr/local/tomcat/bin/setenv.sh

# Set the working directory to Tomcat
WORKDIR /usr/local/tomcat

EXPOSE 8080

# ---------------------------
# Targets with config files
# ---------------------------

FROM ors-test-scenarios-maven-bare AS ors-test-scenarios-maven

ARG CONTAINER_WORK_DIR

RUN mv $CONTAINER_WORK_DIR/ors-config.yml.deactivated $CONTAINER_WORK_DIR/ors-config.yml

COPY ors-test-scenarios/src/test/resources/maven-entrypoint.sh $CONTAINER_WORK_DIR/maven-entrypoint.sh

ENV JAVA_OPTS="-Xmx350M"

# Command to run Maven
CMD ["sh", "maven-entrypoint.sh"]

FROM ors-test-scenarios-jar-bare AS ors-test-scenarios-jar

ARG CONTAINER_WORK_DIR

RUN mv $CONTAINER_WORK_DIR/ors-config.yml.deactivated $CONTAINER_WORK_DIR/ors-config.yml

ENV JAVA_OPTS="-Xmx350M"

# Command to run the JAR
CMD ["java", "-jar", "ors.jar"]

FROM ors-test-scenarios-war-bare AS ors-test-scenarios-war

ARG CONTAINER_WORK_DIR

RUN mv $CONTAINER_WORK_DIR/ors-config.yml.deactivated $CONTAINER_WORK_DIR/ors-config.yml

ENV JAVA_OPTS="-Xmx350M"

# Command to run Tomcat
CMD ["catalina.sh", "run"]
