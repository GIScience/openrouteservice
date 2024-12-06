# Route Attributes

With the request body parameter `attributes`, additional attributes of route segments can be requested in a directions request:

```json
"attributes":["avgspeed","detourfactor","percentage"]
```

The possible values are:

| Value        | Description                                                                                                                                                          |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| avgspeed     | This value is in _km/h_ and equals the average speed for this way segment after grading and applying factors.                                                        |
| detourfactor | This value is a _factor_ and gives the relative length of the segment with regard to the length of the beeline between the start and end point of the route segment. |
| percentage   | This value is in _percent_ and gives the segment length in terms of the route length.                                                                                | 

In the response, the additional attributes can be found in 

```jsonpath
$.routes[*].segments[*].avgspeed
$.routes[*].segments[*].detourfactor
$.routes[*].segments[*].percentage
```
