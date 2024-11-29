---
title: A comprehensive guide to the location of the OSM file
---
Let's say you have just downloaded a fresh OSM file that you want to use for routing, e.g. `andorra-latest.osm.pbf`.
Given the multiple ways to set up and configure the openrouteservice, it's not quite clear where this should go.

In theory, this should be easy. The osm file used is simply the one set in the `sources` parameter in the configuration file.
However, there's a few pitfalls:

## How does ORS know which configuration file to use

The openrouteservice will first determine the configuration file being used.
To do so, it searches the following places:

1. A system property called `ors_config`. This is useful to set a specific
   configuration file, e.g. running ors with a configuration specific for API
   tests in IDEA by setting the `ors_config` property in the Runner configuration.

2. A system property called `ors_app_config`. This is deprecated, the above name should be used.

3. An environment variable called `ORS_CONFIG`. This is useful to set a
   specific configuration file when building the ors via a script, such as a
   Dockerfile.

4. An environment variable called `ORS_APP_CONFIG`. This is deprecated, the above name should be used.

5. A file named `ors-config.json` in the class path. When built with maven and
   run through tomcat, that file should be located in
   `openrouteservice/src/main/resources`.

6. A file named `app.config` in the class path. This is deprecated, the above name should be used.


k


* location configured in osm-config.json in `openrouteservice/openrouteservice/src/main/resources`
  search for `sources`
* sources is an array containing one string to the osm file to be used
* relative to ???
