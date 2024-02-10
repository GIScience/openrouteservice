# WayType

Value list for the directions response values in

```jsonpath
$.routes[*].extras.waytype.values
```

This extra provides info about the type of the way of the corresponding parts of the route.

| Value |     Name     |  Corresponding [`highway`](https://wiki.openstreetmap.org/wiki/Key:highway)-tag(s) |
|:-----:|:------------:|:----------------------------------------------------------------------------------:|
|   0   |    Unknown   |                                                                                    |
|   1   |  State Road  |    `primary`, `primary_link`, `motorway`, `motorway_link`, `trunk`, `trunk_link`   |
|   2   |     Road     | `secondary`, `secondary_link`, `tertiary`, `tertiary_link`, `road`, `unclassified` |
|   3   |    Street    |                      `residential`, `service`, `living_street`                     |
|   4   |     Path     |                                       `path`                                       |
|   5   |     Track    |                                       `track`                                      |
|   6   |   Cycleway   |                                     `cycleway`                                     |
|   7   |    Footway   |                         `footway`, `pedestrian`, `crossing`                        |
|   8   |     Steps    |                                       `steps`                                      |
|   9   |     Ferry    |                        `route=shuttle_train`, `route=ferry`                        |
|   10  | Construction |                                   `construction`                                   |

[//]: # (keep in sync with org.heigit.ors.routing.graphhopper.extensions.WayType)
