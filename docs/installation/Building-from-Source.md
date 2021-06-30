---
parent: Installation and Usage
nav_order: 2
title: Building from Source
---

[:arrow_backward:  Installation and Usage](Installation-and-Usage)
# Building from Source

**We recommend running openrouteservice using a Docker container (see [Running with Docker](Running-with-Docker))**

If you need to install without Docker, on an Ubuntu 16.04 system (also generally works with newer Ubuntu versions) you can use the following steps:

  1. Clone the openrouteservice repository to your machine.
  2. Make sure that you have java 1.8 set as the default Java environment.
  3. Make sure that you have Maven installed.
  4. Download/create an OpenStreetMap pbf file on the machine.
  5. Copy the `openrouteservice/src/main/resources/app.config.sample` file to the same location but renaming it to `app.config`.
  6. Update the `app.config` file to reflect the various settings, profiles you want to have running, and the locations of various files, in particular the source location of the OSM file that will be used and additional files required for extended storages. You should make sure that these folders/files are accessible by the service, for example by using the `sudo chmod -R 777 [path to folder]` command.
  7. From within the `openrouteservice` folder (containing the pom file and the src folder, amongst others) run the command `mvn package`. This will build the openrouteservice ready for tomcat deployment.

After you have packaged openrouteservice, there are two options for running it. One is to run the `mvn tomcat7:run` command which triggers a self contained Tomcat instance, but this is more restrictive in terms of settings for Tomcat. The other is to install and run Tomcat 8 as described next:

  1. Install Tomcat 8 using `sudo apt-get install tomcat8`.

     Note that it might not be available in the lastest repositories of your distribution anymore.
     In that case, add the following line(s) to your `/etc/apt/sources.list`:
     ```
     debian:  deb http://ftp.de.debian.org/debian/ stretch main
              deb http://security.debian.org/ stretch/updates main

     ubuntu:  deb http://de.archive.ubuntu.com/ubuntu bionic main universe
              deb http://de.archive.ubuntu.com/ubuntu bionic-security main universe
     ```
     For more details, visit [the debian wiki](https://wiki.debian.org/SourcesList) on the `sources.list`-format.

  2. If you want to use system settings (i.e. Java heap size) other than the default, then you need to add these to the `/usr/share/tomcat8/bin/setenv.sh` file. If the file is not present, then you can create it. The settings generally used on our servers are similar to:
    
```bash
JAVA_OPTS="-server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4 -Xms105g -Xmx105g -XX:MaxMetaspaceSize=50m"
CATALINA_OPTS="(here we set settings for JMX monitoring)"
```

  3. If you add these new settings to the `setenv.sh` file, then you need to restart Tomcat for these to take effect using `sudo systemctl restart tomcat8.service`.
  4. To get openrouteservice up and running, copy the `ors.war` file found in the `openrouteservice/target` folder to the Tomcat webapps folder. For example

```bash
sudo cp ~/openrouteservice/openroutesrvice/target/ors.war /var/lib/tomcat8/webapps/
```

  5. Tomcat should now automatically detect the new WAR file and deploy the service. Depending on profiles and size of the OSM data, this can take some time until openrouteservice has built graphs and is ready for generating routes. You can check if it is ready by accessing `http://localhost:8080/ors/health` (the port and URL may be different if you have installed Tomcat differently than above). If you get a `status: ready` message, you are good to go in creating routes.

There are numerous settings within the `app.config` which are highly dependent on your individual circumstances, but many of these are documented. As a guide however you can look at the `app.config.sample` file in the `docker/conf` folder. If you run into issues relating to out of memory or similar, then you will need to adjust java/tomcat settings accordingly.
