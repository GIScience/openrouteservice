# Custom Models

The request body parameter `custom_model` allows setting specific parameters influencing the edge weightings during
route calculation.

This parameter is available for directions requests only on profiles that have been created using the required encoder
option set at graph build time. **This feature is still in experimental stat and is currently not available on our public API for any profile**. You can use this feature on your [own openrouteservice instance](/run-instance/) by [enabling it for the profile](/run-instance/configuration/engine/profiles/build.md#encoder-options) in the `encoder_options`. 

The `custom_model` parameter is a JSON object, the following example shows the structure:

```json
{
    "coordinates": [
        [
            8.681495,
            49.41461
        ],
        [
            8.687872,
            49.420318
        ]
    ],
    "custom_model": {
        "speed": [
            {
                "if": true,
                "limit_to": 100
            }
        ],
        "priority": [
            {
                "if": "road_class == MOTORWAY",
                "multiply_by": 0
            }
        ],
        "distance_influence": 100
    }
}
```

## Available parameters

| Parameter            | Type   | Description |
|----------------------|--------|-------------|
| `distance_influence` | Number |             |
| `speed`              | Array  |             |
| `priority`           | Array  |             |
| `areas`              | Array  |             |

## Examples

### general speed limit of 80 km/h, e.g. for car profiles

```json
{
    "speed": [
        {
            "if": true,
            "limit_to": 80
        }
    ]
}
```

### avoid motorways and tunnels

```json
{
    "priority": [
        {
            "if": "road_class == PRIMARY || road_environment == TUNNEL",
            "multiply_by": 0.7
        }
    ]
}
```