# Building from Source

If you need to customize your openrouteservice instance even further than what is possible by [configuration](/run-instance/configuration/), or want to start contributing to the openrouteservice project, the following section will give you starting points.

## Prerequisites

The following documentation assumes you are running an Ubuntu 20.04 system (also generally works with newer Ubuntu versions). Depending on your environent, you might need to adjust certain details. You will also need to make sure to have the following installed: 
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

If you have made modification to the code, you should run all tests before building and using openrouteservice. For a significant part of the tests you need to activate a maven profile called `apitests` to run them. Use the following commands:

```shell
mvn -Papitests verify
# afterwards you can build the artefacts without running the tests again by issueing the folliwing command:
mvn -DskipTests package
```

[//]: # (TODO: overhaul contents below after integrating the jar build PR)
After running this command, you will find the artefact at `ors-api/target/ors.war`. 

After you have packaged openrouteservice, there are two options for running it.
One is to run the `mvn spring-boot:run` command which triggers a spring-boot native
Tomcat instance running on port `8082`.  This is more restrictive in terms of
settings for Tomcat. The other is to install and run Tomcat 9.  

[//]: # (TODO: the part below belongs somewhere in the contributing section)

## Running from within IDE

To run the project from within your IDE, you have to:

  1. Set up your IDE project and import `openrouteservice`
     modules as Maven model.
     For IntelliJ Idea, have a look at [these instructions](/contributing/opening-project-in-intellij).

  2. Configure your IDE to run `spring-boot:run` as the maven goal, setting the
     environment variable `ORS_CONFIG=ors-config-test.json`.

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
   <!--
   <dependency>
       <groupId>com.github.GIScience.graphhopper</groupId>
       <artifactId>graphhopper-core</artifactId>
       <version>v4.5.2</version>
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
   <version>v4.5.2</version>
   <exclusions>
       <exclusion>
           <groupId>com.fasterxml.jackson.dataformat</groupId>
           <artifactId>jackson-dataformat-xml</artifactId>
       </exclusion>
   </exclusions>
   </dependency>

   <dependency>
   <groupId>com.github.GIScience.graphhopper</groupId>
   <artifactId>graphhopper-web-api</artifactId>
   <version>v4.5.2</version>
   <exclusions>
       <exclusion>
           <groupId>com.fasterxml.jackson.dataformat</groupId>
           <artifactId>jackson-dataformat-xml</artifactId>
       </exclusion>
   </exclusions>
   </dependency>
   -->

   <dependency>
   <groupId>com.graphhopper</groupId>
   <artifactId>graphhopper-core</artifactId>
   <version>4.0-SNAPSHOT</version>
   <exclusions>
       <exclusion>
           <groupId>com.fasterxml.jackson.dataformat</groupId>
           <artifactId>jackson-dataformat-xml</artifactId>
       </exclusion>
   </exclusions>
   </dependency>

   <dependency>
   <groupId>com.graphhopper</groupId>
   <artifactId>graphhopper-web-api</artifactId>
   <version>4.0-SNAPSHOT</version>
   <exclusions>
       <exclusion>
           <groupId>com.fasterxml.jackson.dataformat</groupId>
           <artifactId>jackson-dataformat-xml</artifactId>
       </exclusion>
   </exclusions>
   </dependency>

   <dependency>
   <groupId>com.graphhopper</groupId>
   <artifactId>graphhopper-reader-gtfs</artifactId>
   <version>4.0-SNAPSHOT</version>
   <exclusions>
       <exclusion>
           <groupId>com.fasterxml.jackson.dataformat</groupId>
           <artifactId>jackson-dataformat-xml</artifactId>
       </exclusion>
   </exclusions>
   </dependency>
   ```

4. Test your new functionality and run all tests after rebasing your feature branch with the latest `development` branch. Adjust tests if necessary.

5. If successful, create a PR for both [openrouteservice](https://github.com/GIScience/openrouteservice/pulls) and [GraphHopper](https://github.com/GIScience/graphhopper/pulls) against `master` and `ors_4.0` branches, respectively.

**Note that in the above example, the 4.x version of GH is being used - you should adapt according to your specific version. To know which one to use, check the [ors-engine module pom file](https://github.com/GIScience/openrouteservice/ors-engine/pom.xml) and see what version is being used for the `com.github.GIScience.graphhopper` dependencies.**
