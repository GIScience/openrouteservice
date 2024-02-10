# Trail Difficulty

Value list for the directions response values in

```jsonpath
$.routes[*].extras.traildifficulty.values
```

This extra provides information about a trails difficulty for [hiking](https://wiki.openstreetmap.org/wiki/Key:sac_scale) as well as for [mountain-biking](https://wiki.openstreetmap.org/wiki/Key:mtb:scale).

| Value | foot-\*                              | cycling-\* |
|:-----:|-------------------------------------|-------------|
|   0   | no tag                              | no tag      |
|   1   | sac_scale=hiking                    | mtb:scale=0 |
|   2   | sac_scale=mountain_hiking           | mtb:scale=1 |
|   3   | sac_scale=demanding_mountain_hiking | mtb:scale=2 |
|   4   | sac_scale=alpine_hiking             | mtb:scale=3 |
|   5   | sac_scale=demanding_alpine_hiking   | mtb:scale=4 |
|   6   | sac_scale=difficult_alpine_hiking   | mtb:scale=5 |
|   7   |                 ---                 | mtb:scale=6 |

[//]: # (keep in sync with TrailDifficultyScaleGraphStorageBuilder.getSacScale)