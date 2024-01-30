# Steepness

Value list for the directions response values in

```jsonpath
$.routes[*].extras.steepness.values
```

| Value |           Encoding |
|:-----:|-------------------:|
|  -5   |      >=16% decline |
|  -4   | 10% - <16% decline |
|  -3   |  7% - <10% decline |
|  -2   |   4% - <7% decline |
|  -1   |   1% - <4% decline |
|   0   |   0% - <1% incline |
|   1   |   1% - <4% incline |
|   2   |   4% - <7% incline |
|   3   |  7% - <10% incline |
|   4   | 10% - <16% incline |
|   5   |      >=16% incline |

[//]: # (keep in sync with org.heigit.ors.routing.util.SteepnessUtil.getCategory )