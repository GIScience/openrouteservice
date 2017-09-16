# Install and Run openrouteservice Backend via Docker

It's possible and easy to install and launch the openrouteservice backend service with Docker. Please note that the [Dockerfile](../Dockerfile) under the repository root directory is only for building the [WAR file](https://www.wikiwand.com/en/WAR_(file_format)).

## Short version

run the following command within this `docker/` directory:

```bash
docker-compose up
```

It will:

1. build and test the openrouteservice core from the local codebase with the `docker/conf/app.config.sample` as the config and the dataset for Heidelberg under `docker/data/` as sample data;
2. generate the built `ors.war` file and expose it to `docker/build/` directory;
3. launch the openrouteservice backend service on port `8080`.

The service status is queryable via `http://localhost:8080/ors/health` endpoint. When the service is ready, go to `http://localhost:8080/ors/status` and it will show more detailed information. A URL for test can be `http://localhost:8080/ors/routes?profile=foot-walking&coordinates=8.676581,49.418204|8.692803,49.409465`. It should be able to provide the recommanded walking path in JSON format.

## Long version

### WAR file building

For building the WAR file only, either run

```bash
docker run -v /Users/user/build:/ors-core/build giscience/openrouteservice
```

or

```bash
docker-compose up ors-build
```

If everything goes fine, the built `ors.war` file can be found under the host directory, e.g. `/Users/user/build` in the above `docker run` command or `./build/` in the `docker-compose` command.

### Run openrouteservice

No matter whether the WAR file has been built or not, run

```bash
docker-compose up
```

will get everything done with the sample Heidelberg dataset.

### Run with your own OSM dataset

Prepare the OSM dataset (formats support `.osm`, `.osm.gz`, `.osm.zip`, `.pbf`) in the `docker/data/` directory. Make your own `app.config` (check the sample with detailed comments [here](../openrouteservice/WebContent/WEB-INF/app.config.sample) for reference) and change the `APP_CONFIG` variable in `docker-compose.yml` to let it point to your customized `app.config`. Then, run `docker-compose up`.

It should be noticed that if your dataset is very big, please adjust the `-Xmx` parameter of `JAVA_OPTS` in `docker-compose.yml`. According to our experiences, it should be at least `180g` for the whole globe.
