# Road Access Restrictions

Value list for the directions response values in 

```jsonpath
$.routes[*].extras.roadaccessrestrictions.values
```

Provides information about possible restrictions on roads.
Explanation of the values can be found in the [list of possible values in the OSM Wiki](https://wiki.openstreetmap.org/wiki/Key:access)

| Value |             Encoding              |
|:-----:|:---------------------------------:|
|   0   | None (there are no restrictions)  |
|   1   |                No                 |
|   2   |             Customers             |
|   4   |            Destination            |
|   8   |             Delivery              |
|  16   |              Private              |
|  32   |            Permissive             |

[//]: # (keep in sync with org.heigit.ors.routing.graphhopper.extensions.AccessRestrictionType.class)