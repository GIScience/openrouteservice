# `ors.engine.profiles.<PROFILE-NAME>.build`

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.build` are used to define the parameters for building the routing
graphs for the specified profile.

| key                              | type    | description                                                                                                                                                                                                                                                                                                                                                       | default value                                |
|----------------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------|
| source_file                      | string  | The OSM file to be used, supported formats are `.osm`, `.osm.gz`, `.osm.zip` and `.pbf`                                                                                                                                                                                                                                                                           | `ors-api/src/test/files/heidelberg.test.pbf` |
| elevation                        | boolean | Specifies whether to download and use elevation data. If true, `cache_path` and `provider` must be set in ors.engine.elevation as well.                                                                                                                                                                                                                           | `false`                                      |
| elevation_smoothing              | boolean | Smooth out elevation data                                                                                                                                                                                                                                                                                                                                         | `false`                                      |
| traffic                          | boolean | Use traffic data if available                                                                                                                                                                                                                                                                                                                                     | `false`                                      |
| interpolate_bridges_and_tunnels  | boolean | Toggle elevation interpolation of bridges and tunnels on and off                                                                                                                                                                                                                                                                                                  | `true`                                       |
| instructions                     | boolean | Specifies whether way names will be stored during the import or not                                                                                                                                                                                                                                                                                               | `true`                                       |
| optimize                         | boolean | Optimize the sort order when contracting nodes for CH. This is rather expensive, but yields a better contraction hierarchy.                                                                                                                                                                                                                                       | `false`                                      |
| maximum_speed_lower_bound        | number  | Specifies the threshold for the query parameter `maximum_speed`, required when calculating preparation data for the Core-ALT algorithm                                                                                                                                                                                                                            | `80`                                         |
| encoder_flags_size               | number  | The number of bytes used for FlagEncoders                                                                                                                                                                                                                                                                                                                         | `8`                                          |
| location_index_resolution        | number  | The minimum resolution in meters of tiles in the location index. Lower values yield faster queries at a cost of increased memory requirements. Reducing the resolution reduces the lookup radius which can be compensated by increasing `location_index_search_iterations`. Corresponds to GraphHopper's `index.high_resolution` configuration parameter.         | `500`                                        |
| location_index_search_iterations | number  | The maximum number of iterations performed in coordinates lookup. Higher values yield a broader search area, but might reduce query performance. It only affects the storage lookup but not its layout so changing this parameter does not require rebuilding the location index. Corresponds to GraphHopper's `index.max_region_search` configuration parameter. | `4`                                          |
| gtfs_file                        | string  | Only for `public-transport` profile: location of GTFS data; can either be a zip-file or the unzipped folder                                                                                                                                                                                                                                                       | _NA_                                         |
| encoder_options                  | string  | For details see [encoder_options](#encoder-options) below                                                                                                                                                                                                                                                                                                         |                                              |
| preparation                      | object  | [Preparation settings](#preparation) for building the routing graphs                                                                                                                                                                                                                                                                                              |                                              |
| ext_storages                     | object  | [External storages](#ext-storages) for returning extra information                                                                                                                                                                                                                                                                                                |                                              |

## `encoder_options`

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.build.encoder_options`:

