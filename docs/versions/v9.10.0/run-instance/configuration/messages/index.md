
# ors.messages

System messages that can be sent with API responses following simple rules.

The `ors.messages` block expects a list of elements, each of which having the following:

| key       | type    | description                                                      | example value      |
|-----------|---------|------------------------------------------------------------------|--------------------|
| active    | boolean | Enables or disables this message                                 | `true`             |
| text      | string  | The message text                                                 | `The message text` |
| condition | list    | optional; may contain any of the conditions from the table below |                    |

## condition

Properties beneath `ors.messages.condition`: 

| key                | value                                                                                                                | description                                                         |
|--------------------|----------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| time_before        | ISO 8601 datetime string                                                                                             | message sent if server local time is before given point in time     |
| time_after         | ISO 8601 datetime string                                                                                             | message sent if server local time is after given point in time      |
| api_version        | 1 or 2                                                                                                               | message sent if API version requested through matches value         |
| api_format         | String with output formats ("json", "geojson", "gpx"), comma separated                                               | message sent if requested output format matches value               |
| request_service    | String with service names ("routing", "matrix", "isochrones", "snap"), comma separated                               | message sent if requested service matches one of the given names    |
| request_profile    | String with profile names, comma separated                                                                           | message sent if requested profile matches one of the given names    |
| request_preference | String with preference (weightings for routing, metrics for matrix, rangetype for isochrones) names, comma separated | message sent if requested preference matches one of the given names |

If multiple conditions are given, all must be fulfilled to trigger the sending of the corresponding message.

Example:

```
messages:
  - active: true
    text: This message would be sent with every routing bike fastest request
    condition:
      - request_service: routing
      - request_profile: cycling-regular,cycling-mountain,cycling-road,cycling-electric
      - request_preference: fastest
  - active: true
    text: This message would be sent with every request for geojson response
    condition:
      - api_format: geojson
  - active: true
    text: This message would be sent with every request on API v1 from January 2020 until June 2050
    condition:
      - api_version: 1
      - time_after: 2020-01-01T00:00:00Z
      - time_before: 2050-06-01T00:00:00Z
  - active: true
    text: This message would be sent with every request
```
