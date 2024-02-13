# `ors.endpoints.isochrones`

Settings for the isochrones endpoint.

| key                            | type    | description                                                                                        | default value                                      |
|--------------------------------|---------|----------------------------------------------------------------------------------------------------|----------------------------------------------------|
| enabled                        | boolean | Enables or disables the end-point                                                                  | `true`                                             |
| attribution                    | string  | Attribution added to the response metadata                                                         | `openrouteservice.org, OpenStreetMap contributors` |
| maximum_locations              | number  | Maximum number of locations per request                                                            | `2`                                                |
| allow_compute_area             | boolean | Toggles area computation on and off                                                                | `true`                                             |
| maximum_intervals              | number  | Maximum number of intervals/isochrones computed for each location                                  | `1`                                                |
| fastisochrones                 | object  | Range limits for [fastisochrones](#fastisochrones)                                                 |                                                    |
| statistics_providers           | object  | [Statistics providers properties](#statistics-providers)                                           |                                                    |
| maximum_range_distance_default | number  | Maximum default range in metres across all profiles; can be overridden in `maximum_range_distance` | `50000`                                            |
| maximum_range_distance         | object  | List of [maximum range properties](#maximum-range-properties)                                      |                                                    |
| maximum_range_time_default     | number  | Maximum default range in seconds across all profiles; can be overridden in `maximum_range_time`    | `18000`                                            |
| maximum_range_time             | object  | List of [maximum range properties](#maximum-range-properties)                                      |                                                    |

## fastisochrones

Properties beneath `ors.endpoints.isochrones.fastisochrones` for fastisochrone only:

| key                            | type   | description                                                                                        | default value |
|--------------------------------|--------|----------------------------------------------------------------------------------------------------|---------------|
| maximum_range_distance_default | number | Maximum default range in metres across all profiles; can be overridden in `maximum_range_distance` | `50000`       |
| maximum_range_distance         | object | [`maximum range properties`](#maximum-range-properties) object                                     |               |
| maximum_range_time_default     | number | Maximum default range in seconds across all profiles; can be overridden in `maximum_range_time`    | `18000`       |
| maximum_range_time             | object | [`maximum range properties`](#maximum-range-properties) object                                     |               |

### maximum_range properties
These properties can be nested beneath
* `ors.endpoints.isochrones.maximum_range_distance`
* `ors.endpoints.isochrones.maximum_range_time`
* `ors.endpoints.isochrones.fastisochrones.maximum_range_distance`
* `ors.endpoints.isochrones.fastisochrones.maximum_range_time`

| key      | type   | description                     | example value              |
|----------|--------|---------------------------------|----------------------------|
| profiles | list   | List of profile names           | `driving-car, driving-hgv` |
| value    | number | Value of distance or time limit | `3600`                     |


## statistics_providers
Properties beneath `ors.endpoints.isochrones.statistics_providers`:

| key                 | type    | description                                                                                                  | example value                                   | 
|---------------------|---------|--------------------------------------------------------------------------------------------------------------|-------------------------------------------------|
| enabled             | boolean | Enables or disables the provider                                                                             | true                                            |
| attribution         | string  | Provider attribution                                                                                         | OpenStreetMap contributors                      |
| provider_name       | string  | Internal name used to reference this provider (no effect)                                                    | my_statistics_provider_1                        |
| provider_parameters | object  | A list of provider configuration parameters                                                                  | see [provider_parameters](#provider-parameters) |
| property_mapping    | object  | Key-Value pairs with the name of the value to request in the `attributes` parameter in an isochrones request | total_pop: total_pop (only supported currently) |

::: warning
To adjust the `property_mapping` further implementations are needed in `IsochronesRequestEnums.java`,
`PostgresSQLStatisticsProvider.java`, `RoutingProfile.java` and `GeoJSONIsochroneProperties.java` are needed.
:::

### provider_parameters

Properties beneath `ors.endpoints.isochrones.statistics_providers.provider_parameters`:

| key             | type   | description                                                | example value       |
|-----------------|--------|------------------------------------------------------------|---------------------|
| host            | string | Host where the postgres/postgis DB is running on           | "localhost",        |
| port            | number | Port the DB is running on                                  | 5432,               |
| user            | string | DB user                                                    | "admin",            |
| password        | string | DB password for user                                       | "my_safe_password", |
| db_name         | string | Name of the database                                       | "postgres",         |
| table_name      | string | Name of the table used in SQL query of                     | "pop_data",         |
| geometry_column | string | Name of column used in SQL query of                        | "rast",             |
| postgis_version | string | Postgis Version used for conditional logic in SQL query of | "3"                 |

