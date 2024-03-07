
# `ors.engine.profiles`

The profiles object contains key-object-pairs for each profile you are using.

Available profiles are:
- `car`
- `hgv`
- `bike-regular`
- `bike-mountain`
- `bike-road`
- `bike-electric`
- `walking`
- `hiking`
- `wheelchair`
- `public-transport`

Properties for each (enabled) profile are set under `ors.engine.profiles.<profile>`, e.g.
- `ors.engine.profiles.car`
- `ors.engine.profiles.hiking`

| key                                 | type    | description                                                                                                                                                                                                                                                                                                                                                       | default value |
|-------------------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| profile                             | string  | Profile name                                                                                                                                                                                                                                                                                                                                                      | _NA_          |
| enabled                             | boolean | Enables or disables the profile across **openrouteservice** endpoints                                                                                                                                                                                                                                                                                             | `true`        |
| elevation                           | boolean | Specifies whether to use or not elevation data                                                                                                                                                                                                                                                                                                                    | `false`       |
| elevation_smoothing                 | boolean | Smooth out elevation data                                                                                                                                                                                                                                                                                                                                         | `false`       |
| traffic                             | boolean | Use traffic data if available                                                                                                                                                                                                                                                                                                                                     | `false`       |
| interpolate_bridges_and_tunnels     | boolean | Toggle elevation interpolation of bridges and tunnels on and off                                                                                                                                                                                                                                                                                                  | `true`        |
| instructions                        | boolean | Specifies whether way names will be stored during the import or not                                                                                                                                                                                                                                                                                               | `true`        |
| optimize                            | boolean | Optimize the sort order when contracting nodes for CH. This is rather expensive, but yields a better contraction hierarchy.                                                                                                                                                                                                                                       | `false`       |
| graph_path                          | string  | Subdirectory name under `ors.engine.graphs_root_path`. If left unset, the profile entry name on the `profiles` list is used                                                                                                                                                                                                                                       | _NA_          |
| encoder_options                     | string  | For details see [encoder_options](#encoder-options) below                                                                                                                                                                                                                                                                                                         |               |
| preparation                         | object  | [Preparation settings](#preparation) for building the routing graphs                                                                                                                                                                                                                                                                                              |               |
| execution                           | object  | [Execution settings](#execution) relevant when querying services                                                                                                                                                                                                                                                                                                  |               |
| ext_storages                        | object  | [External storages](#ext_storages) for returning extra information                                                                                                                                                                                                                                                                                                |               |
| maximum_distance                    | number  | The maximum allowed total distance of a route                                                                                                                                                                                                                                                                                                                     | `100000`      |
| maximum_distance_dynamic_weights    | number  | The maximum allowed distance between two way points when dynamic weights are used                                                                                                                                                                                                                                                                                 | `100000`      |
| maximum_distance_avoid_areas        | number  | The maximum allowed distance between two way points when areas to be avoided are provided                                                                                                                                                                                                                                                                         | `100000`      |
| maximum_distance_alternative_routes | number  | The maximum allowed total distance of a route for the alternative routes algorithm                                                                                                                                                                                                                                                                                | `100000`      |
| maximum_distance_round_trip_routes  | number  | The maximum allowed total distance of a route for the round trip algorithm                                                                                                                                                                                                                                                                                        | `100000`      |
| maximum_speed_lower_bound           | number  | Specifies the threshold for the query parameter `maximum_speed`                                                                                                                                                                                                                                                                                                   | `80`          |
| maximum_way_points                  | number  | The maximum number of way points in a request                                                                                                                                                                                                                                                                                                                     | `50`          |
| maximum_snapping_radius             | number  | Maximum distance around a given coordinate to find connectable edges                                                                                                                                                                                                                                                                                              | `400`         |
| maximum_visited_nodes               | number  | Maximum allowed number of visited nodes in shortest path computation                                                                                                                                                                                                                                                                                              | `1000000`     |
| encoder_flags_size                  | number  | The number of bytes used for FlagEncoders                                                                                                                                                                                                                                                                                                                         | `8`           |
| location_index_resolution           | number  | The minimum resolution in meters of tiles in the location index. Lower values yield faster queries at a cost of increased memory requirements. Reducing the resolution reduces the lookup radius which can be compensated by increasing `location_index_search_iterations`. Corresponds to GraphHopper's `index.high_resolution` configuration parameter.         | `500`         |
| location_index_search_iterations    | number  | The maximum number of iterations performed in coordinates lookup. Higher values yield a broader search area, but might reduce query performance. It only affects the storage lookup but not its layout so changing this parameter does not require rebuilding the location index. Corresponds to GraphHopper's `index.max_region_search` configuration parameter. | `4`           |
| force_turn_costs                    | boolean | Should turn restrictions be obeyed                                                                                                                                                                                                                                                                                                                                | `false`       |
| gtfs_file                           | string  | Only for `pt` profile: location of GTFS data; can either be a zip-file or the unzipped folder                                                                                                                                                                                                                                                                     | _NA_          |

## encoder_options

Properties beneath `ors.engine.profiles.*.encoder_options`:

| key                      | type    | description                                                                                                                                                                      | example value                  |
|--------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|
| turn_costs               | boolean | Should turn restrictions be respected.                                                                                                                                           | `turn_costs=true`              |
| problematic_speed_factor | number  | For wheelchair profile only! Travel speeds on edges classified as problematic for wheelchair users are multiplied by this factor, use to set slow traveling speeds on such ways  | `problematic_speed_factor=0.7` |
| preferred_speed_factor   | number  | For wheelchair profile only! Travel speeds on edges classified as preferable for wheelchair users are multiplied by this factor, use to set faster traveling speeds on such ways | `preferred_speed_factor=1.2`   |

## preparation

Properties beneath `ors.engine.profiles.*.preparation`:

| key                      | type   | description                                                       | default value |
|--------------------------|--------|-------------------------------------------------------------------|---------------|
| min_network_size         | number | Minimum size (number of edges) of an independent routing subgraph | `200`         |
| methods                  | object | see below                                                         |               |

### methods.ch

Settings for preprocessing contraction hierarchies

Properties beneath `ors.engine.profiles.*.preparation.methods.ch`:

| key        | type    | description                                              | example value          |
|------------|---------|----------------------------------------------------------|------------------------|
| enabled    | boolean | Enables or disables the routing algorithm                | `true`                 |
| threads    | number  | Number of parallel threads for computing the preparation | `1`                    |
| weightings | string  | Comma-separated list of weightings                       | `recommended,shortest` |

### methods.lm
Settings for preprocessing A* with landmarks

Properties beneath `ors.engine.profiles.*.preparation.methods.lm`:

| key        | type    | description                                                                                                               | default value          |
|------------|---------|---------------------------------------------------------------------------------------------------------------------------|------------------------|
| enabled    | boolean | Enables or disables the routing algorithm                                                                                 | `true`                 |
| threads    | number  | Number of parallel threads for computing the preparation                                                                  | `1`                    |
| weightings | string  | Comma-separated list of weightings                                                                                        | `recommended,shortest` |
| landmarks  | number  | Total number of precomputed landmarks; the subset used during the query is set in `execution.methods.lm.active_landmarks` | `16`                   |

### methods.core
Settings for preprocessing core routing with landmarks

Properties beneath `ors.engine.profiles.*.preparation.methods.core`:

| key        | type    | description                                                                                                                 | example value                                               |
|------------|---------|-----------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| enabled    | boolean | Enables or disables the routing algorithm                                                                                   | `true`                                                      |
| threads    | number  | Number of parallel threads for computing the preparation                                                                    | `1`                                                         |
| weightings | string  | Comma-separated list of weightings                                                                                          | `recommended,shortest`                                      |
| landmarks  | number  | Total number of precomputed landmarks, the subset used during the query is set in `execution.methods.core.active_landmarks` | `32`                                                        |
| lmsets     | string  | Landmark sets tailored for specific avoid-filters enabled                                                                   | `highways,tollways;highways;tollways;country_193;allow_all` |

## execution

Properties beneath `ors.engine.profiles.*.execution` are relevant when querying services.

### methods.lm

Settings for using landmarks in routing.

Properties beneath `ors.engine.profiles.*.execution.methods.lm`:

| key               | type    | description                                      | default value |
|-------------------|---------|--------------------------------------------------|---------------| 
| active_landmarks  | number  | Number of landmarks used for computing the route | `8`           |

### methods.core

Settings for using landmarks in routing.

Properties beneath `ors.engine.profiles.*.execution.methods.core`:

| key               | type    | description                                      | example value |
|-------------------|---------|--------------------------------------------------|---------------| 
| active_landmarks  | number  | Number of landmarks used for computing the route | `6`           |

## ext_storages

For each profile it can be defined which external storages for extra info should be included in the graph.
This makes those information available as `extra_info` in a routing response.

To do so, add a key from the list below.
Leave its value empty, unless you want to specify further options (currently only available for
`RoadAccessRestrictions`, `Borders` and `Wheelchair`).

Properties beneath `ors.engine.profiles.*.ext_storages`:

| key                    | type   | description                                                                                                                  | example value                                     |
|------------------------|--------|------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|
| WayCategory            | object | Returns the way category in the route response, Compatible for any profile type                                              |                                                   |
| WaySurfaceType         | object | Returns the way surface in the route response, Compatible for any profile type                                               |                                                   |
| HillIndex              | object | Returns the ascent/descent in the route response, Compatible for any profile type                                            |                                                   |
| TrailDifficulty        | object | Returns the trail difficulty in the route response, Compatible for profile-hiking                                            |                                                   |
| RoadAccessRestrictions | object | RoadAccessRestrictions are where roads are restricted to certain vehicles to certain circumstances, e.g. access=destination. | [RoadAccessRestrictions](#roadaccessrestrictions) |
| Wheelchair             | object | Compatible for wheelchair                                                                                                    | [Wheelchair](#wheelchair)                         |
| OsmId                  | object | Returns the OsmId of the way, Compatible for wheelchair                                                                      |                                                   |
| Borders                | object | Borders allows the restriction of routes to not cross country borders, compatible for any profile type                       | [Borders](#borders)                               |


Have a look at [this table](/api-reference/endpoints/directions/extra-info/index.md#extra-info-availability) to check which external storages are enabled for the which profile by default.


### RoadAccessRestrictions
The `use_for_warnings` parameter tells the ors that this storage can be used for generating warning messages in the route response. 

| key              | type    | description                                                                                                                                                                                | example value |
|------------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| use_for_warnings | boolean | Whenever a route goes over a way which has some restrictions, a warning message will be delivered with the response and the roadaccessrestrictions extra info will be added automatically. | `true`        |

### Wheelchair

| key              | type    | description                              | example value |
|------------------|---------|------------------------------------------|---------------|
| KerbsOnCrossings | boolean | Kerb height is only parsed on crossings. | `true`        |

### Borders

Properties beneath `ors.engine.profiles.*.ext_storages.Borders` allows to define restriction of routes to not cross country borders, compatible for any profile type.:

| key         | type   | description                                                                                         | example value            |
|-------------|--------|-----------------------------------------------------------------------------------------------------|--------------------------|
| boundaries  | string | The path to a file containing geojson data representing the borders of countries                    | `borders.geojson.tar.gz` |
| ids         | string | Path to a csv file containing a unique id for each country, its local name and its English name     | `ids.csv`                |
| openborders | string | Path to a csv file containing pairs of countries where the borders are open (i.e. Schengen borders) | `openborders.csv`        |
