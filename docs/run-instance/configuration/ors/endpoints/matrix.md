
# `ors.endpoints.matrix`

Settings for the matrix endpoint.

| key                     | type    | description                                                                                                                      | default value                                      |
|-------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| enabled                 | boolean | Enables or disables the end-point                                                                                                | `true`                                             |
| attribution             | string  | Attribution added to the response metadata                                                                                       | `openrouteservice.org, OpenStreetMap contributors` |
| maximum_routes          | number  | Maximum amount of routes the matrix should compute; e.g. the value of `2500` could correspond to a 50x50 matrix or 1x2500 matrix | `2500`                                             |
| maximum_routes_flexible | number  | Maximum amount of routes for profiles that do not have the CH routing algorithm enabled                                          | `25`                                               |
| maximum_visited_nodes   | number  | Maximum allowed number of visited nodes in shortest path computation                                                             | `100000`                                           |
| maximum_search_radius   | number  | Maximum allowed distance in meters between the requested coordinate and a point on the nearest road                              | `2000`                                             |
| u_turn_cost             | number  | Penalty of performing a U-Turn; the value of `-1` prevents them entirely                                                         | `-1`                                               |
