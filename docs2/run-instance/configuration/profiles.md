# Profile properties

Profile settings comprise the following properties.

| key                                 | type    | description                                                          | default value |
|-------------------------------------|---------|----------------------------------------------------------------------|---------------|
| profile                             | string  |                                                                      |               |
| enabled                             | boolean |                                                                      |               |
| elevation                           | boolean |                                                                      |               |
| elevation_smoothing                 | boolean |                                                                      |               |
| traffic                             | boolean |                                                                      |               |
| interpolate_bridges_and_tunnels     | boolean |                                                                      |               |
| instructions                        | boolean |                                                                      |               |
| optimize                            | boolean |                                                                      |               |
| graph_path                          | string  |                                                                      |               |
| encoder_options                     | string  | For details see [encoder settings](#encodersettings) below.          |               |
| preparation                         | object  | [Preparation settings](preparation) for building the routing graphs. |               |
| execution                           | object  | [Execution settings](execution) relevant when querying services.     |               |
| ext_storages                        | object  | [External storages](storages).                                       |               |
| maximum_distance                    | number  |                                                                      |               |
| maximum_distance_dynamic_weights    | number  |                                                                      |               |
| maximum_distance_avoid_areas        | number  |                                                                      |               |
| maximum_distance_alternative_routes | number  |                                                                      |               |
| maximum_distance_round_trip_routes  | number  |                                                                      |               |
| maximum_speed_lower_bound           | number  |                                                                      |               |
| maximum_way_points                  | number  |                                                                      |               |
| maximum_snapping_radius             | number  |                                                                      |               |
| maximum_visited_nodes               | number  |                                                                      |               |
| encoder_flags_size                  | number  |                                                                      |               |
| location_index_resolution           | number  |                                                                      |               |
| location_index_search_iterations    | number  |                                                                      |               |
| force_turn_costs                    | boolean |                                                                      |               |
| gtfs_file                           | string  |                                                                      |               |

## Encoder settings

A string containing a list of encoder parameters encoded as pipe `|` separated `key = value` pairs.

| key                      | type    | description                                                                                                                                                                       | example value                  |
|--------------------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|
| turn_costs               | boolean | Should turn restrictions be respected.                                                                                                                                            | `turn_costs=true`              |
| problematic_speed_factor | number  | For wheelchair profile only! Travel speeds on edges classified as problematic for wheelchair users are multiplied by this factor, use to set slow traveling speeds on such ways   | `problematic_speed_factor=0.7` |
| preferred_speed_factor   | number  | For wheelchair profile only! Travel speeds on edges classified as preferrable for wheelchair users are multiplied by this factor, use to set faster traveling speeds on such ways | `preferred_speed_factor=1.2`   |
