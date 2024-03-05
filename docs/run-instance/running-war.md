# Running openrouteservice as WAR

Like older versions, ORS version 8 can still be built and run as a Web Application Resource aka Web Archive (WAR).
A WAR file can be deployed to a Servlet Container like Tomcat, which is running as a service.
To run openrouteservice, you also need an OSM data file, e.g. from [Geofabrik](http://download.geofabrik.de). For more details see chapter [Data](data.md).

## Prerequisites

* [Tomcat](https://tomcat.apache.org/) 10 (or higher) should be installed. E.g. on Ubuntu 22.04, follow these [instructions](https://linuxize.com/post/how-to-install-tomcat-10-on-ubuntu-22-04/). 

To run openrouteservice, you also need an OSM data file, e.g. from [Geofabrik](http://download.geofabrik.de). For more details, see chapter [Data](data.md).

## Download

Starting with version 8 you can download the ready to use WAR from the "Assets" section of the desired release from our GitHub [releases](https://github.com/GIScience/openrouteservice/releases) page.

## Build

How this is done is independent of the artifact type you want to use and is documented in [Building from Source](/run-instance/building-from-source.md).

## Run

Running a WAR file means deploying it to a Tomcat instance. To get openrouteservice up and running, simply copy the `ors.war` file to the Tomcat webapps folder. Tomcat should now automatically detect the new WAR file and deploy the service. 

## Configure

When deploying openrouteservice within Tomcat, there are some important differences to note:
- The recommended way to pass instructions to openrouteservice is to use environment variables. You need to add these to the `setenv.sh` file in your tomcat bin folder, typically somewhere like `/usr/share/tomcat10/bin/`. If the file is not present, then you can create it.
- If you add new settings to the `setenv.sh` file, then you need to restart Tomcat for these to take effect using a command like `sudo systemctl restart tomcat.service`.
- Your configuration file and all input / output files and directories referenced by that configuration need to be accessible (and in case of the output folders, writable) to the user your Tomcat instance is running as. You might need to adjust the location of said files and folders or `chmod` / `chown` them accordingly.

The recommended way to configure an openrouteservice instance is to use a YAML configuration file. You can download an example file by using the following command:

```shell 
wget https://raw.githubusercontent.com/GIScience/openrouteservice/main/ors-config.yml
```

Then you need to add an environment variable to your `setenv.sh` pointing to that configuration file: 

```shell
ORS_CONFIG_LOCATION=/path/to/ors-config.yml
```

For details on how to make openrouteservice apply the settings in the configuration file (there are multiple options) see chapter [Configuration](configuration/index.md).

## Troubleshoot


