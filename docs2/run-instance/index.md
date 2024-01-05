# Run your own instance

If you need to customize the behavior of openrouteservice or if the features or quota provided by our public API is not sufficient for your needs, you can run your own openrouteservice instance on a server or your local computer. There are multiple options to achieve this. 

## Quick start

The fastest and easiest way to have an instance of openrouteservice running is to use our docker compose file. If you have docker installed, running the following commands should get everything done.

```shell
wget https://raw.githubusercontent.com/GIScience/openrouteservice/master/docker-compose.yml
docker compose up
```

This will pull the latest nightly build of openrouteservice from dockerhub and start it up using an example setup and the provided test OSM file for Heidelberg/Germany and surrounding area.
You can then modify the configuration and source file settings to match your needs. For more details, check the [Running in a container](running-in-container) section.

## Installation

Please read the documentation regarding [system requirements](system-requirements) and [data](data) before continuing to properly [install](installation/) your openrouteservice instance.  

For more information on configuring openrouteservice for your specific needs, see the [configuration](configuration/) documentation.