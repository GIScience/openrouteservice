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

# Set the working directory
WORKDIR /build

# Copy pom.xml files
COPY pom.xml /build/pom.xml
COPY ors-api/pom.xml /build/ors-api/pom.xml
COPY ors-engine/pom.xml /build/ors-engine/pom.xml
COPY ors-report-aggregation/pom.xml /build/ors-report-aggregation/pom.xml

# Cache the dependencies to speed up the build process
RUN mvn dependency:go-offline -B -q

# Copy project files
COPY ors-api /build/ors-api
COPY ors-engine /build/ors-engine
COPY ors-report-aggregation /build/ors-report-aggregation

# Build the project
RUN mvn clean package -q -DskipTests -PbuildWar -T$(nproc)

FROM docker.io/tomcat:10.1.30-jdk21-temurin-noble AS final

# Create necessary directories and copy test files
RUN mkdir -p /usr/local/tomcat/logs /usr/local/tomcat/temp /usr/local/tomcat/conf && \
    mkdir -p /home/ors/openrouteservice/logs /home/ors/openrouteservice/files /home/ors/openrouteservice/graphs /home/ors/openrouteservice/elevation_cache \

# Set environment variables
RUN echo 'export CATALINA_OPTS="$CATALINA_OPTS -Xms500M -Xmx2G -server -XX:+UseParallelGC"' > /usr/local/tomcat/bin/setenv.sh
RUN echo 'export JAVA_OPTS="$JAVA_OPTS \
-Dlogging.file.name=/home/ors/openrouteservice/logs/ors.log \
-Dlogging.level.org.heigit=DEBUG \
-Dors.config.location=/home/ors/openrouteservice/conf/ors-config.yml \
-Dors.engine.graphs_data_access=MMAP \
-Dors.engine.elevation.cache_path=/home/ors/openrouteservice/elevation_cache \
-Dors.engine.profile_default.graph_path=/home/ors/openrouteservice/graphs \
-Dors.engine.profile.public-transport.gtfs_file=/home/ors/openrouteservice/files/vrn_gtfs_cut.zip \
-Dors.engine.profile_default.source_file=/home/ors/openrouteservice/files/heidelberg.test.pbf"' >> /usr/local/tomcat/bin/setenv.sh

# Copy the test files
COPY --from=builder /build/ors-api/target/ors.war /usr/local/tomcat/webapps/ors.war

COPY ors-api/src/test/files/elevation/srtm_38_03.gh /home/ors/openrouteservice/elevation_cache/srtm_38_03.gh
COPY ors-api/src/test/files/vrn_gtfs_cut.zip /home/ors/openrouteservice/files/vrn_gtfs_cut.zip
COPY ors-config.yml /home/ors/openrouteservice/conf/ors-config.yml
COPY ors-api/src/test/files/heidelberg.test.pbf /home/ors/openrouteservice/files/heidelberg.test.pbf

# Set the working directory to Tomcat
WORKDIR /usr/local/tomcat

EXPOSE 8082

# Command to run Tomcat
CMD ["catalina.sh", "run"]
