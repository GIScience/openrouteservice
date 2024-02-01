# Elevation Service

:::warning NOTE
This endpoint is not part of the openrouteservice, but of our public API. It is not available when running an own instance of openrouteservice.
:::

The elevation service is a Flask application which extracts elevation from various elevation datasets for Point or LineString 2D geometries and returns 3D geometries in various formats.
Just like the openrouteservice, the elevation service is also accessible via our public API.

Details on how to use it can be found in our [API Playground](https://openrouteservice.org/dev/#/api-docs/elevation).

If you need deeper insights or want to run your own instance, please visit our GitHub project [openelevationservice](https://github.com/GIScience/openelevationservice).