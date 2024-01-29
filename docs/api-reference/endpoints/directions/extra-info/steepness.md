# Steepness

Value list for the directions response values in

```jsonpath
$.routes[*].extras.steepness.values
```

Negative values indicate decline, positive incline.

| Value |  Encoding  |
|:-----:|:----------:|
|  -5   |   >=16%    |
|  -4   | 10% - <16% |
|  -3   | 7% - <10%  |
|  -2   |  4% - <7%  |
|  -1   |  1% - <4%  |
|   0   |  0% - <1%  |
|   1   |  1% - <4%  |
|   2   |  4% - <7%  |
|   3   | 7% - <10%  |
|   4   | 10% - <16% |
|   5   |   >=16%    |

[//]: # (keep in sync with org.heigit.ors.routing.util.SteepnessUtil.getCategory )