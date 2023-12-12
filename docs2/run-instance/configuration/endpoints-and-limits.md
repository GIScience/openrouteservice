# Endpoints and limits

A named list of **openrouteservice** services containing their settings limits.

| key                      | type   | description                                        | example value                         |
|--------------------------|--------|----------------------------------------------------|---------------------------------------|
| ors.endpoints.routing    | object | settings for routing endpoint and vehicle profiles | [routing](#orsendpointsrouting)       | 
| ors.endpoints.isochrones | object | settings for the isochrones endpoint               | [isochrones](#orsendpointsisochrones) | 
| ors.endpoints.matrix     | object | settings for the matrix endpoint                   | [matrix](#orsendpointsmatrix)         | 
| ors.endpoints.snap       | object | settings for the snapping endpoint                 | [snap](#orsendpointssnap)             | 

## ors.endpoints.routing

| key                          | type    | description                                                                                                                                                                                                             | example value                                        |
|------------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|      
| enabled                      | boolean | Enables or disables (true/false) the end-point. Default value is true.                                                                                                                                                  | `true`                                               |
| description                  | string  |                                                                                                                                                                                                                         | `"This is a routing file from openrouteservice"`     |
| routing_name                 | string  | Specifies the gpx `name` tag that is returned in a gpx response                                                                                                                                                         | `"openrouteservice"`                                 |
| sources                      | list    | the osm file to be used, formats supported are `.osm`, `.osm.gz`, `.osm.zip` and `.pbf`                                                                                                                                 | `["heidelberg.osm.gz"]`                              |
| init_threads                 | number  | The number of threads used to initialize (build/load) graphs. Higher numbers requires more RAM.                                                                                                                         | `2`                                                  |
| attribution                  | string  | Attribution added to the response metadata                                                                                                                                                                              | `"openrouteservice.org, OpenStreetMap contributors"` |
| elevation_preprocessed       | boolean | Enables or disables reading ele tags for nodes. Default value is false. If enabled, GH's elevation lookup is prevented and all nodes without ele tag will default to 0. Experimental, for use with the ORS preprocessor | `false`                                              | 
| maximum_avoid_polygon_area   | number  | The maximum allowed total area of a polygon in square kilometers, optional                                                                                                                                              | `200000000`                                          |
| maximum_avoid_polygon_extent | number  | The maximum extent (i.e. envelope side length) of a polygon in kilometers, optional                                                                                                                                     | `20000`                                              |
| maximum_alternative_routes   | number  | The maximum number of alternative routes in a request                                                                                                                                                                   | `3`                                                  |

## Properties in the `routing` block

| key                                             | type   | description                                                                            | default value                     |
|-------------------------------------------------|--------|----------------------------------------------------------------------------------------|-----------------------------------|
| ors.endpoints.routing.base_url                  | string |                                                                                        | https://openrouteservice.org/     |
| ors.endpoints.routing.swagger_documentation_url | string | Define the URL for the the swagger documentation. Can be different from the `base_url` | https://api.openrouteservice.org/ |
| ors.endpoints.routing.support_mail              | string |                                                                                        | support@openrouteservice.org      |
| ors.endpoints.routing.author_tag                | string |                                                                                        | openrouteservice                  |
| ors.endpoints.routing.content_licence           | string |                                                                                        | LGPL 3.0                          ||    

