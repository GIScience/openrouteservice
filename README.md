# Openrouteservice

[![Build Status](https://travis-ci.org/GIScience/openrouteservice.svg?branch=master)](https://travis-ci.org/GIScience/openrouteservice) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.heigit.ors%3Aopenrouteservice&metric=alert_status&branch=master)](https://sonarcloud.io/dashboard?id=org.heigit.ors%3Aopenrouteservice)
[![SourceSpy Dashboard](https://sourcespy.com/shield.svg)](https://sourcespy.com/github/giscienceopenrouteservice/)

The **openrouteservice API** provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from [OpenStreetMap](http://www.openstreetmap.org). It is highly customizable, performant and written in Java.

The following services are available via a HTTP interface served by Tomcat.
- **Directions** - Returns a route between two or more locations for a selected profile with customizable additional settings and instructions.
- **Isochrones** - Obtains areas of reachability from given locations.
- **Matrix** - Computes one-to-many, many-to-one or many-to-many routes for any mode of transport provided by openrouteservice.

To play around with openrouteservice you may use our [demonstration server](https://maps.openrouteservice.org) which comes with both the backend and a [frontend](https://github.com/GIScience/openrouteservice-app). Or simply sign up for an [API key](https://openrouteservice.org) and fire your requests against the API directly.

Please note that openrouteservice uses a forked and edited version of [graphhopper 0.13](https://github.com/GIScience/graphhopper) which can be found [here](https://github.com/GIScience/graphhopper).

[![ors client accessibility](https://user-images.githubusercontent.com/23240110/30385487-9eac96b8-98a7-11e7-9357-afd4df8fccdf.png)](https://openrouteservice.org/reach)

**Note**

- Our geocoding API is a separate service running the stack built around [**Pelias**](https://github.com/pelias/pelias).
- Our locations/API is another service which we have coined **openpoiservice** which can be found [here](https://github.com/GIScience/openpoiservice).


## Changelog/latest changes

[Openrouteservice CHANGELOG](https://github.com/GIScience/openrouteservice/blob/master/CHANGELOG.md)

## Contribute

We appreciate any kind of contribution - bug reports, new feature suggestion or improving our translations are greatly appreciated. Feel free to create an [issue](https://github.com/GIScience/openrouteservice/issues) and label it accordingly. If your issue regards the openrouteservice web-app please use the [corresponding repository](https://github.com/GIScience/openrouteservice-app/issues).

If you want to contribute your improvements, please follow the steps outlined in [our CONTRIBUTION guidelines](./CONTRIBUTE.md)

The [sourcespy dashboard](https://sourcespy.com/github/giscienceopenrouteservice/) provides a high level overview of the repository including technology summary, module dependencies and other components of the system.

## Installation

We recommend using Docker to install and launch the openrouteservice backend. Either pull your desired image from [Dockerhub](https://hub.docker.com/repository/docker/openrouteservice/openrouteservice) or build your own from this repository. Note, you'll have to uncomment the `build` section when building the image from source.

```bash
cd docker && docker-compose up
```

For more details, check the [docker installation guide](docker/README.md).

If you need to install without Docker, on an Ubuntu 16.04 system (also generally works with newer Ubuntu versions) you can use the following steps:

  1. Clone the openrouteservice repository to your machine.
  2. Make sure that you have java 1.8 set as the default Java environment.
  3. Make sure that you have Maven installed.
  4. Download/create an OpenStreetMap pbf file on the machine.
  5. Copy the `openrouteservice/WebContent/WEB-INF/app.config.sample` file to the same location but renaming it to `app.config`.
  6. Update the `app.config` file to reflect the various settings, profiles you want to have running, and the locations of various files, in particular the source location of the OSM file that will be used and additional files required for extended storages. You should make sure that these folders/files are accessible by the service, for example by using the `sudo chmod -R 777 [path to folder]` command.
  7. From within the `openrouteservice` folder (containing the pom file and the src folder, amongst others) run the command `mvn package`. This will build the openrouteservice ready for tomcat deployment.

After you have packaged openrouteservice, there are two options for running it. One is to run the `mvn tomcat7:run` command which triggers a self contained Tomcat instance, but this is more restrictive in terms of settings for Tomcat. The other is to install and run Tomcat 8 as described next:

  1. Install Tomcat 8 using `sudo apt-get install tomcat8`.
  2. If you want to use system settings (i.e. Java heap size) other than the default, then you need to add these to the `/usr/share/tomcat8/bin/setenv.sh` file. If the file is not present, then you can create it. The settings generally used on our servers are similar to:

```bash
JAVA_OPTS="-server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4 -Xms114g -Xmx114g -XX:MaxMetaspaceSize=50m"
CATALINA_OPTS="(here we set settings for JMX monitoring)"
```

  3. If you add these new settings to the `setenv.sh` file, then you need to restart Tomcat for these to take effect using `sudo systemctl restart tomcat8.service`.
  4. To get openrouteservice up and running, copy the `openrouteservice-xxx.war` file found in the `openrouteservice/target` folder to the Tomcat webapps folder. For example

```bash
sudo cp ~/openrouteservice/openroutesrvice/target/openroutservice-4.7.0.war /var/lib/tomcat8/webapps/ors.war
```

  5. Tomcat should now automatically detect the new WAR file and deploy the service. Depending on profiles and size of the OSM data, this can take some time until openrouteservice has built graphs and is ready for generating routes. You can check if it is ready by accessing `http://localhost:8080/ors/health` (the port and URL may be different if you have installed Tomcat differently than above). If you get a `status: ready` message, you are good to go in creating routes.

There are numerous settings within the `app.config` which are highly dependent on your individual circumstances, but many of these are documented. As a guide however you can look at the `app.config.sample` file in the `docker/conf` folder. If you run into issues relating to out of memory or similar, then you will need to adjust java/tomcat settings accordingly.

## Usage

Openrouteservice offers a set of endpoints for different spatial purposes. They are served with the help of [Tomcat in a java servlet container](https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/WebContent/WEB-INF/web.xml). By default you will be able to query the services with these addresses:

- `http://localhost:8080/name_of_war_archive/routes`
- `http://localhost:8080/name_of_war_archive/isochrones`
- `http://localhost:8080/name_of_war_archive/matrix`

## API Documentation

For an easy and interactive way to test the api, visit our documentation at [openrouteservice.org](https://openrouteservice.org/dev/#/api-docs).
After obtaining your key you can try out the different endpoints instantly and start firing requests.


## Questions

For questions please use our [community forum](https://ask.openrouteservice.org).

## Translations

If you notice any thing wrong with translations, or you want to add a new language to the ORS instructions, we have some instructions on our [wiki page](https://github.com/GIScience/openrouteservice/wiki/Contributing-Translations) about how you can submit an update. You can also look over at our [maps client GitHub](https://github.com/GIScience/openrouteservice-app/#translations) if you want to contribute the language to there aswell (adding or editing the language in the openrouteservice GitHub repo only affects the instructions - any new language also needs adding to the client).
