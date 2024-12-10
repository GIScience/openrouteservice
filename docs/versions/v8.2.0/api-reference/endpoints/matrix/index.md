# Matrix Endpoint

The Matrix Endpoint calculates profile specific distance and time matrices between multiple source and destination positions.

Besides parameters for the routing profile, the desired metrics (distance or time) and some more (see [API Playground](https://openrouteservice.org/dev/#/api-docs/matrix_service)), 
the most important and mandatory content of a matrix request is a list of locations (coordinate tuples), e.g.:

```json
{
  "locations": [
    [8.66044,49.41571],
    [8.67842,49.40664],
    [8.68370,49.40854],
    [8.69014,49.40778]
  ]
}
```

To specify, from which to which of these locations the metric should be calculated, the parameters `sources` and `destinations` can be specified in the request:

```json
"sources": ["0", "1"],
"destinations": ["2", "3"]
```

The numbers in both parameter value arrays represent the index of the locations array in the same request. 
In the example above, the duration from the first (0) and second (1) location to the third (2) and fourth (3) location will be calculated: 

    0 -> 2
    0 -> 3
    1 -> 2
    1 -> 3

In the response, the results are contained in the node `durations`.
Here, the first array contains the duration from the first source (0) to both destinations (2 and 3), 
and the second array contains the duration from the second source (1) to both destinations (2 and 3):

```json
  "durations": [
    [            // durations from first source to each destination
      448.82,    //     0 -> 2
      553.01     //     0 -> 3
    ],
    [            // durations from second source to each destination
      142.68,    //     1 -> 2
      246.88     //     1 -> 3
    ]
  ]
```

If `sources` or `destinations` is not specified in the request, each of both defaults to "all locations". 
If for example the locations array has 5 entries, 
the sources array has only one entry `[0]`, 
and `destinations` is missing, then duration are calculated from location 0 to all locations in the location list.

The first entry in the result `durations` list represents the duration from location 0 to itself and is `0`. 

To specify whether distances or duration (or both) are to be calculated, the `metrics` parameter can be set accordingly.

For details about all request parameters and the response type, see the [API Playground](https://openrouteservice.org/dev/#/api-docs/matrix_service).
