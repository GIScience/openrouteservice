# Running openrouteservice as JAR

Since version 8, openrouteservice can be built as a fat JAR file that contains all its dependencies and can be run as stand-alone application.

## Prerequisites

* [java](https://www.java.com/en/) 17 (or higher) should be available, preferably as default Java environment.

To run openrouteservice, you also need an OSM data file, e.g. from [Geofabrik](http://download.geofabrik.de). For more details, see chapter [Data](data.md).

## Download

Starting with version 8 you can download the ready to use JAR file from the "Assets" section of the desired release from our GitHub [releases](https://github.com/GIScience/openrouteservice/releases) page.

## Build

How this is done is independent of the artifact type you want to use and is documented in [Building from Source](/run-instance/building-from-source.md).

## Run

To run the openrouteservice application, use the following command:

```shell 
java -jar ors.jar
```

## Configure

The recommended way to configure an openrouteservice instance run plain using the JAR file is to use a YAML configuration file. You can download an example file by using the following command: 

```shell 
wget https://raw.githubusercontent.com/GIScience/openrouteservice/main/ors-config.yml
```

For details on how to make openrouteservice apply the settings in the configuration file (there are multiple options) see chapter [Configuration](configuration/index.md).

## Troubleshoot


