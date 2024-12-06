# WayCategory

Value list for the directions response values in

```jsonpath
$.routes[*].extras.waycategory.values
```

The exponential assignment of the values is used for [bit fields](http://eddmann.com/posts/using-bit-flags-and-enumsets-in-java/). One route section may belong to different categories. Hence, a value of ``97`` would indicate a belonging to ``Paved road``, ``Tunnel`` and ``Highway`` (``64`` + ``32`` + ``1`` ).

| Value |     Name    |             Corresponding tag(s)            |
|:-----:|:-----------:|:-------------------------------------------:|
|   0   | No category |                                             |
|   1   |   Highway   | `highway=motorway`, `highway=motorway_link` |
|   2   |   Tollways  |                 `toll*=yes`                 |
|   4   |    Steps    |               `highway=steps`               |
|   8   |    Ferry    |     `route=shuttle_train`, `route=ferry`    |
|   16  |     Ford    |                  `ford=yes`                 |

[//]: # (keep in sync with org.heigit.ors.routing.graphhopper.extensions.storages.builders.WayCategoryGraphStorageBuilder)
