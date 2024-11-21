# Openrouteservice Integration tests

::: warning
The integration tests are quite time-consuming and an advanced topic in openrouteservice.
They should only be run if necessary and as part of active development.
:::

The integration tests are located in the `ors-test-scenarios/` module.
The tests execute a set of predefined scenarios against the running ORS instance.
The tests are written in Java and use the JUnit and testcontainers framework.

## Prerequisites

To run the tests you need the following prerequisites:

- Docker
- Maven
- Java 17+

## General

You can execute the tests like any other unit test.
The tests are quite excessive with resources and execution time.
To avoid accidental execution the tests are not included in the default test execution.
Add `-P integrationTests` to the maven command to execute the tests.

Important content and explanation:

```shell
ors-test-scenarios/
├── ors-test-scenarios/graphs-integrationtests/
│   └── ors-test-scenarios/graphs-integrationtests/sharedGraphMount/ # Contains the shared graph mount for the tests to avoid rebuilding the graph. Can be deleted.
├── ors-test-scenarios/pom.xml # The pom file for the integration tests.
└── ors-test-scenarios/src/
    └── ors-test-scenarios/src/test/
        ├── ors-test-scenarios/src/test/java/
        │   ├── ors-test-scenarios/src/test/java/integrationtests/ # Contains the integration tests.
        │   └── ors-test-scenarios/src/test/java/utils/ # Contains the helper classes for the tests.
        └── ors-test-scenarios/src/test/resources/
            ├── ors-test-scenarios/src/test/resources/Builder.Dockerfile # The Dockerfile to create the builder images.
            ├── ors-test-scenarios/src/test/resources/war.Dockerfile # The Dockerfile to create war test images.
            ├── ors-test-scenarios/src/test/resources/jar.Dockerfile # The Dockerfile to create jar test images.
            ├── ors-test-scenarios/src/test/resources/maven.Dockerfile # The Dockerfile to create maven test images.
            ├── ors-test-scenarios/src/test/resources/junit-platform.properties # The config file for the JUnit platform.
            ├── ors-test-scenarios/src/test/resources/logback.xml # Testcontaieners usese logback
            └── ors-test-scenarios/src/test/resources/maven-entrypoint.sh # The entrypoint for the maven test image.
```

## Most important examples:

```shell
# Execute all tests from the repo root with testcontainers.
mvn -pl ors-test-scenarios test -P integrationTests  
# Execute only war tests
mvn -pl ors-test-scenarios test -P integrationTests -Dcontainer.run.scenario=war
```

## Advanced test runs

The test module provides multiple properties to customize the test execution:

- `container.run.scenario` - The scenario to run the tests against. Options are:
    - `maven`,
    - `jar`,
    - `war`,
    - `all` (default).
- `container.builder.use_prebuild` - Use the prebuild builder images. Default is `false`.
- `container.run.share_graphs` - Share the graphs between the tests. Default is `true`. **Only chane this if you know
  what
  you are doing.**

The `container.builder.use_prebuild` property is useful if you want to use custom prebuild builder images.
Testcontainers uses Docker to build the images for the tests.
If necessary, one can inject custom builder images to the tests.
The builder images can be build e.g. with buildx and pushed to the local registry.
This allows the user to with e.g. build with caches, build more efficiently and faster.
This is mainly used in the workflows or for heavy local testing.

###### Examples:

```shell
# Run tests against the war/tomcat setup with builder images controlled by `testcontainers` and shared graphs.
mvn -pl ors-test-scenarios test -P integrationTests -Dcontainer.run.scenario=war
```

```shell
# Build and run the tests with the custom builder images and maven setup
docker buildx build --target ors-test-scenarios-maven-builder -t ors-test-scenarios-maven-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
mvn -pl ors-test-scenarios test -P integrationTests \
-Dcontainer.run.scenario=maven \
-Dcontainer.builder.use_prebuild=true
```

## Pre-Build builder images

The run dockerfiles and build `Bulid.Dockerfile` are separated to allow for custom builder images.
This is also necessary at times because testcontainers own builder images caching strategy is not always optimal for CI.

The scenario oriented dockerfiles `war.Dockerfile`, `jar.Dockerfile` and `maven.Dockerfile` is only for the final
stages.
To be able to build it without testcontainers, the `Build.Dockerfile` must be executed first.
Choose one target of the following targets:

- ors-test-scenarios-maven-builder
- ors-test-scenarios-jar-builder
- ors-test-scenarios-war-builder

###### Examples:

Execute the jar test scenario with a custom builder image:

```shell
# Choose from: jar, war, maven.
export TEST_SCENARIO=jar
# Execute the jar setup with a custom builder image
docker buildx build --target ors-test-scenarios-${TEST_SCENARIO}-builder -t ors-test-scenarios-${TEST_SCENARIO}-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
mvn -pl ors-test-scenarios test -P integrationTests \
  -Dcontainer.run.scenario=${TEST_SCENARIO} \
  -Dcontainer.builder.use_prebuild=true
```

Execute all scenarios and tests with a custom builder image:

