---
grand_parent: Documentation
parent: Routing Options
nav_order: 2
title: Examples
---

# Examples

**If your request works without the options object, but returns an error with it: try to [%-encode](Routing-Options#url-encoding) the options object!**

Some options examples in readable and minified JSON form:

for `profile=driving-car`:

```json
{
    "avoid_features": ["ferries", "tollways"]
}
```
`{"avoid_features":["ferries","tollways"]}`

for `profile=cycling-*`:

```json
{
"avoid_features": ["steps"],
"profile_params": {
    "weightings": {
        "steepness_difficulty": 2,
        },
    }
},
"avoid_polygons": {
    "type": "Polygon",
    "coordinates": [
        [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
 ]}
}
```
`{"avoid_features":["steps"],"profile_params":{"weightings":{"steepness_difficulty":2}}}},"avoid_polygons":{"type":"Polygon","coordinates":[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]}}`

for `profile=foot-*`:

```json
{
    "avoid_features": ["fords","ferries"],
    "profile_params": {
        "weightings": {
            "green": {
                "factor": 0.8
              },
            "quiet": {
                "factor": 1.0
            }
        }
    },
    "avoid_polygons": {  
        "type": "Polygon",
        "coordinates": [
            [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
     ]}
}
```
`{"avoid_features":["fords","ferries"],"profile_params":{"weightings":{"green":{"factor":0.8},"quiet":{"factor":1.0}}},"avoid_polygons":{"type":"Polygon","coordinates":[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]}}`

for `profile=driving-hgv`:

```json
{
    "avoid_features": ["ferries","tollways"],
    "vehcile_type": "hgv",
    "profile_params": {
        "restrictions": {
            "length": 30,
            "width": 30,
            "height": 3,
            "axleload": 4,
            "weight": 3,
            "hazmat": true
        }
    },
    "avoid_polygons": {  
        "type": "Polygon",
        "coordinates": [
            [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
     ]}
}
```
`{"avoid_features":["ferries","tollways"],"vehicle_type":"hgv","profile_params":{"restrictions":{"length":30,"width":30,"height":3,"axleload":4,"weight":3,"hazmat":true}},"avoid_polygons":{"type":"Polygon","coordinates":[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]]]}}`

for `profile=wheelchair`:

```json
{
    "avoid_features": ["ferries","steps"],
    "profile_params": {
        "restrictions": {
            "surface_type": "cobblestone:flattened",
            "track_type": "grade1",
            "smoothness_type": "good",
            "maximum_sloped_curb": 0.06,
            "maximum_incline": 6
        }
    }
}
```
`{"avoid_features":["ferries","steps"],"profile_params":{"restrictions":{"surface_type":"cobblestone:flattened","track_type":"grade1","smoothness_type":"good","maximum_sloped_curb":0.06,"maximum_incline":6}}}`

## Border restrictions

Examples for routing options object with border restrictions:

*Do not cross country borders at all:*

```json
{
    "avoid_borders":"all"
}
```

`{"avoid_borders":"all"}`

*Do not cross controlled borders (i.e. USA - Canada) but allow crossing of open borders (i.e. France - Germany):*

```json
{
    "avoid_borders":"controlled"
}
```

`{"avoid_borders":"controlled"}`

*Do not route through Austria or Switzerland:*

```json
{
    "avoid_countries": [1,120],
}
```

`{"avoid_countries": [1,120]}`

*Pass open borders but do not cross into Switzerland:*

```json
{
    "avoid_borders": "controlled",
    "avoid_countries": [193],
}
```

`{"avoid_borders": "controlled","avoid_countries": [193]}`
