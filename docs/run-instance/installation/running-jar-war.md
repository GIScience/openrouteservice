# Running JAR / WAR

[//]: # (TODO: overhaul contents after integrating the jar build PR)

## Provided artifacts

For release version 8.0.0 and newer, we provide JAR and WAR files as artifacts for download. The JAR file can be run stand-alone and will start openrouteservice using a built-in Tomcat server. Alternatively, you can set up your own Tomcat instance on your server and deploy the WAR artifact on it.  

## Parameters and environment variables

Openrouteservice can be configured in several ways, described in detail in the chapter on [configuration](/run-instance/configuration/index.md).

If you already have an old and deprecated JSON format configuration file, you can still use that configuration file by setting an environment variable called `ORS_CONFIG` to point that file. Currently, all settings in a provided JSON configuration file will *override* any settings in the YAML file. We strongly recommend to migrate your settings to the new YAML format though, since we are planning to remove the support for JSON configuration files with the next major version release.  

## Folders and files

Openrouteservice produces output files of three types, for which the paths can be configured. The directories these paths point to need to be *writable*. The default configuration file for openrouteservice mentioned above provides the following paths:
- `./ors-core/data/graphs`: The converted graphs for the profiles are stored here.
- `./ors-core/data/elevation_cache`: If elevation is activated in the configuration, openrouteservice will download and cache the elevation data tiles here. 
- `./logs`: Log output is written to auto-rotated log files. 
 
Openrouteservice also requires an OpenStreetMap export file to import the graph data from, configured in the YAML configuration file. The default location is `./ors-core/data/osm_file.pbf`. 

See chapter [logging](/run-instance/configuration/spring/logging.md) for details on configuring the location of log files, and chapter [engine](/run-instance/configuration/ors/engine/index.md) for the all other file locations.

# Installing and running openrouteservice within Tomcat 10

1. Install Tomcat 10 on your system. E.g. on Ubuntu 22.04, follow these [instructions](https://linuxize.com/post/how-to-install-tomcat-10-on-ubuntu-22-04/).

2. If you want to use system settings (i.e. Java heap size) other than the default, then you need to add these to the `setenv.sh` file in your tomcat bin folder, typically somewhere like `/usr/share/tomcat10/bin/`. If the file is not present, then you can create it. 

   The environment variable `ORS_CONFIG_LOCATION` and other optional environment variables need to be written to that file, too. The settings generally used on our servers are similar to:

  ```shell
  JAVA_OPTS="-server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4 -Xms105g -Xmx105g -XX:MaxMetaspaceSize=50m"
  CATALINA_OPTS="(here we set settings for JMX monitoring)"
  ORS_CONFIG_LOCATION=/path/to/ors-config.yml
  ```

3. If you add these new settings to the `setenv.sh` file, then you need to restart Tomcat for these to take effect using a command like `sudo systemctl restart tomcat.service`.

4. To get openrouteservice up and running, copy the `ors.war` file you downloaded (or built, in which case the file can be found at `ors-api/target`) to the Tomcat webapps folder.

5. Tomcat should now automatically detect the new WAR file and deploy the service. Depending on profiles and size of the OSM data, this can take some time until openrouteservice has built graphs and is ready for generating routes. You can check if it is ready by accessing `http://localhost:8080/ors/health` (the port and URL may be different if you have installed Tomcat differently than above). If you get a `status: ready` message, you are good to go in creating routes.

6. Your configuration file and all input / output files and directories referenced by that configuration need to be accessible (and in case of the output folders, writable) to the user your Tomcat instance is running as. You might need to adjust the location of said files and folders or `chmod` / `chown` them accordingly.