| key                      | type    | profiles         | description                                                                                                                                                                                                                                                                                                                                                                                                      | example value |
|--------------------------|---------|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| block_fords              | boolean | *                | Do not route through fords                                                                                                                                                                                                                                                                                                                                                                                       | `true`        |
| consider_elevation       | boolean | bike-*           | The maximum possible speed is the bike-type specific maximum downhill speed, which is higher than the usual maximum speed                                                                                                                                                                                                                                                                                        | `true`        |
| maximum_grade_level      | number  | car, hgv         | Relates to the quality of tracks as described in [tracktype](https://wiki.openstreetmap.org/wiki/Key:tracktype). Specifying e.g. `maximum_grade_level=1` means that `tracktype=grade2` and below won't be considered for routing. Setting `maximum_grade_level=0` discards all tracks with a valid `tracktype` tag, while a negative value such as `maximum_grade_level=-1` entirely disables routing on tracks. | `3`           |
| preferred_speed_factor   | number  | wheelchair       | Travel speeds on edges classified as preferable for wheelchair users are multiplied by this factor, use to set faster traveling speeds on such ways                                                                                                                                                                                                                                                              | `1.2`         |
| problematic_speed_factor | number  | wheelchair       | Travel speeds on edges classified as problematic for wheelchair users are multiplied by this factor, use to set slow traveling speeds on such ways                                                                                                                                                                                                                                                               | `0.7`         |
| turn_costs               | boolean | car, hgv, bike-* | Should turn restrictions be respected                                                                                                                                                                                                                                                                                                                                                                            | `true`        |
| use_acceleration         | boolean | car, hgv         | Models how a vehicle would accelerate on the road segment to the maximum allowed speed. In practice it reduces speed on shorter road segments such as ones between nearby intersections in a city                                                                                                                                                                                                                | `true`        |
| enable_custom_models     | boolean | *                | Enables whether the profile is prepared to support custom models. Also see the corresponding parameter `allow_custom_models` in the [service properties](service.md).                                                                                                                                                                                                                                             | `false`       |

## `preparation`

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.build.preparation`:

| key              | type   | description                                                       | default value |
|------------------|--------|-------------------------------------------------------------------|---------------|
| min_network_size | number | Minimum size (number of edges) of an independent routing subgraph | `200`         |
| methods          | object | see below                                                         |               |

### `methods.ch`

Settings for preprocessing contraction hierarchies

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.build.preparation.methods.ch`:

| key        | type    | description                                              | example value          |
|------------|---------|----------------------------------------------------------|------------------------|
| enabled    | boolean | Enables or disables the routing algorithm                | `true`                 |
| threads    | number  | Number of parallel threads for computing the preparation | `1`                    |
| weightings | string  | Comma-separated list of weightings                       | `recommended,shortest` |

### `methods.lm`

Settings for preprocessing A* with landmarks

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.build.preparation.methods.lm`:

| key        | type    | description                                                                                                               | default value          |
|------------|---------|---------------------------------------------------------------------------------------------------------------------------|------------------------|
| enabled    | boolean | Enables or disables the routing algorithm                                                                                 | `true`                 |
| threads    | number  | Number of parallel threads for computing the preparation                                                                  | `1`                    |
| weightings | string  | Comma-separated list of weightings                                                                                        | `recommended,shortest` |
| landmarks  | number  | Total number of precomputed landmarks; the subset used during the query is set in `execution.methods.lm.active_landmarks` | `16`                   |

### `methods.core`

Settings for preprocessing core routing with landmarks

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.build.preparation.methods.core`:

| key        | type    | description                                                                                                                 | example value                                               |
|------------|---------|-----------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| enabled    | boolean | Enables or disables the routing algorithm                                                                                   | `true`                                                      |
| threads    | number  | Number of parallel threads for computing the preparation                                                                    | `1`                                                         |
| weightings | string  | Comma-separated list of weightings                                                                                          | `recommended,shortest`                                      |
| landmarks  | number  | Total number of precomputed landmarks, the subset used during the query is set in `execution.methods.core.active_landmarks` | `32`                                                        |
| lmsets     | string  | Landmark sets tailored for specific avoid-filters enabled                                                                   | `highways,tollways;highways;tollways;country_193;allow_all` |

## `ext_storages`

It can be defined for each profile which auxiliary metadata should be included in the graph.
This information is made available as `extra_info` in a routing response.

To do so, add a key from the list below.
Leave its value empty, unless you want to specify further options (currently only available for
[RoadAccessRestrictions](#roadaccessrestrictions), [Borders](#borders), [Wheelchair](#wheelchair)
and [HeavyVehicle](#heavyvehicle)).

::: warning
In addition to providing the information in query response, data from `WayCategory` and `Tollways` storages is being
used to filter out certain roads via the
[`options.avoid_features`](/api-reference/endpoints/directions/routing-options.md#options-avoid-features)
query parameter, and `Borders` is necessary for the functionality behind
[`options.avoid_borders`](/api-reference/endpoints/directions/routing-options.md#options-avoid-borders) and
[`options.avoid_countries`](/api-reference/endpoints/directions/routing-options.md#options-avoid-countries)
query parameters. Options from
[
`options.profile_params.restrictions`](/api-reference/endpoints/directions/routing-options.md#options-profile-params-restrictions)
require `HeavyVehicle` or `Wheelchair` storages being enabled. Furthermore, hgv profile-specific access restrictions
specified in
[`options.vehicle_type`](/api-reference/endpoints/directions/routing-options.md#options-vehicle-type)
parameter
rely on the `HeavyVehicle` storage.
:::

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.build.ext_storages`:

| key                    | type   | description                                                                                                                                      | example value                                     |
|------------------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|
| WayCategory            | object | Returns the way category in the route response, compatible with any profile type                                                                 |                                                   |
| WaySurfaceType         | object | Returns the way surface in the route response, compatible with any profile type                                                                  |                                                   |
| Tollways               | object | Returns way tolls in the route response, compatible with driving profiles                                                                        |                                                   |
| Borders                | object | Borders allows the restriction of routes to not cross country borders, compatible with any profile type                                          | [Borders](#borders)                               |
| RoadAccessRestrictions | object | Information on restriction of roads to certain vehicles or circumstances, e.g. `access=destination`                                              | [RoadAccessRestrictions](#roadaccessrestrictions) |
| HeavyVehicle           | object | Heavy vehicle-specific storage compatible only with that profile; it contains weight and size limits as well as vehicle-type access restrictions | [HeavyVehicle](#heavyvehicle)                     |
| HillIndex              | object | Returns the ascent/descent in the route response, compatible with any profile type                                                               |                                                   |
| TrailDifficulty        | object | Returns the trail difficulty in the route response, compatible with walking and cycling profiles                                                 |                                                   |
| Wheelchair             | object | Wheelchair-specific attributes compatible only with that profile                                                                                 | [Wheelchair](#wheelchair)                         |
| OsmId                  | object | Returns the OsmId of the way, compatible with any profile type                                                                                   |                                                   |

Check [this table](/api-reference/endpoints/directions/extra-info/index.md#extra-info-availability) for extra
info availability.
The following table summarizes which storages are enabled for which profile by default.

|             | WayCategory | WaySurfaceType | Tollways | Borders | RoadAccessRestrictions | HeavyVehicle | HillIndex | TrailDifficulty | Wheelchair | OsmId |
|:------------|:-----------:|:--------------:|:--------:|---------|:----------------------:|:------------:|:---------:|:---------------:|:----------:|:-----:|
| driving-car |      x      |       x        |    x     |         |           x            |              |           |                 |            |       |
| driving-hgv |      x      |       x        |    x     |         |                        |      x       |           |                 |            |       |
| cycling-*   |      x      |       x        |          |         |                        |              |     x     |        x        |            |       |
| foot-*      |      x      |       x        |          |         |                        |              |     x     |        x        |            |       |
| wheelchair  |      x      |       x        |          |         |                        |              |           |                 |     x      |   x   |

The `use_for_warnings` parameter tells the ors that this storage can be used for generating warning messages in the
route response.

| key              | type    | description                                                                                                                                                                                | example value |
|------------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| use_for_warnings | boolean | Whenever a route goes over a way which has some restrictions, a warning message will be delivered with the response and the roadaccessrestrictions extra info will be added automatically. | `true`        |

### `Wheelchair`

| key              | type    | description                              | example value |
|------------------|---------|------------------------------------------|---------------|
| KerbsOnCrossings | boolean | Kerb height is only parsed on crossings. | `true`        |

### `Borders`

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.build.ext_storages.Borders` allows to define restriction of
routes to not cross
country borders, compatible with any profile type.

| key          | type    | description                                                                                                                                                                                                                             | example value            |
|--------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------|
| boundaries   | string  | The path to a geojson file containing polygons representing country borders. Ignored if `preprocessed = true` is set.                                                                                                                   | `borders.geojson.tar.gz` |
| preprocessed | boolean | Indicates whether the source OSM file has been enriched with country data. If set to `true` then country codes are read from `country` node tags rather than being resolved at build time based on geometries provided in `boundaries`. | `true`                    |
| ids          | string  | Path to a csv file containing a unique id for each country, its local name and its English name                                                                                                                                         | `ids.csv`                |
| openborders  | string  | Path to a csv file containing pairs of countries where the borders are open (i.e. Schengen borders)                                                                                                                                     | `openborders.csv`        |

### `HeavyVehicle`

| key          | type    | description                                                                                                                                                                                                                                                    | example value |
|--------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| restrictions | boolean | Encode certain size and weight limits such as ones contained in `maxheight`, `maxlength`, `maxwidth`, `maxweight` and `maxaxleload` OSM way tags. Includes also access restrictions for vehicles carrying hazardous materials as provided by the `hazmat` tag. | `true`        |
