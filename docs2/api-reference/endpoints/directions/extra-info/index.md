# Extra Info

When requesting routes, there are a number of "extra info" items that can be requested to give you more information about the route. 
This info could be things like the road surface, track type, or OpenStreetMap way ID. 
The list below details which extra info items are available for each profile in the routing provided by https://api.openrouteservice.org.

## Specify Extra Info in Request

The desired extra info can be specified in the request body parameter `extra_info`:

```json
"extra_info": [ "steepness", "waytype"]
```

The following table lists the possible values for the request as well as the keys and possible values in the directions response:

| Request Value in `extra_info` | Description                                                                                                                                  | Key in Response `$.routes[*].extras`         | Response Values                                              |
|:----------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------|--------------------------------------------------------------|
|          steepness           | Provides information about how steep parts of the route are                                                                                  | `steepness`              | [Steepness IDs](steepness.md)                                |
|         suitability          | How suitable the way is based on characteristics of the route and the profile                                                                | `suitability`            | 1 (unsuitable) - 10 (very suitable)                          |
|           surface            | The surface covering along the route                                                                                                         | `surface`                | [Surface IDs](surface.md)                                    |
|         waycategory          | Specific categories of parts of the route (tollways, highways, fords etc.)                                                                   | `waycategory`            | [Category IDs](waycategory.md)                               |
|           waytype            | Types of roads and paths that are used in the route                                                                                          | `waytypes`¹              | [Type IDs](waytype.md)                                       |
|           tollways           | Any tollways that the route crosses. Whether a way is marked as [tollway](https://wiki.openstreetmap.org/wiki/Key:toll) depends on `profile` | `tollways`               | 0 (no tollway) or 1 (tollway)                                |
|       traildifficulty        | The difficulty of parts of the way based on sac or mountainbike scales                                                                       | `traildifficulty`        | [Difficulty IDs](trail-difficulty.md)                        |
|            osmid             | The OpenStreetMap way IDs of the ways the route uses                                                                                         | `osmId`¹                 |                                                              |
|    roadaccessrestrictions    | Information about ways that may have access restrictions (e.g. private roads, destination only)                                              | `roadaccessrestrictions` | [Restrictions IDs](road-access-restrictions.md)              |
|         countryinfo          | Which country parts of the way lies in                                                                                                       | `countryinfo`            | [Country IDs](../../../../technical-details/country-list.md) |
|            green             | How "green" the parts of the route are (influenced by things like number of trees, parks, rivers etc.)                                       | `green`                  | 0 (minimal greenspace) - 10 (a lot of green space)           |
|            noise             | How noisy the parts of the route are (influenced by things like proximity to highways)                                                       | `noise`                  | 0 (quiet) - 10 (noisy)                                       |
|            shadow            |                                                                                                                                              | `shadow`                 |                                                              |
|             csv              |                                                                                                                                              | `csv`                    |                                                              |

¹) *Note the different keys in request and response!*

[//]: # (TODO: write csv and shadow)
[//]: # (TODO: clarify avgspeed - found in ExtraInfoProcessor, but not in swagger docu for request)


## Extra Info Availability

Some values are not available in all routing profiles:

[//]: # (TODO: fill columns csv and shadow)    

|                  | steepness | suitability | surface | waycategory | waytype | tollways | traildifficulty | osmid | roadaccessrestrictions | countryinfo | green | noise | csv | shadow |
|:----------------:|:---------:|:-----------:|:-------:|:-----------:|:-------:|:--------:|:---------------:|:-----:|:----------------------:|:-----------:|:-----:|:-----:|:---:|:------:|
|   driving-car    |     x     |      x      |    x    |      x      |    x    |    x     |        x        |       |           x            |      x      |       |       |  ?  |   ?    |
|   driving-hgv    |     x     |      x      |    x    |      x      |    x    |    x     |        x        |       |           x            |      x      |       |       |  ?  |   ?    |
| cycling-regular  |     x     |      x      |    x    |      x      |    x    |          |        x        |       |                        |             |       |       |  ?  |   ?    |
| cycling-mountain |     x     |      x      |    x    |      x      |    x    |          |        x        |       |                        |             |       |       |  ?  |   ?    |
|   cycling-road   |     x     |      x      |    x    |      x      |    x    |          |        x        |       |                        |             |       |       |  ?  |   ?    |
|   foot-walking   |     x     |      x      |    x    |      x      |    x    |          |        x        |       |                        |             |   x   |   x   |  ?  |   ?    |
|   foot-hiking    |     x     |      x      |    x    |      x      |    x    |          |        x        |       |                        |             |   x   |   x   |  ?  |   ?    |
|    wheelchair    |     x     |      x      |    x    |      x      |    x    |          |        x        |   x   |                        |             |       |       |  ?  |   ?    |


## Extra Info in Responses

The requested extra information can be found in the directions response (JSON endpoints) in the node 

```jsonpath
$.routes[*].extras
```

of type map. Each requested extra_info is represented as a nested map entry with the requested value as key, e.g. `steepness`.
Note that some keys in the `extras` map are different from the requested values, see the table above!

Here one example: 

```json
"extras": {
  "waytypes": {
    "values": [
      [
        0,
        17,
        3
      ],
      [
        17,
        19,
        1
      ],
      [
        19,
        20,
        3
      ]
    ],
    "summary": [
      {
        "value": 3,
        "distance": 1285.2,
        "amount": 93.93
      },
      {
        "value": 1,
        "distance": 83,
        "amount": 6.07
      }
    ]
  },
  "steepness": {
    "values": [
      [
        0,
        20,
        0
      ]
    ],
    "summary": [
      {
        "value": 0,
        "distance": 1368.2,
        "amount": 100
      }
    ]
  }
}
```

The values of each entry have the same structure: Maps with the entries `values` and `summary`:

### `values`

A list of integer arrays with length 3 representing a contiguous section of the calculated route with the same value of the requested information type.
The three values of each list have the following semantics:

1. Index of the way point starting the section 
2. Index of the way point at the end of the section
3. Value of the extra info measure (waytype, steepness etc.) for this section

In the example above, the calculated route has three sections with different waytypes: 

* waypoints 0-17: waytype=3
* waypoints 17-19: waytype=1
* waypoints 19-20: waytype=3

and only one section (i.e. the whole route) from waypoint 0-20 with steepness=0.
 
### `summary`

A list of maps with the entries `value`, `distance`, `amount` summarizing the 
total length (`distance`) and relative length (`amount`) of all segments with a given `value` 
of the extra info unit.   

In the example above, all sections with waytype=3 have a summarized length of 1285.2 m 
which is 93.93% of the calculated route and 100% of the route have steepness=0.
