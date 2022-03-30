---
parent: Installation and Usage
nav_order: 5
title: Advanced Docker Setup
---

# Advanced Docker Setup 

Running openrouteservice out of the box via Docker and docker-compose is a great way to quickly get into how to use openrouteservice. For most applications though, you would want to customise some things such as which profiles you want to build and the data that you want to route with. In it's initial Docker container form, openrouteservice uses an older test dataset of Heidelberg (Germany) as the base data, and only builds the car profile. To do more than that, you need to get your hands a little dirty with some configuration settings. This may sound a little daunting, but it is designed in a way that once you overcome the initial understanding of the configuration and Docker, you will be able to use your own datasets and decide which profiles to use in no time. 

Throughout this setup procedure, we will assume that you are running openrouteservice through Docker Compose after cloning from the official openrouteservice repository. You can of course do it using just Docker or even through Maven and Tomcat, but for now we will look at the Docker Compose method as that is generally the best.

So let's get started!

## Are you running for the first time or the second?

One of the biggest deciding factors as to how the configuration is changed is whether you are building your openrouteservice Docker container for the very first time, or if you are restarting an already built container. 

In general, we recommend that you build openrouteservice first using the default settings and then make modifications after this first build as then the folder structure and config files are produced ready.

### Building for the first time

If you have just downloaded openrouteservice from the GitHub repo (`git clone https://github.com/GIScience/openrouteservice`) and have yet to run `docker-compose up` then the first place to look into is the `docker-compose.yml` file inside the `docker` folder. Here is where you map tell Docker which files to use.

The easiest thing to change is the data that you will be using. In the file, you will see a block of code which has been commented out like the following:
```yaml
#    build:
#      context: ../
#      args:
#        ORS_CONFIG: ./openrouteservice/src/main/resources/ors-config-sample.json
#        OSM_FILE: ./openrouteservice/src/main/files/heidelberg.osm.gz
```
This block is what tells docker-compose that rather than using the pre-built image on docker hub (or one you have already built) we want to build the image here. So to start off, lets uncomment out this code by removing the `#` characters at the start of each line in this block. The two values in `args` are used to tell the build process which config file to build with (we will talk about that soon) and which osm dataset to use. One thing that can get a little confusing here is where the files should be placed in relation to the docker-compose file. You will see the `context: ../` entry which is telling docker-compose that the "root" folder of where the Docker file and all resulting paths are is one folder above where the `docker-compose.yml` file is. That means that any path you define in the two `args` items are relative to that root folder. 
So, lets say you want to use an OSM PBF file for Germany - you could create a folder in the same folder as the Docker file called `data` and put the PBF file in there. Then, inside the docker-compose file, you update the `OSM_FILE` entry to be `OSM_FILE: ./data/germany-latest.osm.pbf`. Now when you run `docker-compose up` this file will be copied into the container and renamed to be `osm_file.pbf` inside the `/ors-core/data` folder, and that is the file that gets used for building the graphs.

The other line of `ORS_CONFIG: ...` tells the system which config file to copy over to the container to use as the base for creating a configuration file that is compatible with the Docker container. As all filepaths used would need to be in relation to the internal file structure of the container, the first time that the container is built, the provided config file is modified so that the paths for the osm file, elevation data and graphs are all modified to point to the `/ors-core/data/...` folders. Also, it is set so that only the first profile in the list of profiles to build is built (see below). The resultant ors-config.json file is then copied to the `conf` folder that is now in the folder that the docker-compose file is located in. It is recommended that this config file is used as the basis for all modifications that you make once you have built.

### Running an already built container

If you have already built the openrouteservice container once, making modifications to the osm data and configuration is somewhat easier.

The first thing you should do before making changes is to stop the container. **Note that using `docker-compose restart` does not carry across changes made inside the `docker-compose.yml` file, so you should use `docker-compose down` to take down the container, and then `docker-compose up` to restart it again when you have made changes.** 

