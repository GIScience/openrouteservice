# Running JAR / WAR

## Provided artifacts

For release version 8.0.0 and newer, we provide JAR and WAR files as artifacts for download. The JAR file can be run stand-alone and will start openrouteservice using a built-in tomcat server. Alternatively, you can set up your own tomcat instance on your server and deploy the WAR artifact on it.  

## Parameters and environment variables

Openrouteservice is configured by pointing an environment variable called `ORS_CONFIG_LOCATION` to a YAML configuration file. Use the following commands to download our example configuration file (which is used by our docker setup by default) to have a starting point, and export the path to that file to the required environment variable.

```shell
wget https://raw.githubusercontent.com/GIScience/openrouteservice/master/ors-api/ors-config.yml
export ORS_CONFIG_LOCATION=${pwd}/ors-config.yml
```

For more details on the configuration options with that YAML file, see the chapter on [configuration](/run-instance/configuration/).

If you already have an old and deprecated JSON format configuration file, you can still use that configuration file by setting an environment variable calles `ORS_CONFIG` to point that file. Currently, all settings in a provided JSON configuration file will *override* any settings in the YAML file. We strongly recommend to migrate your settings to the new YAML format though, since we are planning to remove the support for JSON cofiguration files with the next major version release.  

## Folders and files

Openrouteservice produces output files of three types, for which the paths can be configured. The directories these paths point to need to be *writable*. The default configuration file for openrouteservice mentioned above provides the following paths:
- `./ors-core/data/graphs`: The converted graphs for the profiles are stored here.
- `./ors-core/data/elevation_cache`: If elevation is activated in the configuration, openrouteservice will download and cache the elevation data tiles here. 
- `./logs`: Log output is written to auto-rotated log files. 
 
Openrouteservice also requires an OpenStreetMap export file to import the graph data from, configured in the YAML configuration file. The default location is `./ors-core/data/osm_file.pbf`. 

See chapter [logging](/run-instance/configuration/logging) for details on configuring the location of log files, and chapter [engine](/run-instance/configuration/engine) for the all other file locations.

[//]: # (TODO: overhaul contents below after integrating the jar build PR)

# Installing and running tomcat9

1. Install Tomcat 9 using `sudo apt-get install tomcat9`.

2. If you want to use system settings (i.e. Java heap size) other than the
   default, then you need to add these to the
   `/usr/share/tomcat8/bin/setenv.sh` file. If the file is not present, then you
   can create it. The settings generally used on our servers are similar to:

   ```bash
   JAVA_OPTS="-server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4 -Xms105g -Xmx105g -XX:MaxMetaspaceSize=50m"
   CATALINA_OPTS="(here we set settings for JMX monitoring)"
   ```
3. If you add these new settings to the `setenv.sh` file, then you need to
   restart Tomcat for these to take effect using `sudo systemctl restart
   tomcat8.service`.
4. To get openrouteservice up and running, copy the `ors.war` file found in
   the `ors-api/target` folder to the Tomcat webapps folder. For
   example

   ```bash
   sudo cp ~/openrouteservice/ors-api/target/ors.war /var/lib/tomcat8/webapps/
   ```

5. Tomcat should now automatically detect the new WAR file and deploy the
   service. Depending on profiles and size of the OSM data, this can take
   some time until openrouteservice has built graphs and is ready for generating
   routes. You can check if it is ready by accessing
   `http://localhost:8080/ors/health` (the port and URL may be different if you
   have installed Tomcat differently than above). If you get a `status: ready`
   message, you are good to go in creating routes.

There are numerous settings within the `ors-config.json` which are highly dependent
on your individual circumstances, but many of these [are documented](Configuration). As a guide
however you can look at the `ors-config-sample.json` file in the
`ors-api/src/main/resources` folder. If you run into issues relating
to out of memory or similar, then you will need to adjust java/tomcat settings
accordingly.