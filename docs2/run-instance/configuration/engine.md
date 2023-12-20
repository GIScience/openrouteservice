# Engine properties 

The `engine` block consists of the following properties.

| key                         | type    | description                                                                                                                               | default value |
|-----------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| ors.engine.init_threads     | number  | The number of threads used to initialize (build/load) graphs. Higher numbers requires more RAM                                            | 1             |
| ors.engine.preparation_mode | boolean | If set, graphs and preparations will be build, but the application will shut down immediately afterwards without starting up any services | `false`       |
| ors.engine.source_file      | string  | The OSM file to be used, supported formats are `.osm`, `.osm.gz`, `.osm.zip` and `.pbf`                                                   |               |
| ors.engine.graphs_root_path | string  | The root path to a directory for storing graphs                                                                                           | `./graphs`    |
| ors.engine.elevation        | object  | See [elevation properties](#elevation-properties)                                                                                         |               |
| ors.engine.profile_default  | object  | Described in [profile properties](profiles)                                                                                               |               |
| ors.engine.profiles         | object  | Described in [profile properties](profiles)                                                                                               |               |

## Elevation properties

Elevation settings `ors.engine.elevation` comprise the following properties.

| key          | type    | description                                                                                                                                                                                                       | default value       |
|--------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|
| preprocessed | boolean | Toggles reading of OSM `ele` node tags on and off. If enabled, GH's elevation lookup is prevented and all nodes without `ele` tag will default to 0. Intended to be used in combination with the ORS preprocessor | `false`             |
| cache_clear  | boolean | Keep elevation data once it has been downloaded                                                                                                                                                                   | `false`             |
| provider     | string  | The name of an elevation provider. Possible values are `multi`, `cgiar` or `srtm`                                                                                                                                 | `multi`             |
| cache_path   | string  | The path to a directory in which SRTM tiles will be stored                                                                                                                                                        | `./elevation_cache` |
| data_access  | string  |                                                                                                                                                                                                                   | `MMAP`              |

