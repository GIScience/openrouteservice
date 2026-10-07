# Health Endpoint

:::warning NOTE
This endpoint is not available in the public API, but you can use it when running an own instance of openrouteservice.
:::

The GET request http://localhost:8082/ors/v2/health (host and port might be different) returns the current health or readiness status 
of the running instance with two possible values:

* `not ready`: the openrouteservice instance is still busy with building graphs
* `ready`: all required graphs are computed, the openrouteservice instance is ready to process spatial requests