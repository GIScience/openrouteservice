# `ors.endpoints.isochrone`

Settings for the isochrones endpoint.

| key                            | type    | description                                                                                        | default value                                      |
|--------------------------------|---------|----------------------------------------------------------------------------------------------------|----------------------------------------------------|
| enabled                        | boolean | Enables or disables the end-point                                                                  | `true`                                             |
| attribution                    | string  | Attribution added to the response metadata                                                         | `openrouteservice.org, OpenStreetMap contributors` |
| maximum_locations              | number  | Maximum number of locations per request                                                            | `2`                                                |
| allow_compute_area             | boolean | Toggles area computation on and off                                                                | `true`                                             |
| maximum_intervals              | number  | Maximum number of intervals/isochrones computed for each location                                  | `1`                                                |
| fastisochrones                 | object  | Range limits for [fastisochrones](#fastisochrones)                                                 | `2000`                                             |
| statistics_providers           | object  | [Statistics providers properties](#statistics-providers)                                           |                                                    |
| maximum_range_distance_default | number  | Maximum default range in metres across all profiles; can be overridden in `maximum_range_distance` | `50000`                                            |
| maximum_range_distance         | object  | List of [maximum range properties](#maximum-range-properties)                                      |                                                    |
| maximum_range_time_default     | number  | Maximum default range in seconds across all profiles; can be overridden in `maximum_range_time`    | `18000`                                            |
| maximum_range_time             | object  | List of [maximum range properties](#maximum-range-properties)                                      |                                                    |

## fastisochrones

Properties beneath `ors.endpoints.isochrone.fastisochrones` for fastisochrone only:

| key                            | type   | description                                                                                        | default value |
|--------------------------------|--------|----------------------------------------------------------------------------------------------------|---------------|
| maximum_range_distance_default | number | Maximum default range in metres across all profiles; can be overridden in `maximum_range_distance` | `50000`       |
| maximum_range_distance         | object | List of [maximum range properties](#maximum-range-properties)                                      |               |
| maximum_range_time_default     | number | Maximum default range in seconds across all profiles; can be overridden in `maximum_range_time`    | `18000`       |
| maximum_range_time             | object | List of [maximum range properties](#maximum-range-properties)                                      |               |

### maximum_range properties
These properties can be nested beneath
* `ors.endpoints.isochrone.maximum_range_distance`
* `ors.endpoints.isochrone.maximum_range_time`
* `ors.endpoints.isochrone.fastisochrones.maximum_range_distance`
* `ors.endpoints.isochrone.fastisochrones.maximum_range_time`

| key      | type   | description                     | example value              |
|----------|--------|---------------------------------|----------------------------|
| profiles | list   | List of profile names           | `driving-car, driving-hgv` |
| value    | number | Value of distance or time limit | `3600`                     |


## statistics_providers
Properties beneath `ors.endpoints.isochrone.statistics_providers`:

| key                 | type    | description                                 | 
|---------------------|---------|---------------------------------------------|
| enabled             | boolean | Enables or disables the provider            |
| attribution         | string  | Provider attribution                        |
| provider_name       | string  |                                             |
| provider_parameters | object  | A list of provider configuration parameters |
| property_mapping    | object  |                                             |
