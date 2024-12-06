# Surface

Value list for the directions response values in

```jsonpath
$.routes[*].extras.surface.values
```

This extra provides info about the [surface](https://wiki.openstreetmap.org/wiki/Key:surface) of the corresponding parts of the route.
The strike-through values have been recently removed.

| Value  |       Name       |   Corresponding value* of [`surface`](https://wiki.openstreetmap.org/wiki/Key:surface)-tag(s)   |
|:------:|:----------------:|:-----------------------------------------------------------------------------------------------:|
|   0    |     Unknown      |                                                                                                 |
|   1    |      Paved       |                                             `paved`                                             |
|   2    |     Unpaved      |               `unpaved`, `woodchips`, `rock`, `rocks`, `stone`, `shells`, `salt`                |
|   3    |     Asphalt      |                            `asphalt`, `chipseal`, `bitmac`, `tarmac`                            |
|   4    |     Concrete     |                                      `concrete`, `cement`                                       |
| ~~5~~  | ~~Cobblestone~~  |                                                                                                 |
|   6    |      Metal       |                                             `metal`                                             |
|   7    |       Wood       |                                             `wood`                                              |
|   8    | Compacted Gravel |                                   `compacted`, `pebblestone`                                    |
| ~~9~~  | ~~Fine Gravel~~  |                                                                                                 |
|   10   |      Gravel      |                                     `gravel`, `fine_gravel`                                     |
|   11   |       Dirt       |                                     `dirt`, `earth`, `soil`                                     |
|   12   |      Ground      |                                         `ground`, `mud`                                         |
|   13   |       Ice        |                                          `ice`, `snow`                                          |
|   14   |  Paving Stones   | `paving_stones`, `paved_stones`, `sett`, `cobblestone`, `unhewn_cobblestone`, `bricks`, `brick` |
|   15   |       Sand       |                                             `sand`                                              |
| ~~16~~ |  ~~Woodchips~~   |                                                                                                 |
|   17   |      Grass       |                                             `grass`                                             |
|   18   |   Grass Paver    |                                          `grass_paver`                                          |

*) For tags listing multiple values separated by a semicolon `;` only the first value is considered, and for a
given `value` all values of the form `value[:*]` are matched, where the part `[:*]` is optional. For example, all the
three ways tagged with `surface=concrete`, `surface=concrete:plates;asphalt` and `surface=cement`, respectively, would
be categorized as "Concrete".

[//]: # (keep in sync with org.heigit.ors.routing.graphhopper.extensions.SurfaceType)