# Install and run openrouteservice with docker

Installing the openrouteservice backend service with **Docker** is quite straightforward.

Please clone the repository and run the following command within the `docker/` directory:

```bash
cd docker
docker-compose up -d
```

This will:

1. Build the openrouteservice [war file](https://www.wikiwand.com/en/WAR_(file_format)) from the local codebase and on container startup it sets `docker/conf/app.config.sample` as the config file and the OpenStreetMap dataset for Heidelberg under `docker/data/heidelberg.osm.gz` as sample data.
2. Launch the openrouteservice service on port `8080` within a tomcat container at the address `http://localhost:8080/ors`.

## Volumes

There are some important directories one might want to preserve on the host machine, to survive container regeneration. These directories should be mapped as volumes.

- `/ors-core/data/graphs`: Contains the built graphs after ORS intialized.
- `/ors-core/data/elevation_cache`: Contains the CGIAR elevation tiles if elevation was specified.
- `/var/log/ors/`: Contains the ORS log.
- `/usr/local/tomcat/logs`: Contains the Tomcat log.
- `/share/`: The directory the `app.config` file will be copied to, needed for customization.
- `/ors-core/data/osm_file.pbf`: The OSM file being used to generate graphs.

## Environment variables

- `BUILD_GRAPHS`: Forces ORS to rebuild the routings graph(s) when set to `True`. Useful when another PBF is specified in the Docker volume mapping to `/ors-core/data/osm_file.pbf`
- `JAVA_OPTS`: Custom Java runtime options, such as `-Xms` or `-Xmx`
- `CATALINA_OPTS`: Custom Catalina options

Specify either during container startup or in `docker-compose.yml`.

## Build arguments

When building the image, the following arguments are customizable:

- `APP_CONFIG`: Can be changed to specify the location of a custom `app.config` file. Default: `./docker/conf/app.config.sample`.
- `OSM_FILE`: Can be changed to point to a local custom OSM file.

## Customization

Once you have a built image you can decide to start a container with differently configured openrouteservice, e.g. changing the OSM file or other settings in the `app.config.sample`.

### Different OSM file

Either you point the build argument `OSM_FILE` to your desired OSM file during building the image.

Or to change the PBF file when restarting a container:

1. change the path `/ors-core/data/osm_file.pbf` is pointing to
2. set the `BUILD_GRAPHS` variable to `True`

E.g.
`docker run -d -p 8080:8080 -e BUILD_GRAPHS=True ./data/andorra-latest.osm.pbf:/ors-core/data/osm_file.pbf docker_ors-app`

It should be mentioned that if your dataset is very large, please adjust the `-Xmx` parameter of `JAVA_OPTS` environment variable. A good rule of thumb is to give Java 2 x file size of the PBF **per profile**.

Note, `.osm`, `.osm.gz`, `.osm.zip` and `.pbf` file format are supported as OSM files.

### Customize `app.config.sample`

Either you point the build argument `APP_CONFIG` to your custom `app.config` file.

The `app.config` which is used is also copied to the container's `/share` directory. By mapping a directory to that path, you will get access to the `app.config`. After changing values in the `app.config` just restart the container. Example:

`docker run -d -p 8080:8080 -v .conf:/share docker_ors-app`

## Checking

By default the service status is queriable via the `http://localhost:8080/ors/health` endpoint. When the service is ready, you will be able to request `http://localhost:8080/ors/status` for further information on the running services.

If you use the default dataset you will be able to request `http://localhost:8080/ors/routes?profile=foot-walking&coordinates=8.676581,49.418204|8.692803,49.409465` for test purposes.
