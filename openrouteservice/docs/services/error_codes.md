# HTTP Status Codes

The following table describes the supported HTTP status codes.

| HTTP Status Code   |  Description |
|----------|--------------|
| 200 |  Standard response for successfully processed requests.  |
| 400 |  The request is incorrect and therefore can not be processed. |
| 404 |  The end point is not available, or a given request completed successfully but found no results.  |
| 405 |  The specified HTTP method is not supported. For more details, refer to the EndPoint documentation.   |
| 413 |  The request is larger than the server is able to process, the data provided in the request exceeds the capacity limit. |
| 500 |  An unexpected error was encountered and more detailed internal error code is provided (see **Internal Error Codes**). |
| 501 |  Indicates that the server does not support the functionality needed to fulfill the request. |
| 503 |  The server is currently unavailable due to overload or maintenance. |


# Internal Error Codes

The following sections describes the list of possible internal error codes that might be provided by different ORS EndPoints. 

## Geocoding API
| Error Code   |  Description |
|----------|-------------|
| 1000 |  Unable to parse JSON request. |
| 1001 |  Required parameter is missing. |
| 1002 |  Invalid parameter format. |
| 1003 |  Invalid parameter value. |
| 1004 |  Parameter value exceeds the maximum allowed limit. |
| 1006 |  Unable to parse the request to the export handler. |
| 1007 |  Unsupported export format. |
| 1008 |  Empty Element. |
| 1009 |  Address not found for location. |
| 1099 |  Unknown internal error. |

## Routing API
| Error Code   |  Description |
|----------|-------------|
| 2000 |  Unable to parse JSON request. |
| 2001 |  Required parameter is missing. |
| 2002 |  Invalid parameter format. |
| 2003 |  Invalid parameter value. |
| 2004 |  Parameter value exceeds the maximum allowed limit. |
| 2006 |  Unable to parse the request to the export handler. |
| 2007 |  Unsupported export format. |
| 2008 |  Empty Element. |
| 2009 |  Route could not be found between locations. |
| 2099 |  Unknown internal error. |

## Isochrones API
| Error Code   |  Description |
|----------|-------------|
| 3000 |  Unable to parse JSON request. |
| 3001 |  Required parameter is missing. |
| 3002 |  Invalid parameter format. |
| 3003 |  Invalid parameter value. |
| 3004 |  Parameter value exceeds the maximum allowed limit. |
| 3005 |  Requested feature is not supported. |
| 3006 |  Unable to parse the request to the export handler. |
| 3007 |  Unsupported export format. |
| 3008 |  Empty Element. |
| 3099 |  Unknown internal error. |

## POIs API
| Error Code   |  Description |
|----------|-------------|
| 4000 |  Invalid JSON object in request. |
| 4001 |  Category or category group ids missing. |
| 4002 |  Geometry is missing. |
| 4003 |  Bounding box and or geojson not present in request. |
| 4004 |  Buffer is missing. |
| 4005 |  Geometry length does not meet the restrictions. |
| 4006 |  Unsupported HTTP method. |
| 4007 |  GeoJSON parsing error. |
| 4008 |  Geometry size does not meet the restrictions. |
| 4099 |  Unknown internal error. |

## Matrix API
| Error Code   |  Description |
|----------|-------------|
| 6000 |  Unable to parse JSON request. |
| 6001 |  Required parameter is missing. |
| 6002 |  Invalid parameter format. |
| 6003 |  Invalid parameter value. |
| 6004 |  Parameter value exceeds the maximum allowed limit. |
| 6006 |  Unable to parse the request to the export handler. |
| 6007 |  Unsupported export format. |
| 6008 |  Empty Element. |
| 6099 |  Unknown internal error. |