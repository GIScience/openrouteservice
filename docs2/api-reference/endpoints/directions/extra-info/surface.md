# Surface

Value list for the directions response values in

```jsonpath
$.routes[*].extras.surface.values
```

This extra provides info about the [surface](https://wiki.openstreetmap.org/wiki/Key:surface) of the corresponding parts of the route.

| Value |       Name       |
|:-----:|:----------------:|
| 0     | Unknown          |
| 1     | Paved            |
| 2     | Unpaved          |
| 3     | Asphalt          |
| 4     | Concrete         |
| 5     | Cobblestone      |
| 6     | Metal            |
| 7     | Wood             |
| 8     | Compacted Gravel |
| 9     | Fine Gravel      |
| 10    | Gravel           |
| 11    | Dirt             |
| 12    | Ground           |
| 13    | Ice              |
| 14    | Paving Stones    |
| 15    | Sand             |
| 16    | Woodchips        |
| 17    | Grass            |
| 18    | Grass Paver      |

[//]: # (keep in sync with org.heigit.ors.routing.graphhopper.extensions.SurfaceType)