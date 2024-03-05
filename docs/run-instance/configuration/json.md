# Configuration via `ors-config.json`

:::warning
The ors-config.json is deprecated!
:::

The "old" configuration method is supported for a while for convenience. 
The description below is kept as long as openrouteservice still supports configuration via JSON file, 
but we do **not** recommend using this configuration method. 
Please consider to [migrate JSON configuration](migrate-from-json.md) to the new style.
Note that currently all settings in a provided JSON configuration file will **override** any settings in the YAML file.

## ors

The top level element.

| key            | type   | description                       | example value                          |
|----------------|--------|-----------------------------------|----------------------------------------|
| services       | object | an object comprising the services | [services](#ors-services)              |  
| logging        | object | the logging properties            | [logging](#ors-logging)                |
| system_message | list   | List of system message objects    | [system messages](#ors-system-message) |

---

### ors.services

| key                     | type   | description                           | example value                          |
|-------------------------|--------|---------------------------------------|----------------------------------------|
| ors.services.routing    | object | settings for routing and its profiles | [routing](#ors-services-routing)       | 
| ors.services.isochrones | object | settings for isochrones restrictions  | [isochrones](#ors-services-isochrones) | 
| ors.services.matrix     | object | settings for matrix restrictions      | [matrix](#ors-services-matrix)         | 
| ors.services.snap       | object | settings for snap                     | [snap](#ors-services-snap)             | 

---

#### ors.services.routing

| key                    | type    | description                                                                                                                                                                                                             | example value                                        |
|------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|      
| enabled                | boolean | Enables or disables (true/false) the end-point. Default value is true.                                                                                                                                                  | `true`                                               |
| mode                   | string  | Can be either "normal" or "preparation". If "preparation" then graphs will be built and the service will be shut down afterwards.                                                                                       | `"normal"`                                           |
| description            | string  |                                                                                                                                                                                                                         | `"This is a routing file from openrouteservice"`     |
| routing_name           | string  | Specifies the gpx `name` tag that is returned in a gpx response                                                                                                                                                         | `"openrouteservice"`                                 |
| sources                | list    | the osm file to be used, formats supported are `.osm`, `.osm.gz`, `.osm.zip` and `.pbf`                                                                                                                                 | `["heidelberg.osm.gz"]`                              |
| init_threads           | number  | The number of threads used to initialize (build/load) graphs. Higher numbers requires more RAM.                                                                                                                         | `2`                                                  |
| attribution            | string  | Attribution added to the response metadata                                                                                                                                                                              | `"openrouteservice.org, OpenStreetMap contributors"` |
| elevation_preprocessed | boolean | Enables or disables reading ele tags for nodes. Default value is false. If enabled, GH's elevation lookup is prevented and all nodes without ele tag will default to 0. Experimental, for use with the ORS preprocessor | `false`                                              |
| profiles               | object  |                                                                                                                                                                                                                         | [profiles](#ors-services-routing-profiles)           |

---

##### **ors.services.routing.profiles**

| key            | type   | description                                                                                                                                                                                                                                                                                                    | example value                                                   |
|----------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|      
| active         | list   | Defines a list of active routing profiles. The element name XXX must correspond to a notation "profile-XXX", which is used in the following sections. Can be one or many of `"car"`, `"hgv"`, `"bike-regular"`, `"bike-mountain"`, `"bike-road"`, `"bike-electric"`, `"walking"`, `"hiking"` or `"wheelchair"` | `["car", "bike-regular"]`                                       |
| default_params | object | Set parameters that is applied to every profile by default                                                                                                                                                                                                                                                     | [default_params](#ors-services-routing-profiles-default-params) |
| profile-XXX    | object | Settings that are applied to the specific profile. You may run multiple profiles at once.                                                                                                                                                                                                                      | [profile-XXX](#ors-services-routing-profiles-profile-xxx)       |

---

##### **ors.services.routing.profiles.default_params**

| key                                               | type    | description                                                                                                                                                                                                                                                                                                                                                       | example value                                                            |
|---------------------------------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------| 
| encoder_flags_size                                | number  | The number of bytes used for FlagEncoders                                                                                                                                                                                                                                                                                                                         | `8`                                                                      |
| graphs_root_path                                  | string  | The root path to a directory for storing graphs                                                                                                                                                                                                                                                                                                                   | `"../graphs"`                                                            |
| elevation_provider                                | string  | The name of an elevation provider. Possible values are multi, cgiar or srtm                                                                                                                                                                                                                                                                                       | `"multi"`                                                                |
| elevation_cache_path                              | string  | The path to a directory in which SRTM tiles will be stored                                                                                                                                                                                                                                                                                                        | `"elevation_cache"`                                                      |
| elevation_cache_clear                             | boolean | keep elevation data once it has been downloaded                                                                                                                                                                                                                                                                                                                   | `false`                                                                  |
| elevation_smoothing                               | boolean | smooth out elevation data                                                                                                                                                                                                                                                                                                                                         | `false`                                                                  |
| instructions                                      | boolean | Specifies whether way names will be stored during the import or not                                                                                                                                                                                                                                                                                               | `true`                                                                   |
| maximum_distance                                  | number  | The maximum allowed total distance of a route                                                                                                                                                                                                                                                                                                                     | `100000`                                                                 |
| maximum_segment_distance_<br>with_dynamic_weights | number  | The maximum allowed distance between two way points when dynamic weights are used                                                                                                                                                                                                                                                                                 | `50000`                                                                  |
| maximum_waypoints                                 | number  | The maximum number of way points in a request                                                                                                                                                                                                                                                                                                                     | `50`                                                                     |
| maximum_snapping_radius                           | number  | Maximum distance around a given coordinate to find connectable edges                                                                                                                                                                                                                                                                                              | `100`                                                                    |
| maximum_distance_round_trip_routes                | number  | The maximum allowed total distance of a route for the round trip algo                                                                                                                                                                                                                                                                                             | `100000`                                                                 |
| maximum_distance_alternative_routes               | number  | The maximum allowed total distance of a route for the alternative route algo                                                                                                                                                                                                                                                                                      | `100000`                                                                 |
| maximum_alternative_routes                        | number  | The maximum number of alternative routes in a request                                                                                                                                                                                                                                                                                                             | `3`                                                                      |
| maximum_avoid_polygon_area                        | number  | The maximum allowed total area of a polygon in square kilometers, optional                                                                                                                                                                                                                                                                                        | `200000000`                                                              |
| maximum_avoid_polygon_extent                      | number  | The maximum extent (i.e. envelope side length) of a polygon in kilometers, optional                                                                                                                                                                                                                                                                               | `20000`                                                                  |
| location_index_resolution                         | number  | The minimum resolution in meters of tiles in the location index. Lower values yield faster queries at a cost of increased memory requirements. Reducing the resolution reduces the lookup radius which can be compensated by increasing `location_index_search_iterations`. Corresponds to GraphHopper's `index.high_resolution` configuration parameter.         | `500` (default)                                                          |
| location_index_search_iterations                  | number  | The maximum number of iterations performed in coordinates lookup. Higher values yield a broader search area, but might reduce query performance. It only affects the storage lookup but not its layout so changing this parameter does not require rebuilding the location index. Corresponds to GraphHopper's `index.max_region_search` configuration parameter. | `4` (default)                                                            |
| maximum_speed_lower_bound                         | number  | Specifies the threshold for the query parameter `maximum_speed`.                                                                                                                                                                                                                                                                                                  | `80` (default)                                                           |
| interpolate_bridges_and_tunnels                   | boolean | Interpolate elevation of bridges and tunnels.                                                                                                                                                                                                                                                                                                                     | `true` (default)                                                         | 
| preparation                                       | object  | ...                                                                                                                                                                                                                                                                                                                                                               | [preparation](#ors-services-routing-profiles-default-params-preparation) |
| execution                                         | object  | ...                                                                                                                                                                                                                                                                                                                                                               | [execution](#ors-services-routing-profiles-default-params-execution)     |

---

##### **ors.services.routing.profiles.default_params.preparation**

| key              | type   | description                                                       | example value                                                                |
|------------------|--------|-------------------------------------------------------------------|------------------------------------------------------------------------------| 
| min_network_size | number | Minimum size (number of edges) of an independent routing subgraph | `200`                                                                        |
| methods          | object |                                                                   | [methods](#ors-services-routing-profiles-default-params-preparation-methods) |     

##### **ors.services.routing.profiles.default_params.preparation.methods**

| key  | type   | description                                        | example value                                                                  |
|------|--------|----------------------------------------------------|--------------------------------------------------------------------------------| 
| ch   | object | Settings for preprocessing contraction hierarchies | [ch](#ors-services-routing-profiles-default-params-preparation-methods-ch)     |
| lm   | object | Settings for preprocessing landmarks               | [lm](#ors-services-routing-profiles-default-params-preparation-methods-lm)     |
| core | object | Settings for preprocessing landmarks               | [core](#ors-services-routing-profiles-default-params-preparation-methods-core) |

##### **ors.services.routing.profiles.default_params.preparation.methods.ch**

| key        | type    | description | example value   |
|------------|---------|-------------|-----------------| 
| enabled    | boolean |             | `true`          |
| threads    | number  |             | `1`             |
| weightings | string  |             | `"recommended"` |

##### **ors.services.routing.profiles.default_params.preparation.methods.lm**

| key        | type    | description                                                                                                                                                               | example value            |
|------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------| 
| enabled    | boolean |                                                                                                                                                                           | `true`                   |
| threads    | number  |                                                                                                                                                                           | `1`                      |
| weightings | string  |                                                                                                                                                                           | `"recommended,shortest"` |
| landmarks  | number  | Total number of precomputed landmarks, the subset used during the query is set in [`active_landmarks`](#ors-services-routing-profiles-default-params-executionmethods-lm) | `16`                     |

##### **ors.services.routing.profiles.default_params.preparation.methods.core**

| key        | type    | description                                                                                                                                                                  | example value                                                 |
|------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------| 
| enabled    | boolean |                                                                                                                                                                              | `true`                                                        |
| threads    | number  |                                                                                                                                                                              | `1`                                                           |
| weightings | string  |                                                                                                                                                                              | `"recommended,shortest"`                                      |
| landmarks  | number  | Total number of precomputed landmarks, the subset used during the query is set in [`active_landmarks`](#ors-services-routing-profiles-default-params-execution-methods-core) | `32`                                                          |
| lmsets     | string  |                                                                                                                                                                              | `"highways,tollways;highways;tollways;country_193;allow_all"` |

---

##### **ors.services.routing.profiles.default_params.execution**

| key     | type   | description | example value                                                              |
|---------|--------|-------------|----------------------------------------------------------------------------| 
| methods | object |             | [methods](#ors-services-routing-profiles-default-params-execution-methods) |

##### **ors.services.routing.profiles.default_params.execution.methods**

| key  | type   | description                                           | example value                                                                |
|------|--------|-------------------------------------------------------|------------------------------------------------------------------------------| 
| ch   | object | Settings for using contraction hierarchies in routing | [ch](#ors-services-routing-profiles-default-params-execution-methods-ch)     |
| lm   | object | Settings for using landmarks in routing               | [lm](#ors-services-routing-profiles-default-params-execution-methods-lm)     |
| core | object | Settings for using landmarks in routing               | [core](#ors-services-routing-profiles-default-params-execution-methods-core) |

##### **ors.services.routing.profiles.default_params.execution.methods.lm**

| key               | type    | description                                      | example value |
|-------------------|---------|--------------------------------------------------|---------------| 
| active_landmarks  | number  | Number of landmarks used for computing the route | `8`           |

##### **ors.services.routing.profiles.default_params.execution.methods.core**

| key               | type    | description                                      | example value |
|-------------------|---------|--------------------------------------------------|---------------| 
| active_landmarks  | number  | Number of landmarks used for computing the route | `6`           |

---

##### **ors.services.routing.profiles.profile-XXX**

| key        | type   | description                                   | example value                                                       |
|------------|--------|-----------------------------------------------|---------------------------------------------------------------------| 
| profiles   | string | name of the profile when called via the API   | `driving-car`                                                       |
| parameters | object | the specific profile parameters of an profile | [parameters](#ors-services-routing-profiles-profile-xxx-parameters) |

##### **ors.services.routing.profiles.profile-XXX.parameters**

| key                                 | type    | description                                                                                       | example value                                                                           |
|-------------------------------------|---------|---------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------| 
| encoder_options                     | string  |                                                                                                   | `"turn_costs=true\                                                                      |block_fords=false"` [encoder options](#ors-services-routing-profiles-profile-xxx-parametersencoder_options) |
| elevation                           | boolean | This will enable the elevation information in the response                                        | `true`                                                                                  |
| maximum_distance                    | number  | Maximum distance a route can have in meters                                                       | `100000`                                                                                |
| maximum_snapping_radius             | number  | Maximum distance around a given coordinate to find connectable edges                              | `100`                                                                                   |
| maximum_distance_round_trip_routes  | number  | The maximum allowed total distance of a route for the round trip route algo                       | `100000`                                                                                |
| maximum_distance_alternative_routes | number  | The maximum allowed total distance of a route for the alternative route algo                      | `100000`                                                                                |
| maximum_alternative_routes          | number  | The maximum number of alternative routes in a request                                             | `3`                                                                                     |
| maximum_avoid_polygon_area          | number  | The maximum allowed total area of a polygon in square kilometers, optional                        | `200000000`                                                                             |
| maximum_avoid_polygon_extent        | number  | The maximum extent (i.e. envelope side length) of a polygon in kilometers, optional               | `20000`                                                                                 |
| ext_storages                        | object  | Controls which external storages are enabled                                                      | [external storages](#ors-services-routing-profiles-profile-xxx-parameters-ext-storages) |
| gtfs_file                           | string  | Only for pt profile: location of gtfs-file used. Can either be a zip-file or the unzipped folder. | `"src/test/files/vrn_gtfs_cut.zip"`                                                     |

##### **ors.services.routing.profiles.profile-XXX.parameters.encoder_options**

| key                      | type   | description                                                                                                                                                                       | example value                  |
|--------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------| 
| problematic_speed_factor | number | For wheelchair profile only! Travel speeds on edges classified as problematic for wheelchair users are multiplied by this factor, use to set slow traveling speeds on such ways   | `problematic_speed_factor=0.7` |
| preferred_speed_factor   | number | For wheelchair profile only! Travel speeds on edges classified as preferable for wheelchair users are multiplied by this factor, use to set faster traveling speeds on such ways | `preferred_speed_factor=1.2`   |

##### **ors.services.routing.profiles.profile-XXX.parameters.ext_storages**

| key                    | type   | description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | example value                                                                         |
|------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------| 
| WayCategory            | object | Returns the way category in the route response, Compatible for any profile type                                                                                                                                                                                                                                                                                                                                                                                                               | `{}`                                                                                  |
| WaySurfaceType         | object | Returns the way surface in the route response, Compatible for any profile type                                                                                                                                                                                                                                                                                                                                                                                                                | `{}`                                                                                  |
| HillIndex              | object | Returns the ascent/descent in the route response, Compatible for any profile type                                                                                                                                                                                                                                                                                                                                                                                                             | `{}`                                                                                  |
| TrailDifficulty        | object | Returns the trail difficulty in the route response, Compatible for profile-hiking                                                                                                                                                                                                                                                                                                                                                                                                             | `{}`                                                                                  |
| RoadAccessRestrictions | object | RoadAccessRestrictions are where roads are restricted to certain vehicles to certain circumstances, e.g. access=destination. The use_for_warnings parameter tells the ors that this storage can be used for generating warning messages in the route response. For RoadAccessRestrictions, this means that whenever a route goes over a way which has some restrictions, a warning message will be delivered with the response and the roadaccessrestrictions extra info automatically added. | `{ use_for_warnings: true }`                                                          |
| Wheelchair             | object | Compatible for wheelchair                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | `{ KerbsOnCrossings: "true" }`                                                        |
| OsmId                  | object | Returns the OsmId of the way, Compatible for wheelchair                                                                                                                                                                                                                                                                                                                                                                                                                                       | `{}`                                                                                  |
| Borders                | object | Borders allows the restriction of routes to not cross country borders, compatible for any profile type                                                                                                                                                                                                                                                                                                                                                                                        | [Borders](#ors-services-routing-profiles-profile-xxx-parameters-ext-storages-borders) |    

##### **ors.services.routing.profiles.profile-XXX.parameters.ext_storages.Borders**

| key         | type   | description                                                                                         | example value              |
|-------------|--------|-----------------------------------------------------------------------------------------------------|----------------------------| 
| boundaries  | string | The path to a file containing geojson data representing the borders of countries                    | `'borders.geojson.tar.gz'` |
| ids         | string | Path to a csv file containing a unique id for each country, its local name and its English name     | `'ids.csv'`                |
| openborders | string | Path to a csv file containing pairs of countries where the borders are open (i.e. Schengen borders) | `'openborders.csv'`        |

---

#### ors.services.isochrones

| key                    | type    | description                                                                                                                                | example value                                                                              |
|------------------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------| 
| enabled                | boolean | Enables or disables (true/false) the end-point (default: true)                                                                             | `true`                                                                                     |
| maximum_range_distance | list    | Possible values for maximum_range_distance and maximum_range_time are an integer or a list of values specifically defined for each profile | `[{profiles: "any", value: 50000}, {profiles: "driving-car, driving-hgv", value: 100000}]` |
| maximum_range_time     | list    |                                                                                                                                            | `[{profiles: "any", value: 18000},{profiles: "driving-car, driving-hgv", value: 3600}]`    |
| maximum_intervals      | number  | Maximum number of intervals/isochrones computed for each location                                                                          | `10`                                                                                       |
| maximum_locations      | number  | Maximum number of locations in one request                                                                                                 | `2`                                                                                        |
| allow_compute_area     | number  | Specifies whether area computation is allowed                                                                                              | `true`                                                                                     |

##### **ors.services.isochrones.fastisochrones**

| key                    | type | description                                                                                                                                | example value                                                                              |
|------------------------|------|--------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------| 
| maximum_range_distance | list | Possible values for maximum_range_distance and maximum_range_time are an integer or a list of values specifically defined for each profile | `[{profiles: "any", value: 50000}, {profiles: "driving-car, driving-hgv", value: 100000}]` |
| maximum_range_time     | list |                                                                                                                                            | `[{profiles: "any", value: 18000},{profiles: "driving-car, driving-hgv", value: 3600}]`    |

##### **ors.services.isochrones.fastisochrones.profiles.default_params**

| key          | type    | description                                              | example value            |
|--------------|---------|----------------------------------------------------------|--------------------------|
| enabled      | boolean |                                                          | `true`                   |
| threads      | number  |                                                          | `1`                      |
| weightings   | string  |                                                          | `"recommended,shortest"` |
| maxcellnodes | number  | Maximum number of nodes allowed in single isochrone cell | `5000`                   |

---

#### ors.services.matrix

| key                     | type    | description                                                                                                                 | example value                                        |
|-------------------------|---------|-----------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------| 
| enabled                 | boolean | Enables or disables (true/false) the end-point (default: true)                                                              | `true`                                               |
| maximum_routes          | number  | Maximum amount of routes the matrix should compute. E.g. `2500` could be a 50x50 matrix or 1x2500 matrix                    | `2500`                                               |
| maximum_routes_flexible | number  | Maximum amount of routes for using custom profiles that do not support contraction hierarchies                              | `25`                                                 |
| maximum_search_radius   | number  | Maximum allowed distance between the requested coordinate and a point on the nearest road. The value is measured in meters  | `5000`                                               |
| maximum_visited_nodes   | number  | Maximum allowed number of visited nodes in shortest path computation. This threshold is applied only for Dijkstra algorithm | `100000`                                             |
| allow_resolve_locations | number  | Specifies whether the name of a nearest street to the location can be resolved or not. Default value is true                | `true`                                               |
| attribution             | string  | Attribution added to the response metadata                                                                                  | `"openrouteservice.org, OpenStreetMap contributors"` |

---
#### ors.services.snap

| key                     | type    | description                                                    | example value                                        |
|-------------------------|---------|----------------------------------------------------------------|------------------------------------------------------| 
| enabled                 | boolean | Enables or disables (true/false) the end-point (default: true) | `true`                                               |
| attribution             | string  | Attribution added to the response metadata                     | `"openrouteservice.org, OpenStreetMap contributors"` |

---

#### ors.logging

| key        | type    | description                                                     | example value          |
|------------|---------|-----------------------------------------------------------------|------------------------| 
| enabled    | boolean | Enables or disables the end-point (default: true)               | `true`                 |
| level_file | string  | Can be either `DEBUG_LOGGING.json` or `PRODUCTION_LOGGING.json` | `"DEBUG_LOGGING.json"` |
| location   | string  | Location of the logs                                            | `"/var/log/ors"`       |
| stdout     | boolean |                                                                 | `true`                 |

---

#### ors.system_message

Array of message objects where each has

| key       | type    | description                                                      | example value        |
|-----------|---------|------------------------------------------------------------------|----------------------| 
| active    | boolean | Enables or disables this message                                 | `true`               |
| text      | string  | The message text                                                 | `"The message text"` |
| condition | object  | optional; may contain any of the conditions from the table below |                      |

| condition          | value                                                                                                                | description                                                         |
|--------------------|----------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| time_before        | ISO 8601 datetime string                                                                                             | message sent if server local time is before given point in time     |
| time_after         | ISO 8601 datetime string                                                                                             | message sent if server local time is after given point in time      |
| api_version        | 1 or 2                                                                                                               | message sent if API version requested through matches value         |
| api_format         | String with output formats ("json", "geojson", "gpx"), comma separated                                               | message sent if requested output format matches value               |
| request_service    | String with service names ("routing", "matrix", "isochrones"), comma separated                                       | message sent if requested service matches one of the given names    |
| request_profile    | String with profile names, comma separated                                                                           | message sent if requested profile matches one of the given names    |
| request_preference | String with preference (weightings for routing, metrics for matrix, rangetype for isochrones) names, comma separated | message sent if requested preference matches one of the given names |

##### Example:

```
system_message: [
    {
        active: true,
        text: "This message would be sent with every routing bike fastest request. E.g. 'The fastest weighting for cycling profiles is deprecated, use recommended weighting instead. API will be kept for compatibility until release of version 7.0.0'",
        condition: {
            "request_service": "routing",
            "request_profile": "cycling-regular,cycling-mountain,cycling-road,cycling-electric",
            "request_preference": "fastest"
        }
    },
    {
        active: true,
        text: "This message would be sent with every request for geojson response.",
        condition: {
            "api_format": "geojson"
        }
    },
    {
        active: true,
        text: "This message would be sent with every request on API v1 from January 2020 until June 2050. E.g. 'The V1 API is deprecated. You should switch to using the V2 API.'",
        condition: {
            "api_version": "1",
            "time_after": "2020-01-01T00:00:00Z",
            "time_before": "2050-06-01T00:00:00Z"
        }
    },
    {
        active: true,
        text: "This message would be sent with every request. E.g. 'Scheduled downtime due to version upgrade on March 15th 2020, 12:00 AM CET for approx. 3 hours.'"
    }
]
```
