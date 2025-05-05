# `ors.engine.profiles.<PROFILE-NAME>.repo`

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.repo` are used to address a graph in a graph repository.
If *all* of these properties are set,
and if graph management is
enabled [ors.engine.graph_management.enabled](/run-instance/configuration/engine/graph-management.md),
openrouteservice will use the specified repository to load the graph data.

| key                      | type   | description                                                                                                                                                | example values                                                                                        |
|--------------------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| repository_uri           | string | The base URL (for a http or minio repository) or path (for a file system repository)                                                                       | * `http://some.domain.ors`<br/>* `file:///absolute/path`<br/>* `/absolute/path`<br/>* `relative/path` |
| repository_name          | string | An editorial name representing the target group or vendor for the graphs.                                                                                  | `public`, `my-organization`                                                                           |
| repository_profile_group | string | A group of profiles with specific characteristics lke the usage of traffic data or the pre-calculation of advanced data structures e.g. for fastisochrones | `traffic`, `fastiso`                                                                                  |             
| graph_extent             | string | The geographic region covered by the graph. This corresponds to the OSM PBF file used for graph building.                                                  | `planet`                                                                                              |
| repository_user          | string | The user name (key_id) for authentication at the repository. Currently only used by the minio client.                                                      | `username`                                                                                            |
| repository_password      | string | The password (access_key) for authentication at the repository. Currently only used by the minio client.                                                   | `password`                                                                                            |

Each routing profile can have its individual repository parameters,
which makes it possible to have routing profiles from different repositories
or in different geographic regions.

Repository properties that are common for all routing profiles can be configured
in [ors.engine.profile_default](/run-instance/configuration/engine/index.md)

See [graph repo client](/technical-details/graph-repo-client/) for more information.
