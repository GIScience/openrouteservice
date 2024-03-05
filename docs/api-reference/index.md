# API Reference

There are different ways to get information about the openrouteservice API.

As a first starting point best try out our [API Playground](#api-playground).

The following chapter [Endpoints](endpoints/index.md) contains explanation and examples for some selected topics that might not
be explained sufficiently in the API Playground.
Here, the single endpoints are not completely covered with all their request parameters etc.
But on the other hand, there is also information about endpoints that are not available in our live API,
but e.g. on instances you run or host yourself like [Export](endpoints/export/index.md), [Snapping](endpoints/snapping/index.md), [Health](endpoints/health/index.md) and [Status](endpoints/status/index.md).

If you are developing ORS or running your own instance, you might benefit from the included [Swagger-UI](#swagger-ui).

## API Playground

The [API Playground](https://openrouteservice.org/dev/#/api-docs/directions_service) is a complete, interactive and concise technical documentation of our live API.
It can also be used to send sample requests to either our live API or an instance of openrouteservice running on your local machine.
Use the form "API Server" on the top left to select and edit "Development Server" if you want to send requests to your local instance:
![Development server usage](/playground-select-server.png "Development server usage"){ style="display: block; margin: 0 auto"}

The responses of your sample requests are visualized on a small map on the bottom right,
but can also be shown as json, table or downloaded as file:
![Example request visualization](/playground-map.png "Example request visualization"){ style="display: block; margin: 0 auto"}

::: warning Hint
If you prefer the swagger-ui you can also use the [swagger editor](https://editor-next.swagger.io/) and load
the full OpenAPI spec file for our API from https://openrouteservice.org/wp-json/ors-api/v1/api-doc/source/V2.
However, some of the displayed endpoints or features won't be accessible with our live API.
:::

## Swagger-UI

Local instances of openrouteservice also have an accessible swagger-ui.
This is a way to get an interactive API doc for your current or an older version of openrouteservice:
1. Checkout the source code for the desired version or tag and [run the service locally](/run-instance/index.md).
2. You can then navigate to the swagger-ui hosted by your local instance in your browser: [http://localhost:8082/ors/swagger-ui/index.html](http://localhost:8082/ors/swagger-ui/index.html)
(the port may be different on your local environment).

:::warning Hint
The swagger-ui is not available on the production ORS instances hosted by HeiGIT.
:::

![Swagger-UI](/swagger-ui.png "Swagger UI")
