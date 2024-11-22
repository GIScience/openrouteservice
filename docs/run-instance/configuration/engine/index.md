# `ors.engine`

Engine properties are required at graph-build time during startup.

openrouteservice can simultaneously run a number of different profiles, each with its own set of parameters. The
profiles are defined in the `profiles` section of the configuration file. The `profile_default` section is used to
define default values for all profiles. The `profiles` section is used to define specific values for individual
profiles. See [profiles](/run-instance/configuration/engine/profiles/index.md) for more information.

| key                | type    | description                                                                                                                                                                                                                                                                                          | default value |
|--------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| init_threads       | number  | The number of threads used to initialize (build/load) graphs. Higher numbers requires more RAM                                                                                                                                                                                                       | `1`           |
| preparation_mode   | boolean | If set, graphs and preparations will be built, but the application will shut down immediately afterwards without starting up any services                                                                                                                                                            | `false`       |
| config_output      | string  | If set, the default configuration values are written to a file named as the value. The application will shut down immediately afterwards without starting up any services. See [configuration](/run-instance/configuration/how-to-configure.md#configuration-defaults-output) page for details.      |               |
| graphs_data_access | string  | Defines how a DataAccess object is created. <br> - `MMAP`: memory mapped storage <br> - `RAM_STORE`: in-memory storage with a safe/flush option.<br> Further info in the [source code](https://github.com/GIScience/graphhopper/blob/ors_4.0/core/src/main/java/com/graphhopper/storage/DAType.java) | `RAM_STORE`   |
| elevation          | object  | See [elevation properties](elevation.md)                                                                                                                                                                                                                                                             |               |
| graph_management   | object  | See [graph management properties](graph-management.md)                                                                                                                                                                                                                                               |               |
| profile_default    | object  | Described in [profiles](/run-instance/configuration/engine/profiles/index.md)                                                                                                                                                                                                                        |               |
| profiles           | object  | Described in [profiles](/run-instance/configuration/engine/profiles/index.md)                                                                                                                                                                                                                        |               |

