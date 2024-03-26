
# `ors.engine`

Engine properties are required at graph-build time during startup.

::: warning
If you specified `profile_default` settings they might not be taken into account!
This will be fixed in the next patch release.
As a workaround, you can move all `profile_default` settings to the specific profile where you need them to work.
:::

| key                | type    | description                                                                                                                                                                                                                                                                                          | default value                              |
|--------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|
| init_threads       | number  | The number of threads used to initialize (build/load) graphs. Higher numbers requires more RAM                                                                                                                                                                                                       | `1`                                        |
| preparation_mode   | boolean | If set, graphs and preparations will be build, but the application will shut down immediately afterwards without starting up any services                                                                                                                                                            | `false`                                    |
| source_file        | string  | The OSM file to be used, supported formats are `.osm`, `.osm.gz`, `.osm.zip` and `.pbf`                                                                                                                                                                                                              | `ors-api/src/test/files/heidelberg.osm.gz` |
| graphs_root_path   | string  | The root path to a directory for storing graphs                                                                                                                                                                                                                                                      | `./graphs`                                 |
| graphs_data_access | string  | Defines how a DataAccess object is created. <br> - `MMAP`: memory mapped storage <br> - `RAM_STORE`: in-memory storage with a safe/flush option.<br> Further info in the [source code](https://github.com/GIScience/graphhopper/blob/ors_4.0/core/src/main/java/com/graphhopper/storage/DAType.java) | `RAM_STORE`                                |
| elevation          | object  | See [elevation properties](elevation.md)                                                                                                                                                                                                                                                             |                                            |
| profile_default    | object  | Described in [profile properties](profiles.md)                                                                                                                                                                                                                                                       |                                            |
| profiles           | object  | Described in [profile properties](profiles.md)                                                                                                                                                                                                                                                       |                                            |