```shell
# Execute all scenarios and tests with a custom builder image
docker buildx build --target ors-test-scenarios-jar-builder -t ors-test-scenarios-jar-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
docker buildx build --target ors-test-scenarios-war-builder -t ors-test-scenarios-war-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
docker buildx build --target ors-test-scenarios-maven-builder -t ors-test-scenarios-maven-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .

mvn -pl ors-test-scenarios test -P integrationTests \
  -Dcontainer.run.scenario=all \
  -Dcontainer.builder.use_prebuild=true
```

## Setup local test containers

Sometimes one need a reproducible test environment container to test things out.
Each test scenario can be run in a container. The following containers are available:

- `ors-test-scenarios-maven`: Starts a fully functional ORS maven setup.
- `ors-test-scenarios-jar`: Starts a fully functional ORS jar setup.
- `ors-test-scenarios-war`: Starts a fully functional ORS `war/tomcat` setup.

###### Examples:

```shell
# Fully functional ORS war/tomcat setup
# Build the custom builder image
export TEST_SCENARIO=war
docker buildx build --target ors-test-scenarios-${TEST_SCENARIO}-builder -t ors-test-scenarios-${TEST_SCENARIO}-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
docker buildx build --target ors-test-scenarios-${TEST_SCENARIO} -t ors-test-scenarios-${TEST_SCENARIO}:latest -f ors-test-scenarios/src/test/resources/${TEST_SCENARIO}.Dockerfile .
docker run -it --rm --name ors-test-scenarios-${TEST_SCENARIO} -p 8080:8080 ors-test-scenarios-${TEST_SCENARIO}
```

Run the ORS war setup with a custom PBF file, cache the graphs and activate the hgv profile:

```shell
# Basic builder image setup.
export TEST_SCENARIO=war
docker buildx build --target ors-test-scenarios-${TEST_SCENARIO}-builder -t ors-test-scenarios-${TEST_SCENARIO}-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
docker buildx build --target ors-test-scenarios-${TEST_SCENARIO} -t ors-test-scenarios-${TEST_SCENARIO}:latest -f ors-test-scenarios/src/test/resources/${TEST_SCENARIO}.Dockerfile .
# Run the container with the custom war file and cache the graphs
docker run --rm -it --name ors-test-scenarios-${TEST_SCENARIO} -p 8080:8080 \
  -v ./ors-api/src/test/files/heidelberg.test.pbf:/home/ors/openrouteservice/files/heidelberg.test.pbf \
  -v ./your-local-graph-${TEST_SCENARIO}:/home/ors/openrouteservice/graphs \
  -e "ors.engine.profiles.driving-hgv.enabled=true" \
  ors-test-scenarios-${TEST_SCENARIO}
```

Side load a custom war file for testing:

```shell
# Basic builder image setup.
docker buildx build --target ors-test-scenarios-war-builder -t ors-test-scenarios-war-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
docker buildx build --target ors-test-scenarios-war -t ors-test-scenarios-war:latest -f ors-test-scenarios/src/test/resources/war.Dockerfile .

# Build the custom war file
mvn clean package -DskipTests -PbuildWar

# Run the container with the custom war file
docker run --rm -it --name ors-test-scenarios-war -p 8080:8080 \
  -v ./your-local-graph-war:/home/ors/openrouteservice/graphs \
  -v ./ors-api/target/ors.war:/usr/local/tomcat/webapps/ors.war \
  ors-test-scenarios-war
```

Side load a custom jar file for testing:

```shell
# Basic builder image setup.
docker buildx build --target ors-test-scenarios-jar-builder -t ors-test-scenarios-jar-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
docker buildx build --target ors-test-scenarios-jar -t ors-test-scenarios-jar:latest -f ors-test-scenarios/src/test/resources/jar.Dockerfile .

# Build the custom jar file
mvn clean package -DskipTests

# Run the container with the custom jar file
docker run --rm -it --name ors-test-scenarios-jar -p 8080:8080 \
  -v ./your-local-graph-jar:/home/ors/openrouteservice/graphs \
  -v ./ors-api/target/ors.jar:/home/ors/openrouteservice/ors.jar \
  ors-test-scenarios-jar
```

Side load a custom maven setup for testing:

```shell
# Basic builder image setup.
docker buildx build --target ors-test-scenarios-maven-builder -t ors-test-scenarios-maven-builder:latest -f ors-test-scenarios/src/test/resources/Builder.Dockerfile .
docker buildx build --target ors-test-scenarios-maven -t ors-test-scenarios-maven:latest -f ors-test-scenarios/src/test/resources/maven.Dockerfile .

mvn clean package -DskipTests

# Run the container with the custom maven setup
docker run --rm -it --name ors-test-scenarios-maven -p 8080:8080 \
  -v ./your-local-graph-maven:/home/ors/openrouteservice/graphs \
  -v ./ors-api:/home/ors/openrouteservice/ors-api \
  -v ./ors-engine:/home/ors/openrouteservice/ors-engine \
  ors-test-scenarios-maven
```