# Instruction Types

[//]: # (keep in sync with org.heigit.ors.routing.instructions.InstructionType; PT_ENTER, PT_TRANSFER, PT_EXIT are replaced with street names )


The following table contains the encoding of the instruction types present in the `type`-field of a directions response in a step of a segment of a route.

JSON-Path in json response:
```jsonpath
$.routes[*].segments[*].steps[*].type
```

JSON-Path in geojson response:
```jsonpath
$.features[*].properties.segments[*].steps[*].type
```

| Value | Encoding         |
|-------|------------------|
| 0     | Left             |
| 1     | Right            |
| 2     | Sharp left       |
| 3     | Sharp right      |
| 4     | Slight left      |
| 5     | Slight right     |
| 6     | Straight         |
| 7     | Enter roundabout |
| 8     | Exit roundabout  |
| 9     | U-turn           |
| 10    | Goal             |
| 11    | Depart           |
| 12    | Keep left        |
| 13    | Keep right       |
