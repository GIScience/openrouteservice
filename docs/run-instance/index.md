# Run your own instance

If you need to customize the behavior of openrouteservice or if the features or quota provided by our public API is not sufficient for your needs, you can run your own openrouteservice instance on a server or your local computer. There are multiple options to achieve this. 

The service can exist or be built in the form of 4 different artifact types:

* **WAR:** Like older versions, ORS v8 can still be built as a Web Application Resource/Web ARchive (WAR) which can be deployed into a web container like tomcat.
* **JAR:** Since v8, openrouteservice can be built as a fat JAR, that contains all its dependencies. The JAR can be started stand-alone, without an installed tomcat. The trick is, that an embedded servlet container (tomcat) is used. 
* **Docker Image with WAR:** Docker images for ORS versions before v8 were built with a openrouteservice WAR that was running in a tomcat inside a Docker container. If required, you can still go this way, but it is no longer supported by the ORS team.  
* **Docker Image with JAR:** Since v8, the docker images, that are created by the ORS team and published on [docker hub](https://hub.docker.com/r/openrouteservice/openrouteservice) contain an ORS fat JAR with an embedded tomcat.

No matter how you want to run your openrouteservice, you first need the corresponding artifact.
In the table below, we link the documentation for downloading or building the different artifact types, 
how to configure and run the service and where to find logs for trouble shooting: 

| Artifact           | Build yourself                                  | or Download                                           | Configure                                               | Run                                         | Trouble shoot                                                 | 
|--------------------|-------------------------------------------------|-------------------------------------------------------|---------------------------------------------------------|---------------------------------------------|---------------------------------------------------------------| 
| WAR                | [Build WAR](war/build.md)                       | [Download WAR](war/download.md)                       | [Configure WAR](war/configure.md)                       | [Run WAR](war/run.md)                       | [Troubleshoot WAR](war/troubleshoot.md)                       | 
| JAR                | [Build JAR](jar/build.md)                       | [Download JAR](jar/download.md)                       | [Configure JAR](jar/configure.md)                       | [Run JAR](jar/run.md)                       | [Troubleshoot JAR](jar/troubleshoot.md)                       | 
| Docker Image (WAR) | see [older versions](...?)                      | no longer supported since v8                          | [Configure Docker Image (WAR)](war-docker/configure.md) | [Run docker (WAR)](war-docker/run.md)       | [Troubleshoot docker (WAR)](war-docker/troubleshoot.md)       | 
| Docker Image (JAR) | [Build Docker Image (JAR)](jar-docker/build.md) | [Download Docker Image (JAR)](jar-docker/download.md) | [Configure Docker Image (JAR)](jar-docker/configure.md) | [Run Docker Image (JAR)](jar-docker/run.md) | [Troubleshoot Docker Image (JAR)](jar-docker/troubleshoot.md) | 


::: tip
We recommend to use one of the JAR options, plain or with docker. 
:::


## Quick start

The fastest and easiest way to have an instance of openrouteservice running is to use our docker compose file. If you have docker installed, running the following commands should get everything done.

```shell
wget https://raw.githubusercontent.com/GIScience/openrouteservice/main/docker-compose.yml
docker compose up
```

This will pull the latest nightly build of openrouteservice from Docker Hub and start it up using an example setup and the provided test OSM file for Heidelberg/Germany and surrounding area.
You can then modify the configuration and source file settings to match your needs. For more details, check the [Running with Docker](installation/running-with-docker) section.


## Developer options

Of course, developers can also build a WAR or JAR or docker Image as described in the linked documentations,
but there is also the option to run openrouteservice directly 

* [from an IDE](runDirectlyFromIDE.md) with specific run configurations
* [on the command line with maven](runDirectlyWithMaven.md)

## RPM Package

A new way to install openrouteservice ist RPM. 

[//]: # (TODO describe)


## Prerequisites

Before you start with one of the mentioned options, 
please read the documentation regarding [System Requirements](system-requirements) and [Data](data)!  
