# Configuration



### Available properties

The properties are organized in a hierarchical structure. Since **openrouteservice** is based
on [spring](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html), all common
spring properties can be set in the ors-config.yml file. The most relevant for normal use are the following:

| key                            | type   | description                                                                                                                                                                                                  | default value                       |
|--------------------------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| server.port                    | int    | Server port                                                                                                                                                                                                  | 8082                                |                        
| logging.log4j2.config.override | string | Logging configuration file. **openrouteservice** ships with three presets (`DEFAULT_LOGGING.json`, `DEBUG_LOGGING.json`, `PRODUCTION_LOGGING.json`), but you can also define your own logging configuration. | classpath:logs/DEFAULT_LOGGING.json |                        

Additional properties specific to **openrouteservice** are organized under the following top level keys. Detailed
descriptions of each block follows below.

| key           | description                                                            |
|---------------|------------------------------------------------------------------------|
| ors.endpoints | Settings required at runtime to process API requests.                  |
| ors.engine    | Settings required at graph-build time during startup.                  |
| ors.cors      | CORS settings for the **openrouteservice** API.                        |
| ors.messages  | System messages can be sent with API responses following simple rules. |

### Properties in the `endpoints` block

| key                                             | type   | description                                                                            | default value                     |
|-------------------------------------------------|--------|----------------------------------------------------------------------------------------|-----------------------------------|
| ors.endpoints.routing.base_url                  | string |                                                                                        | https://openrouteservice.org/     |
| ors.endpoints.routing.swagger_documentation_url | string | Define the url for the the swagger documentation. Can be different from the `base_url` | https://api.openrouteservice.org/ |
| ors.endpoints.routing.support_mail              | string |                                                                                        | support@openrouteservice.org      |
| ors.endpoints.routing.author_tag                | string |                                                                                        | openrouteservice                  |
| ors.endpoints.routing.content_licence           | string |                                                                                        | LGPL 3.0                          ||    

### Properties in the `engine` block

| key          | type   | description | default value |
|--------------|--------|-------------|---------------|
| ors.engine.x | string |             |               |

### Properties in the `cors` block

| key                    | type                          | description                                                                        | default value                                                                                                                |
|------------------------|-------------------------------|------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| cors.allowed_origins   | string / comma separated list | Configures the Access-Control-Allow-Origins CORS header. `*` for all origins       | *                                                                                                                            |                        
| cors.allowed_headers   | string / comma separated list | Configures the Access-Control-Allow-Headers CORS header. `*` for all headers       | Content-Type, X-Requested-With, accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization | 
| cors.preflight_max_age | int                           | Duration in seconds. Specifies how long the OPTIONS response is cached by browsers | 600                                                                                                                          |                               

### Properties in the `messages` block

The messages property expects a list of elements where each has the following:

| key       | type    | description                                                       | example value        |
|-----------|---------|-------------------------------------------------------------------|----------------------|
| active    | boolean | Enables or disables this message                                  | `true`               |
| text      | string  | The message text                                                  | `"The message text"` |
| condition | list    | omittable; may contain any of the conditions from the table below |                      |

| condition          | value                                                                                                                | description                                                         |
|--------------------|----------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| time_before        | ISO 8601 datetime string                                                                                             | message sent if server local time is before given point in time     |
| time_after         | ISO 8601 datetime string                                                                                             | message sent if server local time is after given point in time      |
| api_version        | 1 or 2                                                                                                               | message sent if API version requested through matches value         |
| api_format         | String with output formats ("json", "geojson", "gpx"), comma separated                                               | message sent if requested output format matches value               |
| request_service    | String with service names ("routing", "matrix", "isochrones", "snap"), comma separated                               | message sent if requested service matches one of the given names    |
| request_profile    | String with profile names, comma separated                                                                           | message sent if requested profile matches one of the given names    |
| request_preference | String with preference (weightings for routing, metrics for matrix, rangetype for isochrones) names, comma separated | message sent if requested preference matches one of the given names |

##### Example:

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
