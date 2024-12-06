
# `ors.engine`

Engine properties are required at graph-build time during startup.

| key              | type    | description                                                                                                                               | default value                              |
|------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|
| init_threads     | number  | The number of threads used to initialize (build/load) graphs. Higher numbers requires more RAM                                            | `1`                                        |
| preparation_mode | boolean | If set, graphs and preparations will be build, but the application will shut down immediately afterwards without starting up any services | `false`                                    |
| source_file      | string  | The OSM file to be used, supported formats are `.osm`, `.osm.gz`, `.osm.zip` and `.pbf`                                                   | `ors-api/src/test/files/heidelberg.osm.gz` |
| graphs_root_path | string  | The root path to a directory for storing graphs                                                                                           | `./graphs`                                 |
| elevation        | object  | See [elevation properties](elevation.md)                                                                                                  |                                            |
| profile_default  | object  | Described in [profile properties](profiles.md)                                                                                            |                                            |
| profiles         | object  | Described in [profile properties](profiles.md)                                                                                            |                                            |
