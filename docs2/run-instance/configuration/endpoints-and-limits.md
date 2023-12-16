# Endpoints and limits

A named list of **openrouteservice** services containing their settings limits.

| key                                               | type   | description                                                                             |
|---------------------------------------------------|--------|-----------------------------------------------------------------------------------------|
| swagger_documentation_url                         | string | Defines the URL for the the swagger documentation. Can be different from the `base_url` |
| [ors.endpoints.defaults](#orsendpointsdefaults)   | object | common settings for all endpoints                                                       |
| [ors.endpoints.routing](#orsendpointsrouting)     | object | settings for routing endpoint                                                           |
| [ors.endpoints.isochrone](#orsendpointsisochrone) | object | settings for the isochrones endpoint                                                    |
| [ors.endpoints.matrix](#orsendpointsmatrix)       | object | settings for the matrix endpoint                                                        |
| [ors.endpoints.snap](#orsendpointssnap)           | object | settings for the snapping endpoint                                                      | 

## ors.endpoints.defaults

| key         | type    | description                                | default value |
|-------------|---------|--------------------------------------------|---------------|      
| attribution | string  | Attribution added to the response metadata | _NA_          |

## ors.endpoints.routing

| key                          | type    | description                                                                         | default value                                                                  |
|------------------------------|---------|-------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|      
| enabled                      | boolean | Enables or disables the end-point. Default value is true.                           | true                                                                           |
| attribution                  | string  | Attribution added to the response metadata                                          | openrouteservice.org, OpenStreetMap contributors, tmc - BASt                   |
| gpx_name                     | string  | Specifies the gpx `name` tag that is returned in a gpx response                     | ORSRouting                                                                     |
| gpx_description              | string  | Specifies the gpx `description` tag that is returned in a gpx response              | This is a directions instructions file as GPX, generated from openrouteservice |
| gpx_base_url                 | string  |                                                                                     | https://openrouteservice.org/                                                  |                                                 
| gpx_support_mail             | string  |                                                                                     | support@openrouteservice.org                                                   | 
| gpx_author                   | string  |                                                                                     | openrouteservice                                                               |                                                 
| gpx_content_licence          | string  |                                                                                     | LGPL 3.0                                                                       | 
| maximum_avoid_polygon_area   | number  | The maximum allowed total area of a polygon in square kilometers, optional          | `200000000`                                                                    |
| maximum_avoid_polygon_extent | number  | The maximum extent (i.e. envelope side length) of a polygon in kilometers, optional | `20000`                                                                        |
| maximum_alternative_routes   | number  | The maximum number of alternative routes in a request                               | `3`                                                                            |

## ors.endpoints.matrix

| key                     | type    | description                                                                         | default value                                    |
|-------------------------|---------|-------------------------------------------------------------------------------------|--------------------------------------------------|      
| enabled                 | boolean | Enables or disables the end-point. Default value is true.                           | true                                             |
| attribution             | string  | Attribution added to the response metadata                                          | openrouteservice.org, OpenStreetMap contributors |                                                              | 
| maximum_routes          | number  | The maximum allowed total area of a polygon in square kilometers, optional          | 2500                                             |
| maximum_routes_flexible | number  | The maximum extent (i.e. envelope side length) of a polygon in kilometers, optional | 25                                               |
| maximum_visited_nodes   | number  | The maximum number of visited nodes in a request                                    | 100000                                           |
| maximum_search_radius   | number  |                                                                                     | 2000                                             |
| u_turn_cost             | number  |                                                                                     | -1                                               |

## ors.endpoints.isochrone

| key                            | type    | description                                                      | default value                                    |
|--------------------------------|---------|------------------------------------------------------------------|--------------------------------------------------|      
| enabled                        | boolean | Enables or disables the end-point. Default value is true.        | true                                             |
| attribution                    | string  | Attribution added to the response metadata                       | openrouteservice.org, OpenStreetMap contributors |                                                              | 
| maximum_locations              | number  |                                                                  | 2                                                |
| allow_compute_area             | boolean |                                                                  | true                                             |
| maximum_intervals              | number  |                                                                  | 1                                                |
| fastisochrones                 | object  | Range limits for [fastisochrones](fastisochroneproperties)       | 2000                                             |
| statistics_providers           | object  | [Statistics providers properties](statisticsprovidersproperties) |                                                  |
| maximum_range_distance_default | number  |                                                                  | 50000                                            |
| maximum_range_distance         | object  | List of [maximum range properties](#maximumrangeproperties)      |                                                  |
| maximum_range_time_default     | number  |                                                                  | 18000                                            |
| maximum_range_time             | object  | List of [maximum range properties](#maximumrangeproperties)      |                                                  |

### Fastisochrone properties

| key                            | type   | description                                                 | default value |
|--------------------------------|--------|-------------------------------------------------------------|---------------|      
| maximum_range_distance_default | number |                                                             | 50000         |
| maximum_range_distance         | object | List of [maximum range properties](#maximumrangeproperties) |               |
| maximum_range_time_default     | number |                                                             | 18000         |
| maximum_range_time             | object | List of [maximum range properties](#maximumrangeproperties) |               |

#### Maximum range properties

| key      | type   | description                     | example value              |
|----------|--------|---------------------------------|----------------------------|      
| profiles | object | List of profile names           | `driving-car, driving-hgv` |
| value    | number | Value of distance or time limit | `3600`                     |                                                              |

### Statistics providers properties

| key                 | type    | description                                | default value                                    |
|---------------------|---------|--------------------------------------------|--------------------------------------------------|      
| enabled             | boolean | Enables or disables the end-point          | true                                             |
| attribution         | string  | Attribution added to the response metadata | openrouteservice.org, OpenStreetMap contributors |                                                              | 
| provider_name       | string  |                                            |                                                  |                                                              | 
| provider_parameters | object  |                                            |                                                  |
| property_mapping    | object  |                                            |                                                  |

## ors.endpoints.snap

| key                  | type    | description                                               | default value                                    |
|----------------------|---------|-----------------------------------------------------------|--------------------------------------------------|      
| enabled              | boolean | Enables or disables the end-point. Default value is true. | true                                             |
| attribution          | string  | Attribution added to the response metadata                | openrouteservice.org, OpenStreetMap contributors |                                                              | 
