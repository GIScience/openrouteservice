FROM maven:3-jdk-8

# This will supress any download for dependencies and plugins or upload messages which would clutter the console log.
# `showDateTime` will show the passed time in milliseconds. You need to specify `--batch-mode` to make this work.
ENV MAVEN_OPTS="-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
# As of Maven 3.3.0 instead of this you may define these options in `.mvn/maven.config` so the same config is used
# when running from the command line.
# `installAtEnd` and `deployAtEnd`are only effective with recent version of the corresponding plugins.
ENV MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true"
ARG APP_CONFIG=docker/conf/app.config.sample

RUN mkdir -p /ors-core/build

COPY .git /ors-core/.git
COPY openrouteservice /ors-core/openrouteservice
COPY $APP_CONFIG /ors-core/openrouteservice/WebContent/WEB-INF/app.config

WORKDIR /ors-core

# Build and install openrouteservice
RUN mvn -f ./openrouteservice/pom.xml package -DskipTests 

CMD ORS_VER=$(mvn -f ./openrouteservice/pom.xml -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec) && cp /ors-core/openrouteservice/target/openrouteservice-$ORS_VER.war /ors-core/build/ors.war
