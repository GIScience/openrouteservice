# Building from Source

If you need to customize your openrouteservice instance even further than what is possible by [configuration](/run-instance/configuration/index.md), or want to start [contributing](/contributing/index.md) to the openrouteservice project, the following section will give you starting points.

## Prerequisites

The following documentation assumes you are running an Ubuntu 20.04 system (also generally works with newer Ubuntu versions). Depending on your environment, you might need to adjust certain details. You will also need to make sure to have the following installed: 
* [java](https://www.java.com/en/) 17 (or higher) should be available, preferably as default Java environment.
* [maven](https://maven.apache.org/) should be installed on your system.
* [git](https://github.com/git-guides/install-git) should be available on your system if you want to download the source code conveniently.

## Download source code

If you have git installed, the easiest way to download the source code is to clone the repository with the following command:

```shell
git clone https://github.com/GIScience/openrouteservice.git
cd openrouteservice
```

This creates a directory named `openrouteservice` containing the downloaded source code. All following instructions will assume you are within that directory. 

If you do not have git installed on your system, you can also download the packed (`.zip` and `.tar.gz`) source code file from the "Assets" section of the desired release from our GitHub [releases](https://github.com/GIScience/openrouteservice/releases) page. Unpack the archive and run the following instructions within the directory you unpacked the source code into.

## Build JAR 

When your source code is set up, you can generate a runnable openrouteservice fat JAR:

```shell
mvn clean package -PbuildFatJar
```

Because JAR is the default, you can also run the command without `-PbuildFatJar`:

```shell
mvn clean package
```

You will find the fat JAR file in `ors-api/target/ors.jar`

The chapter on [JAR](running-jar.md) artifact explains how to configure and run the JAR file.

## Build WAR

When your source code is set up, you can generate a deployable openrouteservice WAR:

```shell
mvn clean package -PbuildWar
```

You will find the WAR file in `ors-api/target/ors.war`

The chapter on [WAR](running-war.md) artifact explains how to configure and deploy the WAR file.

## Run source code directly

You should be able to run the application directly with  

```shell
mvn spring-boot:run
```

or in your IDE (see below). This will start openrouteservice on port `8082` with the default configuration `ors-config.yml` in the project root directory
and a small OSM data set from Heidelberg.

In the [Configuration](configuration/index.md) section you find the options how you can use customised configurations.  

## For developers

If you need to customize openrouteservice more than what is possible by [Configuration](configuration/index.md) you might need to make changes to the code. If you implement features that might be useful for others as well, consider [contributing](/contributing/index.md)! 

The following instructions are useful to get you set up to start modifying the code.

### Running from within IDE

[//]: # (TODO: review below here)

To run the project from within your IDE, you have to:

1. Open IntelliJ and Create a project via File -> New -> Project from Existing Sources
2. Select the "pom.xml" file from the cloned "openrouteservice" folder and click "Open"
3. Click through project settings with "Next" until you reach the page for selecting project SDK.
4. Choose "17" as project SDK and click "Next"
5. Finalize the project import by clicking "Finish" in the last window.
6. Configure your IDE to run `spring-boot:run` as the maven goal. Now you can run your application directly in IntelliJ.
7. To use a different config file than ors-config.yml in the project directory, you can set the environment variable `ORS_CONFIG_LOCATION=<config>` in your run config. 
8. You can run all tests via JUnit.


### Running Tests

Running tests is essential if you change the code. Please always make sure that all tests are passing. Failing test sometimes indicate, that code changes break existing code. If the expected behavior of the application changes, it might also be necessary to change existing tests. For new functionality, new tests should be added.

It is very convenient to run unit tests (all or just one or some) in the IDE.
You can also run tests with maven on the command line. Here are some examples, checkout the maven documentation for more options.

```shell
mvn clean test                   # runs unit tests in all modules
mvn clean test -pl :ors-api      # runs unit tests in ors-api
mvn clean test -pl :ors-api -Dtest="APIEnumsTest" # run tests in a single test class
mvn clean test -pl :ors-api -Dtest="APIEnumsTest#testLanguagesEnumCreation" # or a single test method only
```

The api tests (in `ors-api/src/test/java/org/heigit/ors/apitests`) are excluded by default. 
To include them, add the option `-Papitests`, e.g.:

```shell
mvn clean verify -Papitests
```

If you want to run maven tasks without tests, add the option`-DskipTests`, e.g.:

```shell
mvn clean package -DskipTests
```


### Integrating GraphHopper

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
