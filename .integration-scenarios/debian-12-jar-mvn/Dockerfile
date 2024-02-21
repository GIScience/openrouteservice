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

ARG WORK_DIR=/home/ors/openrouteservice
ARG CONF_DIR_USER=/root/.config/openrouteservice
ARG CONF_DIR_ETC=/etc/openrouteservice
ARG TESTFILES_DIR=/testfiles
ARG TESTSUITES_DIR=/testsuites
ARG UID=1000
ARG GID=1000

# Copy the jar file from the builder stage
COPY --from=builder /app/ors-api/target/ors.jar $TESTFILES_DIR/ors.jar

#COPY .integration-scenarios/debian-12-jar-mvn/testfiles $TESTFILES_DIR
#COPY .integration-scenarios/debian-12-jar-mvn/testsuites $TESTSUITES_DIR
COPY ors-api $WORK_DIR/ors-api
COPY ors-engine $WORK_DIR/ors-engine
COPY ors-report-aggregation $WORK_DIR/ors-report-aggregation
COPY pom.xml $WORK_DIR/pom.xml


#COPY --from=builder /app/ors-api/src/test/resources/application-test.yml /ors-config.yml
#COPY --from=builder /app/ors-api/src/test/files $WORK_DIR/files

RUN mkdir -p "$CONF_DIR_USER"
RUN mkdir -p "$CONF_DIR_ETC"
RUN mkdir -p "$WORK_DIR/logs"
#RUN mkdir -p "$TESTFILES_DIR"
#RUN mkdir -p "$TESTSUITES_DIR"
RUN chmod -R 770 "$WORK_DIR"
#RUN chmod -R 770 "$TESTFILES_DIR"
#RUN chmod -R 770 "$TESTSUITES_DIR"

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV WORK_DIR=$WORK_DIR
ENV CONF_DIR_USER=$CONF_DIR_USER
ENV CONF_DIR_ETC=$CONF_DIR_ETC
ENV TESTFILES_DIR=$TESTFILES_DIR
ENV TESTSUITES_DIR=$TESTSUITES_DIR


# Expose ports
EXPOSE 8082
# Start systemd with the following command
#ENTRYPOINT [ "bash", "-c", "/home/ors/testsuites/warmup.sh" ]
#CMD [ "test-find-config-workdir.sh" ]