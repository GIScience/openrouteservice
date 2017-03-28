# HTTP Status Codes

The following table describes the supported HTTP status codes.

| HTTP Status Code   |  Description |
|----------|--------------|
| 200 |  Standard response for successfully processed requests.  |
| 400 |  TODO |
| 405 |  TODO |
| 413 |  TODO |
| 500 |  An unexpected error was encountered and more detailed internal error code is provided. |
| 501 |  Indicates that the server does not support the functionality needed to fulfill the request. |
| 503 |  The server is currently unavailable due to overload or maintenance. |


# Internal Error Codes

The following sections describes the list of possible internal error codes that might be provided by different ORS EndPoints. 

## Geocoding
| Error Code   |  Description |
|----------|-------------|
| 100 |  Unable to parse JSON request. |
| 101 |  Required parameter is missing. |
| 102 |  Invalid parameter format. |
| 103 |  Invalid parameter value. |
| 104 |  Parameter value exceeds the maximum allowed limit. |
| 199 |  Unknown internal error. |

## Routing
| Error Code   |  Description |
|----------|-------------|
| 200 |  Unable to parse JSON request. |
| 299 |  Unknown internal error. |


## Isochrones
| Error Code   |  Description |
|----------|-------------|
| 300 |  Unable to parse JSON request. |
| 399 |  Unknown internal error. |

## Locations
| Error Code   |  Description |
|----------|-------------|
| 400 |  Unable to parse JSON request. |
| 499 |  Unknown internal error. |