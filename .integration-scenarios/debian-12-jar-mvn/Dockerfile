# To execute this script locally, see the run.sh in the same folder
FROM docker.io/debian:bookworm-slim as builder
RUN apt-get update -y && \
    apt-get install -y openjdk-17-jdk-headless maven

# Create work directory init
WORKDIR /app
# Copy the project files into the container
COPY ors-api ors-api
COPY ors-engine ors-engine
COPY ors-report-aggregation ors-report-aggregation
COPY pom.xml pom.xml

# Build the project
RUN mvn -B clean package -DskipTests -PbuildFatJar -DCI=true



FROM docker.io/debian:bookworm-slim as final

RUN apt-get update -y && \
    apt-get install -y vim curl procps openjdk-17-jdk-headless maven jq

ARG HOME_DIR=/home/ors
ARG TESTFILES_DIR=/testfiles
ARG TESTSUITES_DIR=/testsuites
ARG REPO_DIR=/home/ors/openrouteservice
ARG UID=1000
ARG GID=1000

# Copy the jar file from the builder stage
COPY --from=builder /app/ors-api/target/ors.jar $TESTFILES_DIR/ors.jar

#COPY .integration-scenarios/debian-12-jar-mvn/testfiles $TESTFILES_DIR
#COPY .integration-scenarios/debian-12-jar-mvn/testsuites $TESTSUITES_DIR
COPY ors-api $REPO_DIR/ors-api
COPY ors-engine $REPO_DIR/ors-engine
COPY ors-report-aggregation $REPO_DIR/ors-report-aggregation
COPY pom.xml $REPO_DIR/pom.xml


#COPY --from=builder /app/ors-api/src/test/resources/application-test.yml /ors-config.yml
#COPY --from=builder /app/ors-api/src/test/files $REPO_DIR/files

RUN mkdir -p "/etc/openrouteservice"
RUN mkdir -p "$HOME_DIR/.config/openrouteservice"
RUN mkdir -p "$REPO_DIR/logs"
#RUN mkdir -p "$TESTFILES_DIR"
#RUN mkdir -p "$TESTSUITES_DIR"
RUN chmod -R 770 "$HOME_DIR"
RUN chmod -R 770 "$REPO_DIR"
#RUN chmod -R 770 "$TESTFILES_DIR"
#RUN chmod -R 770 "$TESTSUITES_DIR"

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV HOME_DIR=$HOME_DIR
ENV TESTFILES_DIR=$TESTFILES_DIR
ENV TESTSUITES_DIR=$TESTSUITES_DIR
ENV REPO_DIR=$REPO_DIR


# Expose ports
EXPOSE 8082
# Start systemd with the following command
#ENTRYPOINT [ "bash", "-c", "/home/ors/testsuites/warmup.sh" ]
#CMD [ "test-find-config-workdir.sh" ]