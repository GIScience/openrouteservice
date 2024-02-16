# Run Your Own openrouteservice Instance

If you need to customize the behavior of openrouteservice or if the features or quota provided by our public API is not sufficient for your needs, 
you can run your own openrouteservice instance on a server or your local computer.
In an own instance, you can activate all endpoints, also those that are not available in our public API.

There are different options to achieve this. The service can be built in the form of different artifact types: 
**WAR**, **JAR** or as **Docker Image** with one of both. In the first column of the table below you find a link to some basic information about each artifact type.

No matter how you want to run your openrouteservice, you first need the corresponding artifact. 
The second and third columns link to detail information about how to build a customized version yourself or where you can download the artifact.    

For configuration, operation and troubleshooting it plays no role, 
if you run a downloaded or customized version (as long as you don't change basic things like configuration). 
You find links to information about these topics in the three right columns.

But before you start operating your own openrouteservice in the technical way of your choice,
please read the documentation regarding [System Requirements](system-requirements) and [Data](data)!


| Artifact                                  | Build yourself                                  | or Download                                           | Configure                                               | Run                                         | Trouble shoot                                                 | 
|-------------------------------------------|-------------------------------------------------|-------------------------------------------------------|---------------------------------------------------------|---------------------------------------------|---------------------------------------------------------------| 
| [JAR](jar/index.md)                       | [Build JAR](jar/build.md)                       | [Download JAR](jar/download.md)                       | [Configure JAR](jar/configure.md)                       | [Run JAR](jar/run.md)                       | [Troubleshoot JAR](jar/troubleshoot.md)                       | 
| [WAR](war/index.md)                       | [Build WAR](war/build.md)                       | [Download WAR](war/download.md)                       | [Configure WAR](war/configure.md)                       | [Run WAR](war/run.md)                       | [Troubleshoot WAR](war/troubleshoot.md)                       | 
| [Docker Image (JAR)](jar-docker/index.md) | [Build Docker Image (JAR)](jar-docker/build.md) | [Download Docker Image (JAR)](jar-docker/download.md) | [Configure Docker Image (JAR)](jar-docker/configure.md) | [Run Docker Image (JAR)](jar-docker/run.md) | [Troubleshoot Docker Image (JAR)](jar-docker/troubleshoot.md) | 
| [Docker Image (WAR)](war-docker/index.md) | see [older versions](...?)                      | no longer supported since version 8                   | [Configure Docker Image (WAR)](war-docker/configure.md) | [Run docker (WAR)](war-docker/run.md)       | [Troubleshoot docker (WAR)](war-docker/troubleshoot.md)       | 

[//]: # (TODO add row for rpm package once integrated)

::: tip
We recommend to use one of the JAR options, plain or with docker. 
:::
