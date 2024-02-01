# Building from Source

If you need to customize your openrouteservice instance even further than what is possible by [configuration](/run-instance/configuration/), or want to start contributing to the openrouteservice project, the following section will give you starting points.

## Prerequisites

The following documentation assumes you are running an Ubuntu 20.04 system (also generally works with newer Ubuntu versions). Depending on your environment, you might need to adjust certain details. You will also need to make sure to have the following installed: 
* [git](https://github.com/git-guides/install-git) should be available on your system
* [java](https://www.java.com/en/) 17 should be available, preferably as default Java environment
* [maven](https://maven.apache.org/) should be installed on your system

## Installation from source

To build openrouteservice from source, you can use the following commands:
```shell
git clone https://github.com/user/openrouteservice.git
cd openrouteservice
mvn package
```

If you have made modifications to the code, you should run all tests before building and using openrouteservice. For a significant part of the tests you need to activate a maven profile called `apitests` to run them. Use the following commands:

```shell
mvn -Papitests verify
# afterwards you can build the artefacts without running the tests again by issuing the following command:
mvn -DskipTests package
```

[//]: # (TODO: overhaul contents below after integrating the jar build PR)
After running this command, you will find the artefact at `ors-api/target/ors.war`. 

After you have packaged openrouteservice, there are two options for running it. One is to run the `mvn spring-boot:run` command which triggers a spring-boot native Tomcat instance running on port `8082`.  This is more restrictive in terms of settings for Tomcat. The other is to install and run Tomcat 10. In both cases the requirements of configuration and source files discussed [here](running-jar-war) apply.

[//]: # (TODO: the part below partly belongs somewhere in the contributing section; running with IDE needs to be overhauled, too, since it does not mention running with spring boot run config)

## Running from within IDE

To run the project from within your IDE, you have to:

  1. Set up your IDE project and import `openrouteservice`
     modules as Maven model.
     For IntelliJ Idea, have a look at [these instructions](/contributing/opening-project-in-intellij).

  2. Configure your IDE to run `spring-boot:run` as the maven goal, setting the
     environment variable `ORS_CONFIG_LOCATION=ors-config.yml`.

  3. You can run all tests via JUnit.


## Integrating GraphHopper

If you need to make adjustments to our forked and edited [GraphHopper repository](https://github.com/GIScience/graphhopper), follow these steps:

1. Clone and checkout `ors_4.0` branch:

   ```shell
   git clone https://github.com/GIScience/graphhopper.git
   cd graphhopper
   git checkout ors_4.0
   ```

2. Build the project to create the local snapshot.

   ```shell
    mvn package
   ```

3. Change the `ors-engine/pom.xml`:

   ```xml
    <!-- remove the comment to enable debugging // [!code ++]
    <dependency>
        <groupId>com.github.GIScience.graphhopper</groupId>
        <artifactId>graphhopper-core</artifactId>
        <version>v4.9.1</version>
        <exclusions>
            <exclusion>
                <groupId>com.fasterxml.jackson.dataformat</groupId>
                <artifactId>jackson-dataformat-xml</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>com.github.GIScience.graphhopper</groupId>
        <artifactId>graphhopper-reader-gtfs</artifactId>
        <version>v4.9.1</version>
        <exclusions>
            <exclusion>
                <groupId>com.fasterxml.jackson.dataformat</groupId>
                <artifactId>jackson-dataformat-xml</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>com.github.GIScience.graphhopper</groupId>
        <artifactId>graphhopper-map-matching</artifactId>
        <version>v4.9.1</version>
        <exclusions>
            <exclusion>
                <groupId>com.fasterxml.jackson.dataformat</groupId>
                <artifactId>jackson-dataformat-xml</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- remove the comment to enable debugging // [!code --]
    --> // [!code ++]
    <dependency>
        <groupId>com.github.GIScience.graphhopper</groupId>
        <artifactId>graphhopper-core</artifactId>
        <version>4.9-SNAPSHOT</version>
        <exclusions>
            <exclusion>
                <groupId>com.fasterxml.jackson.dataformat</groupId>
                <artifactId>jackson-dataformat-xml</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>com.github.GIScience.graphhopper</groupId>
        <artifactId>graphhopper-reader-gtfs</artifactId>
        <version>4.9-SNAPSHOT</version>
        <exclusions>
            <exclusion>
                <groupId>com.fasterxml.jackson.dataformat</groupId>
                <artifactId>jackson-dataformat-xml</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>com.github.GIScience.graphhopper</groupId>
        <artifactId>graphhopper-map-matching</artifactId>
        <version>4.9-SNAPSHOT</version>
        <exclusions>
            <exclusion>
                <groupId>com.fasterxml.jackson.dataformat</groupId>
                <artifactId>jackson-dataformat-xml</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    --> // [!code --]
   ```

4. Test your new functionality and run all tests after rebasing your feature branch with the latest `main` branch. Adjust tests if necessary.

5. If successful, create a PR for both [openrouteservice](https://github.com/GIScience/openrouteservice/pulls) and [GraphHopper](https://github.com/GIScience/graphhopper/pulls) against `master` and `ors_4.0` branches, respectively.
