# Running with Docker

## Install and run openrouteservice with Docker

Installing the openrouteservice backend service with Docker is quite straightforward. All you need is an OSM extract, e.g. from [Geofabrik](http://download.geofabrik.de), and a working [Docker installation](https://www.digitalocean.com/community/tutorial_collections/how-to-install-and-use-docker).

You can use [Dockerhub's hosted Openrouteservice image](https://hub.docker.com/repository/docker/openrouteservice/openrouteservice) or build your own image.

> For a step by step guide also check out [this YouTube video](https://www.youtube.com/watch?v=VQXlbqKArFk) (Thanks a lot SyntaxByte <3), though it is a bit outdated by now...

## Docker scenarios

There are multiple ways with Docker to quickly have a running instance.

1. Recommended: Run latest release version image using `docker compose`

    ```shell
    # For latest build
    wget https://raw.githubusercontent.com/GIScience/openrouteservice/master/docker-compose.yml
    docker compose up -d
    ```

2. Run specific version image using `docker compose`

    You can specify the Docker image tag by declaring an environment variable named `ORS_TAG`. Possible values are specific version tags such as `v7.2.0` or `nightly` for the newest nightly build. 

    ```shell
    # For nightly builds
    wget https://raw.githubusercontent.com/GIScience/openrouteservice/master/docker-compose.yml
    export ORS_TAG=nightly && docker compose up -d
    ```
 
3. Run a Docker image built from local code

   It is also possible to compile local code and build a Docker image to run. This can be useful in cases where you want to use a version of openrouteservice with modified code. 

    ```shell
    # Download entire source code
    git clone https://github.com/GIScience/openrouteservice.git
    cd openrouteservice
    ```

    After downloading the code, open the file `docker-compose.yml`, remove line 8 and uncomment lines 11-14.

    ::: details Click here to see the code

    ```yaml
    version: '2.4'
    services:
      ors-app:
        container_name: ors-app
        ports:
          - "8080:8080"
          - "9001:9001"
        image: openrouteservice/openrouteservice:nightly // [!code --]
        # For versioned images see https://giscience.github.io/openrouteservice/run-instance/installation/running-with-docker
        user: "${UID:-0}:${GID:-0}"
    #    build: // [!code --]
    #      context: ./ // [!code --]
    #      args: // [!code --]
    #        OSM_FILE: ./ors-api/src/test/files/heidelberg.osm.gz // [!code --]
        build: // [!code ++]
          context: ./ // [!code ++]
          args: // [!code ++]
            OSM_FILE: ./ors-api/src/test/files/heidelberg.osm.gz // [!code ++]
        volumes:
          - ./docker/graphs:/home/ors/ors-core/data/graphs          
    [...]
    ```
    :::

    This tells Docker to build the local code using maven and build a Docker image with that code instead of pulling the ready-built image from Dockerhub. You can then build and start the image with the following command.

     ```shell
     # After modifying the docker-compose.yml file 
     docker compose up -d
     ```

4. `docker run` for ors versions >= 6.8.2
    
    You can specify the entire Docker command (that `docker compose` would run for you) if you need to change specific details. It is easier to modify the Docker compose file, but to test a specific setup you can run the following command. To use a diffrent version of openrouteservice, change the tag (after the colon) in the last line.

    ```shell
    # create directories for volumes to mount as local user
    mkdir -p docker/conf docker/elevation_cache docker/graphs docker/logs/ors docker/logs/tomcat
    docker run -dt -u "${UID}:${GID}" \
      --name ors-app \
      -p 8080:8080 \
      -v $PWD/docker/graphs:/home/ors/ors-core/data/graphs \
      -v $PWD/docker/elevation_cache:/home/ors/ors-core/data/elevation_cache \
      -v $PWD/docker/logs/ors:/home/ors/ors-core/logs/ors \
      -v $PWD/docker/logs/tomcat:/home/ors/tomcat/logs \
      -v $PWD/docker/conf:/home/ors/ors-conf \
      -v $PWD/docker/data:/home/ors/ors-core/data \
      #-e "BUILD_GRAPHS=True" \
      -e "JAVA_OPTS=-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g" \
      -e "CATALINA_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost" \
      openrouteservice/openrouteservice:latest 
    ```

5. `docker run` for ors versions <= 6.8.1

    For older versions of openrouteservice, the Dockerfile has a different internal directory structure, and the `docker compose` file will not yield the same result. Use the following `docker` command instead. 

    ```bash
    # create directories for volumes to mount as local user
    mkdir -p docker/conf docker/elevation_cache docker/graphs docker/logs/ors docker/logs/tomcat
    docker run -dt -u "${UID}:${GID}" \
      --name ors-app \
      -p 8080:8080 \
      -v $PWD/docker/graphs:/ors-core/data/graphs \
      -v $PWD/docker/elevation_cache:/ors-core/data/elevation_cache \
      -v $PWD/docker/logs/ors:/home/ors/ors-core/logs/ors \
      -v $PWD/docker/logs/tomcat:/home/ors/tomcat/logs \
      -v $PWD/docker/conf:/ors-conf \
      -v $PWD/docker/data:/ors-core/data \
      #-e "BUILD_GRAPHS=True" \
      -e "JAVA_OPTS=-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g" \
      -e "CATALINA_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost" \
      openrouteservice/openrouteservice:v6.8.1
    ```

## Checking

By default the service status can be queries via the health endpoint.

```shell 
curl 'http://localhost:8080/ors/v2/health'
# shoud result in an output like 
# {"status":"ready"}
```

When the service is ready, you will be able to request the status endpoint for further information on the running services.

```shell 
curl 'http://localhost:8080/ors/v2/status'
# shoud result in an output like 
# {"languages":["cs","cs-cz","de","de-de","en","en-us","eo","eo-eo","es","es-es","fr","fr-fr","gr","gr-gr","he","he-il","hu","hu-hu","id","id-id","it","it-it","ja","ja-jp","ne","ne-np","nl","nl-nl","pl","pl-pl","pt","pt-pt","ro","ro-ro","ru","ru-ru","tr","tr-tr","zh","zh-cn"],"engine":{"build_date":"2024-01-02T16:34:45Z","version":"8.0"},"profiles":{"profile 1":{"storages":{"WayCategory":{"gh_profile":"car_ors_fastest_with_turn_costs"},"HeavyVehicle":{"gh_profile":"car_ors_fastest_with_turn_costs"},"WaySurfaceType":{"gh_profile":"car_ors_fastest_with_turn_costs"},"RoadAccessRestrictions":{"gh_profile":"car_ors_fastest_with_turn_costs","use_for_warnings":"true"}},"profiles":"driving-car","creation_date":"","limits":{"maximum_distance":100000,"maximum_waypoints":50,"maximum_distance_dynamic_weights":100000,"maximum_distance_avoid_areas":100000}}},"services":["routing","isochrones","matrix","snap"]}
```

If you use the default dataset you will be able to request something like the following route request to the car profile in Heidelberg.

```shell 
curl 'http://localhost:8080/ors/v2/directions/driving-car?start=8.681495,49.41461&end=8.687872,49.420318'
```

## Docker configuration

Running openrouteservice out of the box via Docker and docker-compose is a great way to quickly get into how to use openrouteservice. For most applications though, you would want to customise some things such as which profiles you want to build and the data that you want to route with. In it's initial Docker container form, openrouteservice uses an older test dataset of Heidelberg (Germany) as the base data, and only builds the car profile. To do more than that, you need to get your hands a little dirty with some configuration settings. This may sound a little daunting, but it is designed in a way that once you overcome the initial understanding of the configuration and Docker, you will be able to use your own datasets and decide which profiles to use in no time.

### What you get

All the above scenarios will:

1. Pull the openrouteservice Docker image from Dockerhub and start a container named `ors-app`
2. Launch the openrouteservice service on port `8080` within a tomcat running in that container, available at the address `http://localhost:8080/ors`.
3. A local `./docker` folder containing the files used and produced by openrouteservice for easy access. Most relevant is `./docker/conf/ors-config.yml` controlling ORS behaviour, and the test OSM data file  `/home/ors/ors-core/data/osm_file.pbf` of Heidelberg and surroundings.

### Customization

Once you have a built image you can decide to start a container with different settings, e.g. changing the active profiles or other settings. To run ORS with a custom configuration, modify the `./docker/conf/ors-config.yml` to your needs, and restart the container. You can obviously also modify the volume mappings in the `docker-compose.yml` to your needs. **Note that using `docker-compose restart` does not carry across changes made inside the `docker-compose.yml` file, so you should use `docker-compose down` to take down the container, and then `docker-compose up` to restart it again when you have made changes.**

For detailed information on the settings you can make, see the chapter on [configuration](../configuration/).

Old ORS configuration files in JSON format are **deprecated**. If you have a custom `ors-config.json` file from a previous installation we strongly recommend to migrate to the new YAML format. For the transitional period ORS allows the use of old format JSON files placed at `./docker/conf/ors-config.json`. All settings in such a file, if present, will override settings in the proper YAML format.

If you are making changes to anything relating to the OSM data or the settings that change how graphs are built, you need to delete the folders in `graphs` or set the environment variable `BUILD_GRAPHS=True` (see comment in Dockerfile or examples above). This makes it so that the graphs are built again with the new data/settings.

### Different OSM file
To change the OSM data that is used, you can either overwrite the `docker/data/osm_file.pbf` file, or modify the volume mount of `/home/ors/ors-core/data` to a directory containing a file `osm_file.pbf`, or volume mount something like  `/YOUR/PATH/TO/ANOTHER/OSM_FILE.pbf:/home/ors/ors-core/data/osm_file.pbf`. 

Make sure to set the environment variable `BUILD_GRAPHS=True` or empty the `docker/graphs/` directory before restarting the container.

If you are building the Docker image locally, you can also point the build argument `OSM_FILE` to your desired OSM file before building the image.

Note, `.osm`, `.osm.gz`, `.osm.zip` and `.pbf` file format are supported as OSM files.

If your dataset is very large, it may be necessary to adjust the `-Xmx` parameter of `JAVA_OPTS` environment variable. A good rule of thumb is to give Java 2 x file size of the PBF **per profile**.

::: details Details on memory settings
In the `JAVA_OPTS` line of the `docker-compose.yml` file, you will see the `-Xms1g` and `-Xmx2g` items. These tell Java that it should start with 1GB RAM assigned to it, and go no higher than 2 GB of usage. If let's say your pbf file is 1.5 GB in size, and you have two profiles configured (e.g. car and foot-walking), then you would update the `-Xmx` item to be **AT LEAST** `-Xmx6g` (1.5GB * 2 Profiles * 2). In general, we would recommend adding a bit more to the RAM value if possible to reduce the chances of hitting an out of memory exception towards the end of the graph building.
:::

### UID

If you need to change the UID the ors is running with, you can use these variables:
```bash
# set it explicitly to 1001
ORS_UID=1001 ORS_GID=1001 docker compose up -d

# or set it to the current user
ORS_UID=${UID} ORS_GID=${GID} docker compose up -d
```

### Volumes

There are some important directories one might want to preserve on the host machine, to survive container regeneration. These directories should be mapped as volumes. 

- `/home/ors/ors-core/data/graphs`: Contains the built graphs after ORS intialized.
- `/home/ors/ors-core/data/elevation_cache`: Contains the CGIAR elevation tiles if elevation was specified.
- `/home/ors/ors-core/logs/ors`: Contains the ORS logs.
- `/home/ors/tomcat/logs`: Contains the Tomcat logs.
- `/home/ors/ors-conf`: Contains the `ors-config.json` which is used to control ORS.
- `/home/ors/ors-core/data`: Contains the OSM file `osm_file.pbf` being used to generate graphs.

Look at the [`docker-compose.yml`](https://github.com/GIScience/openrouteservice/blob/master/docker-compose.yml) for examples.

### Environment variables

- `BUILD_GRAPHS`: Forces ORS to rebuild the routings graph(s) when set to `True`. Useful when another PBF is specified in the Docker volume mapping to `/home/ors/ors-core/data/osm_file.pbf`
- `JAVA_OPTS`: Custom Java runtime options, such as `-Xms` or `-Xmx`
- `CATALINA_OPTS`: Custom Catalina options

Specify either during container startup or in `docker-compose.yml`.

### Build arguments

When building the image, the following arguments are customizable:

- `ORS_CONFIG`: Can be changed to specify the location of a custom `ors-config.json` file. Default `./ors-api/src/main/resources/ors-config.json`.
- `OSM_FILE`: Can be changed to point to a local custom OSM file. Default `./ors-api/src/test/files/heidelberg.osm.gz`.

## Memory mapping in large builds
If you are running a large build (e.g. a planet file) then you may need to increase the number of memory mappings. You only need to do this on the host machine as this value is used by the Docker containers running on it as well. To do this, go into the system configuration file with `sudo nano /etc/sysctl.conf` and add the following line to the bottom of the file:

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
