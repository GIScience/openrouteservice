# Custom Models

The request body parameter `custom_model` allows setting specific parameters influencing the edge weightings during
route calculation.

This parameter is available for directions requests only on profiles that have been created using the required encoder
option set at graph build time. **This feature is still in experimental state and is currently not available on our
public API for any profile**. You can use this feature on your [own openrouteservice instance](/run-instance/)
by [enabling it for the profile](/run-instance/configuration/engine/profiles/build.md#encoder-options) in the
`encoder_options`.

The `custom_model` parameter is a JSON object, the following example shows the structure within a request body for the
directions endpoint:

```json
{
    "preference": "custom",
    "coordinates": [
        [
            8.681495,
            49.41461
        ],
        [
            8.687872,
            49.420318
        ]
    ],
    "custom_model": {
        "speed": [
            {
                "if": true,
                "limit_to": 100
            }
        ],
        "priority": [
            {
                "if": "road_class == MOTORWAY",
                "multiply_by": 0
            }
        ],
        "distance_influence": 100
    }
}
```

## Available parameters

| Parameter            | Type   | Description                                                                                                                                                 |
|----------------------|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `distance_influence` | Number | Time saved in seconds to require for each kilometer of [detour accepted](#weighting-function), determining [distance influence factor](#weighting-function) |
| `speed`              | Array  | Set of [rules](#speed-and-priority-rules) determining [speed factor](#weighting-function)                                                                   |
| `priority`           | Array  | Set of [rules](#speed-and-priority-rules) determining [priority factor](#weighting-function)                                                                |
| `areas`              | Object | Map of [GeoJSON Features](#areas) used in area-based [rules](#speed-and-priority-rules)                                                                     |

## Basic principle

Custom models invoke a special weighting function for each edge in the graph and replaces the `shortest` or `fastest`
weightings normally used during calculations with a combined weighting function that takes into account the custom model
parameters.

The priority weightings of openrouteservice for foot and bike profiles (that represent preference of designated
footpaths or designated bicycle paths over car roads and so on) can be combined "on top" of the custom model weighting.
Therefore, possible values for the `preference` parameter of a route request with a custom model are `custom` and
`recommended`. If a request is made with the `shortest` or `fastest` preference AND a custom model value, the preference
value will be IGNORED.

## Weighting function

The weighting function for each edge is calculated as follows:

weight = distance / (speed * `speed factor` * `priority factor`) + distance * `distance influence factor`

where `speed factor`, `priority factor` and `distance influence factor` are values derived from the respective
parameters of the custom model, and distance and speed are values stored in the graph for each edge.

The `distance_influence` parameter is used to calculate the `distance influence factor` that influences the weight of
the edge based on the travel distance. If set to zero, the distance will not be taken into account in the weighting
function. If set to a value greater than zero, the distance will be multiplied by the derived factor before being added
to the weight. The result of this is that a ratio of preference between distance and speed can be set. The value
provided in the custom model is used such that it corresponds to the required time saved in seconds per kilometer of
detour. If you e.g. set `distance_influence` to 60, that means a faster route by travel time is only returned if it
saves at least a minute for every additional kilometer of distance travelled, therefore a detour to take the highway
instead of ordinary roads that adds, say, 2 kilometers, is only returned as "better" route if this saves at least 2
minutes of time.

The `speed factor` and the `priority factor` are multiplied with the edge speed. Both are calculated based on the rules
provided in the corresponding parameters `speed` and `priority` in the custom model, the difference being that the
`speed factor` also changes the travel time computation, while the `priority factor` is only used to influence the
weight of the edge during route computation and does not change the travel time. Both parameters expect an array of
objects, where each object is a rule consisting of a condition and an operation to be applied. These are described in
more detail in the next section.

## Speed and Priority Rules

The parameters `speed` and `priority` are arrays of rules that are applied to the edge speed and priority, respectively.
All rules within the arrays are applied in the order they are provided. Rules are defined as objects of the following
structure:

```json
{
    "operation_key": "operation_value",
    "condition_key": "condition_value"
}
```

### Operations

The `operation_key` is a string with two possible values. The `operation_value` is a number.

| `operation_key` | `operation_value`                                           |
|-----------------|-------------------------------------------------------------|
| `multiply_by`   | Factor to multiply with travel speed, e.g. `0`, `0.5`, etc. |
| `limit_to`      | Speed limit in km/h, e.g. `70`                              |

If the key is `multiply_by`, the value is interpreted as a factor that gets multiplied to the speed stored on the edge
for determining the weight of the edge.

If the rule is in the `speed` array, the factor is also applied to the speed value used in the travel time calculation,
so a value of `0.5` would mean to travel at half the normal speed on the affected edges.

If the rule is in the `priority` array, the factor is only applied to the weight of the edge, not to the time
calculation, and you can think of the number as a factor determining how favorable the affected edge is. A `0` would
mean to avoid the edge completely, a `1` would mean indeterminate; any number in between can denote degrees of
favorability, smaller numbers meaning less favorable.

If the key is `limit_to`, the value is interpreted as a maximum speed that can be travelled on the affected edges. Edges
with a higher speed will have their speeds adjusted, edges with a lower travel speed stored in the graph will not be
affected by this rule.

### Conditions

The `condition_key` is a string with the possible values `if`, `else_if` and `else`. `else_if` and `else` require a
preceding rule with `if` or `else_if` as condition key, and are applied only if the preceding rule's condition did NOT
match.

The `condition_value` can be a string or boolean value. If it is a string, it can either have the form
`in_[AREA_NAME]` (see [section below](#areas) for details) OR describe a logical statement that can evaluate as true or
false. The logical statement can be a simple comparison of a variable and a value, e.g. `road_class == MOTORWAY`, or a
more complex statement using Java boolean operators, e.g.
`max_speed <= 30 && (road_environment == TUNNEL || roundabout)`.

The variables that can be used in those statements are called `encoded values`, and different ones are available for
different profiles. The available `encoded values` can be found in the response to the [status endpoint](/api-reference/endpoints/status/), in the array
`profiles.<PROFILE NAME>.encoded_values`. Below is a list of example `encoded values` and their possible values that are
available. Note that this list is incomplete and the available variables depend on the profile in question.

| name               | Type      | Description                                                               |
|--------------------|-----------|---------------------------------------------------------------------------|
| `road_class`       | Enum      | MOTORWAY, TRUNK, PRIMARY, SECONDARY, TRACK, STEPS, CYCLEWAY, FOOTWAY, ... |
| `road_environment` | Enum      | ROAD, FERRY, BRIDGE, TUNNEL, ...                                          |
| `road_access`      | Enum      | DESTINATION, DELIVERY, PRIVATE, NO, ...                                   |
| `smoothness`       | Enum      | EXCELLENT, GOOD, INTERMEDIATE, ...                                        |
| `roundabout`       | Boolean   | Whether edge is part of a roundabout                                      |
| `get_off_bike`     | Boolean   | Whether edge is marked as "requiring to get off bike"                     |
| `max_speed`        | Numerical | Max speed in km/h                                                         |
| `bike_network`     | Enum      | MISSING, INTERNATIONAL, NATIONAL, REGIONAL, LOCAL, OTHER                  |
| `foot_network`     | Enum      | MISSING, INTERNATIONAL, NATIONAL, REGIONAL, LOCAL, OTHER                  |

Enum type encoded values can be used with the `==` and `!=` operators, while numerical encoded values can be used with
the
`==`, `!=`, `<`, `<=`, `>`, `>=` operators. Boolean encoded valued do not require an operator, but can be used with the
`==`, `!=` and `!` operators.

### Areas

Area-based rules are defined by using a `condition_value` in the form `in_[AREA_NAME]` in the `priority` ond/or `speed`
arrays and providing a corresponding named object in the `areas` object of the custom model.

Each area object is required to be a GeoJSON `Feature` object. The `bbox` member and other foreign members such as
`properties` are NOT supported. The `geometry` member of the feature object must be of `"type": "Polygon"`.

The following is an example of a custom model with an area-based rule that sets the priority to zero (avoid completely)
for all edges within the area defined by the polygon:

```json
{
    "priority": [
        {
            "if": "in_custom_area_1",
            "multiply_by": 0
        }
    ],
    "areas": {
        "custom_area_1": {
            "type": "Feature",
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            8.7062144,
                            49.4077481
                        ],
                        [
                            8.7068045,
                            49.4108196
                        ],
                        [
                            8.7132203,
                            49.4117201
                        ],
                        [
                            8.7139713,
                            49.4084322
                        ],
                        [
                            8.7062144,
                            49.4077481
                        ]
                    ]
                ]
            }
        }
    }
}
```

## Further Examples

Below are some examples of custom models that illustrate how the `custom_model` parameter can be used.

#### A general speed limit of 80 km/h, e.g. for car profiles

```json
{
    "speed": [
        {
            "if": true,
            "limit_to": 80
        }
    ]
}
```

#### Try to avoid motorways and tunnels (even more), avoid applying both rules to motorways within tunnels

```json
{
    "priority": [
        {
            "if": "road_class == MOTORWAY",
            "multiply_by": 0.7
        },
        {
            "else_if": "road_environment == TUNNEL",
            "multiply_by": 0.5
        }
    ]
}
```

#### Avoid having to get off the bike

```json
{
    "priority": [
        {
            "if": "get_off_bike",
            "multiply_by": 0
        }
    ]
}
```

#### Prefer staying on hiking routes

```json
{
    "priority": [
        {
            "if": "foot_network == MISSING",
            "multiply_by": 0.2
        }
    ]
}
```
