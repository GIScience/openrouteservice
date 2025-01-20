# `ors.engine.profiles.<PROFILE-NAME>.service`

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.service` represent parameters relevant when querying services
that
need to be set specifically for each profile. More parameters relevant at query time can be found in the [
`ors.endpoints`](/api-reference/endpoints/index.md) section.

| key                                 | type    | description                                                                                               | default value |
|-------------------------------------|---------|-----------------------------------------------------------------------------------------------------------|---------------|
| maximum_distance                    | number  | The maximum allowed total distance of a route                                                             | `100000`      |
| maximum_distance_dynamic_weights    | number  | The maximum allowed distance between two way points when dynamic weights are used                         | `100000`      |
| maximum_distance_avoid_areas        | number  | The maximum allowed distance between two way points when areas to be avoided are provided                 | `100000`      |
| maximum_distance_alternative_routes | number  | The maximum allowed total distance of a route for the alternative routes algorithm                        | `100000`      |
| maximum_distance_round_trip_routes  | number  | The maximum allowed total distance of a route for the round trip algorithm                                | `100000`      |
| maximum_way_points                  | number  | The maximum number of way points in a request                                                             | `50`          |
| maximum_snapping_radius             | number  | Maximum distance around a given coordinate to find connectable edges                                      | `400`         |
| maximum_visited_nodes               | number  | Only for `public-transport` profile: maximum allowed number of visited nodes in shortest path computation | `1000000`     |
| force_turn_costs                    | boolean | Should turn restrictions be obeyed                                                                        | `false`       |
| execution                           | object  | [Execution settings](#execution) relevant when querying services                                          |               |

## `execution`

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.service.execution` represent options specific for certain
algorithms.

### `methods.astar`

Parameters for beeline approximation in the A* routing algorithm.

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.service.execution.methods.astar`:

| key           | type   | description                                                                                                                                 | default value           |
|---------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------|-------------------------| 
| approximation | string | Method to use for distance approximation. Can be either the faster `BeelineSimplification` or the more precise but slower `BeelineAccurate` | `BeelineSimplification` |
| epsilon       | number | Factor to use for distance approximation                                                                                                    | `1`                     |

### `methods.lm`

Settings for using landmarks in routing.

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.service.execution.methods.lm`:

| key              | type   | description                                      | default value |
|------------------|--------|--------------------------------------------------|---------------| 
| active_landmarks | number | Number of landmarks used for computing the route | `8`           |

### `methods.core`

Settings for using landmarks in routing using the Core-ALT algorithm.

Properties beneath `ors.engine.profiles.<PROFILE-NAME>.service.execution.methods.core`:

| key              | type   | description                                      | example value |
|------------------|--------|--------------------------------------------------|---------------| 
| active_landmarks | number | Number of landmarks used for computing the route | `6`           |
