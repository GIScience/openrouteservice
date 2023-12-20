# Endpoints and limits

A named list of **openrouteservice** services containing their settings limits.

| key                                               | type   | description                                                                             |
|---------------------------------------------------|--------|-----------------------------------------------------------------------------------------|
| swagger_documentation_url                         | string | Defines the URL for the the swagger documentation. Can be different from the `base_url` |
| [ors.endpoints.defaults](#orsendpointsdefaults)   | object | Common settings for all endpoints                                                       |
| [ors.endpoints.routing](#orsendpointsrouting)     | object | Settings for routing endpoint                                                           |
| [ors.endpoints.isochrone](#orsendpointsisochrone) | object | Settings for the isochrones endpoint                                                    |
| [ors.endpoints.matrix](#orsendpointsmatrix)       | object | Settings for the matrix endpoint                                                        |
| [ors.endpoints.snap](#orsendpointssnap)           | object | Settings for the snapping endpoint                                                      |

## ors.endpoints.defaults

| key         | type    | description                                | default value |
|-------------|---------|--------------------------------------------|---------------|
| attribution | string  | Attribution added to the response metadata | _NA_          |

## ors.endpoints.routing

| key                          | type    | description                                                                            | default value                                                                    |
|------------------------------|---------|----------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| enabled                      | boolean | Enables or disables the end-point                                                      | `true`                                                                           |
| attribution                  | string  | Attribution added to the response metadata                                             | `openrouteservice.org, OpenStreetMap contributors, tmc - BASt`                   |
| gpx_name                     | string  | Specifies the `name` tag that is returned in `metadata` of a gpx response              | `ORSRouting`                                                                     |
| gpx_description              | string  | Specifies the `desc` tag that is returned in `metadata` of a gpx response              | `This is a directions instructions file as GPX, generated from openrouteservice` |
| gpx_base_url                 | string  | Specifies the `link` tag that is returned in `metadata/author` of a gpx response       | `https://openrouteservice.org/`                                                  |
| gpx_support_mail             | string  | Specifies the `email` tag that is returned in `metadata/author` of a gpx response      | `support@openrouteservice.org`                                                   |
| gpx_author                   | string  | Specifies the `name` tag that is returned in `metadata/author` of a gpx response       | `openrouteservice`                                                               |
| gpx_content_licence          | string  | Specifies the `license` tag that is returned in `metadata/copyright` of a gpx response | `LGPL 3.0`                                                                       |
| maximum_avoid_polygon_area   | number  | The maximum allowed total area of a polygon in square kilometers, optional             | `200000000`                                                                      |
| maximum_avoid_polygon_extent | number  | The maximum extent (i.e. envelope side length) of a polygon in kilometers, optional    | `20000`                                                                          |
| maximum_alternative_routes   | number  | The maximum number of alternative routes in a request                                  | `3`                                                                              |

## ors.endpoints.matrix

| key                     | type    | description                                                                                                                      | default value                                      |
|-------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| enabled                 | boolean | Enables or disables the end-point                                                                                                | `true`                                             |
| attribution             | string  | Attribution added to the response metadata                                                                                       | `openrouteservice.org, OpenStreetMap contributors` |
| maximum_routes          | number  | Maximum amount of routes the matrix should compute; e.g. the value of `2500` could correspond to a 50x50 matrix or 1x2500 matrix | `2500`                                             |
| maximum_routes_flexible | number  | Maximum amount of routes for profiles that do not have the CH routing algorithm enabled                                          | `25`                                               |
| maximum_visited_nodes   | number  | Maximum allowed number of visited nodes in shortest path computation                                                             | `100000`                                           |
| maximum_search_radius   | number  | Maximum allowed distance in meters between the requested coordinate and a point on the nearest road                              | `2000`                                             |
| u_turn_cost             | number  | Penalty of performing a U-Turn; the value of `-1` prevents them entirely                                                         | `-1`                                               |

## ors.endpoints.isochrone

| key                            | type    | description                                                                                        | default value                                      |
|--------------------------------|---------|----------------------------------------------------------------------------------------------------|----------------------------------------------------|
| enabled                        | boolean | Enables or disables the end-point                                                                  | `true`                                             |
| attribution                    | string  | Attribution added to the response metadata                                                         | `openrouteservice.org, OpenStreetMap contributors` |
| maximum_locations              | number  | Maximum number of locations per request                                                            | `2`                                                |
| allow_compute_area             | boolean | Toggles area computation on and off                                                                | `true`                                             |
| maximum_intervals              | number  | Maximum number of intervals/isochrones computed for each location                                  | `1`                                                |
| fastisochrones                 | object  | Range limits for [fastisochrones](#fastisochrone-properties)                                       | `2000`                                             |
| statistics_providers           | object  | [Statistics providers properties](#statistics-providers-properties)                                |                                                    |
| maximum_range_distance_default | number  | Maximum default range in metres across all profiles; can be overridden in `maximum_range_distance` | `50000`                                            |
| maximum_range_distance         | object  | List of [maximum range properties](#maximum-range-properties)                                      |                                                    |
| maximum_range_time_default     | number  | Maximum default range in seconds across all profiles; can be overridden in `maximum_range_time`    | `18000`                                            |
| maximum_range_time             | object  | List of [maximum range properties](#maximum-range-properties)                                      |                                                    |

### Fastisochrone properties

| key                            | type   | description                                                                                        | default value |
|--------------------------------|--------|----------------------------------------------------------------------------------------------------|---------------|
| maximum_range_distance_default | number | Maximum default range in metres across all profiles; can be overridden in `maximum_range_distance` | `50000`       |
| maximum_range_distance         | object | List of [maximum range properties](#maximum-range-properties)                                      |               |
| maximum_range_time_default     | number | Maximum default range in seconds across all profiles; can be overridden in `maximum_range_time`    | `18000`       |
| maximum_range_time             | object | List of [maximum range properties](#maximum-range-properties)                                      |               |

#### Maximum range properties

| key      | type   | description                     | example value              |
|----------|--------|---------------------------------|----------------------------|
| profiles | object | List of profile names           | `driving-car, driving-hgv` |
| value    | number | Value of distance or time limit | `3600`                     |

### Statistics providers properties

| key                 | type    | description                                 | 
|---------------------|---------|---------------------------------------------|
| enabled             | boolean | Enables or disables the provider            |
| attribution         | string  | Provider attribution                        |
| provider_name       | string  |                                             |
| provider_parameters | object  | A list of provider configuration parameters |
| property_mapping    | object  |                                             |

## ors.endpoints.snap

| key         | type    | description                                | default value                                      |
|-------------|---------|--------------------------------------------|----------------------------------------------------|
| enabled     | boolean | Enables or disables the end-point          | `true`                                             |
| attribution | string  | Attribution added to the response metadata | `openrouteservice.org, OpenStreetMap contributors` |
