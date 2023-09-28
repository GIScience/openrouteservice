---
parent: Installation and Usage
nav_order: 1
title: Running with Docker
---

# Running with Docker

## Install and run openrouteservice with docker

> For a step by step guide check out [this YouTube video](https://www.youtube.com/watch?v=VQXlbqKArFk) (Thanks a lot SyntaxByte <3).

Installing the openrouteservice backend service with Docker is quite straightforward. All you need is an OSM extract, e.g. from [Geofabrik](http://download.geofabrik.de), and a working [docker installation](https://www.digitalocean.com/community/tutorial_collections/how-to-install-and-use-docker).

Use [Dockerhub's hosted Openrouteservice image](https://hub.docker.com/r/openrouteservice/openrouteservice) or build your own image
### Docker scenarios

There are multiple ways with docker to quickly have a running instance.

1. Run with `docker compose`

```bash
wget https://raw.githubusercontent.com/GIScience/openrouteservice/master/docker-compose.yml
docker compose up -d
```

2. `docker run` for ors versions >= 6.8.2

```bash
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
  #-v $PWD/your_osm.pbf:/home/ors/ors-core/data/osm_file.pbf \
  #-e "BUILD_GRAPHS=True" \
  -e "JAVA_OPTS=-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g" \
  -e "CATALINA_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost" \
  openrouteservice/openrouteservice:latest
```

3. `docker run` for ors versions <= 6.8.1

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
  #-v $PWD/your_osm.pbf:/ors-core/data/osm_file.pbf \
  #-e "BUILD_GRAPHS=True" \
  -e "JAVA_OPTS=-Djava.awt.headless=true -server -XX:TargetSurvivorRatio=75 -XX:SurvivorRatio=64 -XX:MaxTenuringThreshold=3 -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ParallelGCThreads=4 -Xms1g -Xmx2g" \
  -e "CATALINA_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9001 -Dcom.sun.management.jmxremote.rmi.port=9001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost" \
  openrouteservice/openrouteservice:v6.8.1
```

### Docker configuration

**What you get**

All the above scenarios will:

1. Pull the openrouteservice docker image from dockerhub and start a container named `ors-app`
2. Launch the openrouteservice service on port `8080` within a tomcat running in that container, available at the address `http://localhost:8080/ors`.
3. A local `./docker` folder containing the files used and produced by openrouteservice for easy access. Most relevant is `./docker/conf/ors-config.json` controlling ORS behaviour. If you map a local `your_osm.pbf` to the container's `/home/ors/ors-core/data/osm_file.pbf` (see comment in dockerfile or examples above), it will build a graph from that OSM file. Otherwise openrouteservice will use the provided OSM test file of Heidelberg and surroundings.

After container launch, modify the `./docker/conf/ors-config.json` to your needs, and restart the container. Alternatively you can map an existing `ors-config.json` which you have locally to the container's `/ors-conf/ors-config.json` to initialize ORS immediately. If you changed the OSM file after the first container start, don't forget to set the environment variable `BUILD_GRAPHS=True` (see comment in dockerfile or examples above) to force a rebuild of the graph(s) (or delete the `./docker/graphs` folder, which is the same thing).

**UID**

If you need to change the UID the ors is running with, you can use these variables:
```bash
# set it explicitly to 1001
ORS_UID=1001 ORS_GID=1001 docker compose up -d

# or set it to the current user
ORS_UID=${UID} ORS_GID=${GID} docker compose up -d
```


**Volumes**

There are some important directories one might want to preserve on the host machine, to survive container regeneration. These directories should be mapped as volumes. 

- `/home/ors/ors-core/data/graphs`: Contains the built graphs after ORS intialized.
- `/home/ors/ors-core/data/elevation_cache`: Contains the CGIAR elevation tiles if elevation was specified.
- `/home/ors/ors-core/logs/ors`: Contains the ORS logs.
- `/home/ors/tomcat/logs`: Contains the Tomcat logs.
- `/home/ors/ors-conf`: Contains the `ors-config.json` which is used to control ORS.
- `/home/ors/ors-core/data/osm_file.pbf`: The OSM file being used to generate graphs.

Look at the [`docker-compose.yml`](https://github.com/GIScience/openrouteservice/blob/master/docker-compose.yml) for examples.

**Environment variables**

- `BUILD_GRAPHS`: Forces ORS to rebuild the routings graph(s) when set to `True`. Useful when another PBF is specified in the Docker volume mapping to `/home/ors/ors-core/data/osm_file.pbf`
- `JAVA_OPTS`: Custom Java runtime options, such as `-Xms` or `-Xmx`
- `CATALINA_OPTS`: Custom Catalina options

Specify either during container startup or in `docker-compose.yml`.

**Build arguments**

When building the image, the following arguments are customizable:

- `ORS_CONFIG`: Can be changed to specify the location of a custom `ors-config.json` file. Default `./ors-api/src/main/resources/ors-config.json`.
- `OSM_FILE`: Can be changed to point to a local custom OSM file. Default `./ors-api/src/test/files/heidelberg.osm.gz`.

**Customization**

Once you have a built image you can decide to start a container with different settings, e.g. changing the active profiles or other settings in the `ors-config.json`.

**Different OSM file**

Either you point the build argument `OSM_FILE` to your desired OSM file during building the image.

Or to change the PBF file when restarting a container:

1. change the path `/home/ors/ors-core/data/osm_file.pbf` is pointing to your new PBF
2. set the `BUILD_GRAPHS` variable to `True`

E.g.
`docker run -d -p 8080:8080 -e BUILD_GRAPHS=True -v ./data/andorra-latest.osm.pbf:/home/ors/ors-core/data/osm_file.pbf ors-app`

It should be mentioned that if your dataset is very large, it may be necessary to adjust the `-Xmx` parameter of `JAVA_OPTS` environment variable. A good rule of thumb is to give Java 2 x file size of the PBF **per profile**.

Note, `.osm`, `.osm.gz`, `.osm.zip` and `.pbf` file format are supported as OSM files.

**Customize ors configuration**

Either you point the build argument `ORS_CONFIG` to your custom `ors-config.json` file.

The `ors-config.json` which is used is also copied to the container's `/share` directory. By mapping a directory to that path, you will get access to the `ors-config.json`. After changing values in the `ors-config.json` just restart the container. Example:

`docker run -d -p 8080:8080 -v ./conf:/ors-conf ors-app`

**Checking**

By default the service status is queriable via the `http://localhost:8080/ors/v2/health` endpoint. When the service is ready, you will be able to request `http://localhost:8080/ors/v2/status` for further information on the running services.

If you use the default dataset you will be able to request `http://localhost:8080/ors/v2/directions/foot-walking?start=8.676581,49.418204&end=8.692803,49.409465` for test purposes.
