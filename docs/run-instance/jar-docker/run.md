# Run Docker Image with openrouteservice JAR



## With docker compose

The fastest and easiest way to have an instance of openrouteservice running is to use our docker compose file. If you have docker installed, running the following commands should get everything done.

```shell
wget https://raw.githubusercontent.com/GIScience/openrouteservice/main/docker-compose.yml
docker compose up
```

This will pull the latest nightly build of openrouteservice from Docker Hub and start it up using an example setup and the provided test OSM file for Heidelberg/Germany and surrounding area.
You can then modify the configuration and source file settings to match your needs. For more details, check the [Running with Docker](installation/running-with-docker) section.



## With docker run
