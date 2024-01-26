# API Reference

There are different ways to get information about the openrouteservice API:

* **First starting point:** [API interactive examples](https://openrouteservice.org/dev/#/api-docs/directions_service): 
  Complete and concise technical documentation of our live API. 
  It can also be used to send sample requests to both, our live API or an instance of openrouteservice running on your local machine. 
  Use the form "API Server" on the top left to select and edit "Development Server" if you want to send requests to your local instance:
  ![](../public/playground-select-server.png)

  The responses of your sample requests are visualized on a small map on the bottom right, 
  but can also be shown as json, table or downloaded as file:
  ![](../public/playground-map.png)

* **This chapter** "API Reference" of the present text documentation: 
  Explanation and examples for some selected topics that might not be explained sufficiently in the interactive examples. 
  Here, the single endpoints are not completely covered with all their request parameters etc. 
  But on the other hand, there is some information about endpoints that are not available in our live API, 
  but e.g. on instances you run or host yourself: [Export](endpoints/export/index.md), [Snapping](endpoints/snapping/index.md), [Health](endpoints/health/index.md), [Status](endpoints/status/index.md)

* **Swagger-UI:** 
  Local instances of openrouteservice also have an accessible swagger-ui. 
  This is a way to get an interactive API doc also for old versions of openrouteservice:  
  Checkout the source code for the desired version or tag and run the service locally.
  You can then navigate to the swagger-ui hosted by your local instance in your browser: [http://localhost:8082/ors/swagger-ui/index.html](http://localhost:8082/ors/swagger-ui/index.html)
  (the port may be different on your local environment).
    :::warning Hint
    The swagger-ui is not available on the production ORS instances hosted by HeiGIT.
    :::

  ![](../public/swagger-ui.png)
