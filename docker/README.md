# Install and run openrouteservice with docker

Installing the openrouteservice backend service with **Docker** is quite straightforward.

Please clone the repository and run the following command within this `docker/` directory:

```bash
docker-compose up -d
```

This will:

1. Build the openrouteservice [war file](https://www.wikiwand.com/en/WAR_(file_format)) from the local codebase with the `docker/conf/app.config.sample` as the config file and the OpenStreetMap dataset for Heidelberg under `docker/data/` as sample data.
2. Launch the openrouteservice service on port `8080` within a tomcat container.

By default the service status is queryable via the `http://localhost:8080/ors/health` endpoint. When the service is ready, you will be able to request `http://localhost:8080/ors/status` for further information on the running services.
If you use the default dataset you will be able to request `http://localhost:8080/ors/routes?profile=foot-walking&coordinates=8.676581,49.418204|8.692803,49.409465` for test purposes.

## Run with your own OpenStreetMap dataset

Save your OSM dataset (formats supported are `.osm`, `.osm.gz`, `.osm.zip` and `.pbf`) in the `docker/data/` directory and then adapt `docker-compose.yml`.
Afterwards run `docker-compose up -d` (if you want to rebuild graphs with a new OSM file first of all delete the contents of the `docker/graphs` folder and restart the service with `docker-compose up -d`).

It should be mentioned that if your dataset is very large, please adjust the `-Xmx` parameter of `JAVA_OPTS` in `docker-compose.yml`.
According to our experience, this should be at least `180g` for the whole globe if you are planning to use 3 or more modes of transport at the same time.