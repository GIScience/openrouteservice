# Geocoder Endpoint

:::warning NOTE
This endpoint is not part of the openrouteservice, but of our public API. It is not available when running an own instance of openrouteservice. 
:::

The Geocoder Endpoint of our public API is served by a [Pelias](https://www.pelias.io) instance.
It resolves geographic coordinates to addresses and vice versa.

Our public API exposes these endpoints:

* [Forward Geocode Service](https://openrouteservice.org/dev/#/api-docs/geocode/search/get): In the simplest search, you can provide only one parameter, the text you want to match in any part of the location details. To do this, build a query where the text parameter is set to the item you want to find.
* [Geocode Autocomplete Service](https://openrouteservice.org/dev/#/api-docs/geocode/autocomplete/get): This type-ahead functionality helps users find what they are looking for, without requiring them to fully specify their search term. Typically, the user starts typing and a drop-down list appears where they can choose the term from the list below.
* [Structured Forward Geocode Service](https://openrouteservice.org/dev/#/api-docs/geocode/search/structured/get): Structured geocoding can improve how the items in your query are parsed and interpreted in a search by defining search terms for specific fields like `address`, `postalcode` etc. _(:warning: This endpoint is beta.)_
* [Reverse Geocode Service](https://openrouteservice.org/dev/#/api-docs/geocode/reverse/get): Reverse geocoding is used for finding places or addresses near a latitude, longitude pair — like clicking on a map to see what's there.

For more insights please refer to the [Pelias Documentation](https://github.com/pelias/documentation).