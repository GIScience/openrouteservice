# The reason for a separate builder dockerfile is to avoid the need to build the image every time a container is started.
# Testcontainers modifies the dockerfiles when something down the road changes from the beginning on, resulting in many unnecessary rebuilds.
# The builders in this file the the execution container are very heavy and it's better to separate them.
# Keeping them separate makes it impossible for testcontainers to change the build context for the builder images.
# Look into the documentation under docs/technical-details/integration-tests.md for more information.

ARG CONTAINER_BUILD_DIR=/build
ARG CONTAINER_WORK_DIR=/home/ors/openrouteservice

FROM docker.io/maven:3.9.9-amazoncorretto-21-alpine AS ors-test-scenarios-builder

RUN apk add --no-cache bash=5.2.26-r0 yq=4.44.1-r2 zip=3.0-r12 && \
    rm -rf /var/cache/apk/*

ARG CONTAINER_BUILD_DIR

# Set the working directory
WORKDIR "$CONTAINER_BUILD_DIR"

# Copy pom.xml files
COPY pom.xml $CONTAINER_BUILD_DIR/pom.xml
COPY ors-api/pom.xml $CONTAINER_BUILD_DIR/ors-api/pom.xml
COPY ors-engine/pom.xml $CONTAINER_BUILD_DIR/ors-engine/pom.xml
COPY ors-report-aggregation/pom.xml $CONTAINER_BUILD_DIR/ors-report-aggregation/pom.xml
COPY ors-test-scenarios/pom.xml $CONTAINER_BUILD_DIR/ors-test-scenarios/pom.xml

# Cache the dependencies to speed up the build process
RUN mvn dependency:go-offline -B -q

# Copy project files
COPY ors-api "$CONTAINER_BUILD_DIR"/ors-api
COPY ors-engine "$CONTAINER_BUILD_DIR"/ors-engine
COPY ors-report-aggregation "$CONTAINER_BUILD_DIR"/ors-report-aggregation

# Build the projects war and jar files
RUN mvn clean package -q -DskipTests -Dmaven.test.skip=true -PbuildWar -pl \
    '!:ors-test-scenarios,!:ors-report-aggregation' && \
    mvn package install -q -DskipTests -Dmaven.test.skip=true -PbuildJar -pl \
    '!:ors-test-scenarios,!:ors-report-aggregation'

# Prepare the config file
COPY ors-config.yml "$CONTAINER_BUILD_DIR"/ors-config.yml

RUN yq -i '\
    .server.port = 8080 | \
    .logging.file.name = "/home/ors/openrouteservice/logs/ors.log" | \
    .logging.level.org.heigit = "INFO" | \
    .ors.engine.graphs_data_access = "MMAP" | \
    .ors.engine.elevation.profile_default.build.elevation = false | \
    .ors.engine.profile_default.graph_path = "/home/ors/openrouteservice/graphs" | \
    .ors.engine.profiles.public-transport.gtfs_file = "/home/ors/openrouteservice/files/vrn_gtfs_cut.zip" | \
    .ors.engine.profile_default.build.source_file = "/home/ors/openrouteservice/files/heidelberg.test.pbf"\
    ' "$CONTAINER_BUILD_DIR"/ors-config.yml


FROM docker.io/tomcat:10.1.30-jdk21-temurin-jammy AS ors-test-scenarios-war-builder
# Build: docker build --target ors-test-scenarios-war-bare --tag ors-test-scenarios-war-bare:latest -f ors-test-scenarios/src/test/resources/Dockerfile .

RUN apt-get update && apt-get install -y --no-install-recommends unzip=6.0-26ubuntu3 zip=3.0-12build2 && \
    wget https://github.com/mikefarah/yq/releases/download/v4.44.5/yq_linux_amd64.tar.gz -O - |\
    tar xz && mv yq_linux_amd64 /usr/bin/yq && chmod +x /usr/bin/yq && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ARG CONTAINER_WORK_DIR
ARG CONTAINER_BUILD_DIR

# Copy the test files
COPY --from=ors-test-scenarios-builder $CONTAINER_BUILD_DIR/ors-api/target/ors.war /usr/local/tomcat/webapps/ors.war
COPY --from=ors-test-scenarios-builder $CONTAINER_BUILD_DIR/ors-config.yml /home/ors/openrouteservice/ors-config.yml.deactivated
COPY ors-api/src/test/files/heidelberg.test.pbf "$CONTAINER_WORK_DIR"/files/heidelberg.test.pbf
COPY ors-api/src/test/files/vrn_gtfs_cut.zip "$CONTAINER_WORK_DIR"/files/vrn_gtfs_cut.zip

RUN echo 'export CATALINA_OPTS="$CATALINA_OPTS -server -XX:+UseParallelGC"' > /usr/local/tomcat/bin/setenv.sh

# Set the working directory to Tomcat
WORKDIR /usr/local/tomcat


FROM ors-test-scenarios-builder AS ors-test-scenarios-maven-builder

ARG CONTAINER_WORK_DIR
ARG CONTAINER_BUILD_DIR

WORKDIR $CONTAINER_WORK_DIR

COPY --from=ors-test-scenarios-builder $CONTAINER_BUILD_DIR "$CONTAINER_WORK_DIR"
COPY ors-api/src/test/files/heidelberg.test.pbf "$CONTAINER_WORK_DIR"/files/heidelberg.test.pbf
COPY ors-api/src/test/files/vrn_gtfs_cut.zip "$CONTAINER_WORK_DIR"/files/vrn_gtfs_cut.zip

RUN mvn install -q -DskipTests -Dmaven.test.skip=true -PbuildJar -pl \
    '!:ors-test-scenarios,!:ors-report-aggregation' && \
    cp -r /root/.m2 $CONTAINER_WORK_DIR/.m2
# Needed step for CI to persist cache layer
RUN mv "$CONTAINER_WORK_DIR"/.m2 /root/.m2

COPY ors-test-scenarios/src/test/resources/maven-entrypoint.sh $CONTAINER_WORK_DIR/maven-entrypoint.sh

RUN mv "$CONTAINER_WORK_DIR"/ors-config.yml "$CONTAINER_WORK_DIR"/ors-config.yml.deactivated

ENV JAVA_OPTS="-Xmx350M"

FROM docker.io/amazoncorretto:21.0.4-alpine3.20 AS ors-test-scenarios-jar-builder
# Build: docker build --target ors-test-scenarios-jar-bare --tag ors-test-scenarios-jar-bare:latest -f ors-test-scenarios/src/test/resources/Dockerfile .
RUN apk add --no-cache bash=5.2.26-r0 yq=4.44.1-r2 zip=3.0-r12

ARG CONTAINER_WORK_DIR
ARG CONTAINER_BUILD_DIR

WORKDIR $CONTAINER_WORK_DIR

COPY --from=ors-test-scenarios-builder $CONTAINER_BUILD_DIR/ors-api/target/ors.jar "$CONTAINER_WORK_DIR"/ors.jar
COPY --from=ors-test-scenarios-builder $CONTAINER_BUILD_DIR/ors-config.yml "$CONTAINER_WORK_DIR"/ors-config.yml.deactivated
COPY ors-api/src/test/files/heidelberg.test.pbf "$CONTAINER_WORK_DIR"/files/heidelberg.test.pbf
COPY ors-api/src/test/files/vrn_gtfs_cut.zip "$CONTAINER_WORK_DIR"/files/vrn_gtfs_cut.zip

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