If you are making changes to anything relating to the OSM data or the settings that change how graphs are built, you need to delete the folders in `graphs` or ensure that the `BUILD_GRAPHS` is set to true in the `docker-compose` file. This makes it so that the graphs are built again with the new data/settings.

To change the OSM data that is used, you need to uncomment out the following line within the `volumes` section of the `docker-compose.yml` file:
```yaml
#- ./your_osm.pbf:/ors-core/data/osm_file.pbf
```
and then modify the `./your_osm.pbf` part to point to the osm file that you want to use. This will tell docker to overwrite the current osm file in the container with the one that you are providing. 

One thing to be aware of is the size of the data and how much RAM is given to Java inside the container. We generally [recommend](System-Requirements) at least twice the amount of RAM as the file size, but to tell Java this, you need to update the `JAVA_OPTS` in the `environment` section of the `docker-compose` file. In that line, you will see the `-Xms1g` and `-Xmx2g` items. These tell Java that it should start with 1GB RAM assigned to it, and go no higher than 2 GB of usage. If your pbf file is 1.5 GB in size then you would update the `-Xmx` item to be **AT LEAST** `-Xmx3g`. In general, we would recommend adding a bit more to the RAM value if possible to reduce the chances of hitting an out of memory exception towards the end of the graph building.

To change any configuration settings, all you need to do is modify the `ors-config.json` file found in the `conf` folder and then restart the container as mentioned above. There are a number of configurations that can be changed, with information available on the [configuration wiki page](Configuration) about what each of these are. As a quick example, by default the openrouteservice docker container only builds the car profile. If you want to add another profile, for example the hiking profile, all you need to do is modify the
```json
"active": [
            "car"
          ],
```
list to include that profile:
```json
"active": [
            "car",
            "hiking"
          ],
```
Now when you restart the container, the hiking profile will also be built. As another example, lets say that rather than the default maximum number of locations in a matrix request (100) you want to do some analysis with a lot more, e.g. 10,000 so that you can do a 100 origin x 100 destination matrix calculation. To do that, you would change the `"maximum_routes": 100` within the `matrix` object to be `"maximum_routes": 10000` and then restart the container. 

## Memory mapping in large builds
If you are running a large build (e.g. a planet file) then you may need to increase the number of memory mappings. You only need to do this on the host machine as this value is used by the Docker containers running on it aswell. To do this, go into the system configuration file with `sudo nano /etc/sysctl.conf` and add the following line to the bottom of the file:

```sh
vm.max_map_count=81920
```

The usual sign that you need to do this change is if you see something similar to the following in your logs:

```sh
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 16384 bytes for committing reserved memory.
# An error report file with more information is saved as:
# /ors-core/hs_err_pid128.log
```

## Instance infrastructure
Though having a single container works great for smaller datasets or when the graph data doesn't need updating, in many real world implementations having just the one instance isn't the most suitable solution. If you have one container, then all building and serving of routes happens through that single container, meaning that when you rebuild graphs, you can't make any requests to that instance for things like directions as there are no complete graphs that can be used to generate routes with. If it is important that you have graph updates from new data whilst ensuring that there is a minimal amount of time where users cannot make requests, we would recommend having two instances - one that is permanently active for serving requests, and one that gets fired up to rebuild graphs. 

In that setup, when graphs have been built you can simply stop the container serving requests, replace the graphs used (they are mapped to a folder on the host machine which is defined in the `docker-compose` file), and then restart the container. The new graphs will be reloaded into memory (the amount of time needed for this depends on the size of the graphs and the type of hard drive) and then ready to use for routing. The downtime from reloading already built graphs is normally far less than the time needed to build the graphs. A thing to note though is that you should ensure that the config files and the amount of RAM allocated (as described earlier) is the same on both the builder and the request server else the newly built graphs may not load. **Also, ensure that `BUILD_GRAPHS` parameter in the `docker-compose` file used by the request serving container is set to false else it will try to build the graphs for itself!**
