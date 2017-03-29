# HTTP Status Codes

The following table describes the supported HTTP status codes.

| HTTP Status Code   |  Description |
|----------|--------------|
| 200 |  Standard response for successfully processed requests.  |
| 400 |  The request is incorrect and therefore can not be processed. |
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
| 100 |  Unable to parse JSON request. |
| 101 |  Required parameter is missing. |
| 102 |  Invalid parameter format. |
| 103 |  Invalid parameter value. |
| 104 |  Parameter value exceeds the maximum allowed limit. |
| 199 |  Unknown internal error. |

## Routing API
| Error Code   |  Description |
|----------|-------------|
| 200 |  Unable to parse JSON request. |
| 201 |  Required parameter is missing. |
| 202 |  Invalid parameter format. |
| 203 |  Invalid parameter value. |
| 204 |  Parameter value exceeds the maximum allowed limit. |
| 299 |  Unknown internal error. |


## Isochrones API
| Error Code   |  Description |
|----------|-------------|
| 300 |  Unable to parse JSON request. |
| 301 |  Required parameter is missing. |
| 302 |  Invalid parameter format. |
| 303 |  Invalid parameter value. |
| 304 |  Parameter value exceeds the maximum allowed limit. |
| 305 |  Requested feature is not supported. |
| 399 |  Unknown internal error. |

## Locations API
| Error Code   |  Description |
|----------|-------------|
| 400 |  Unable to parse JSON request. |
| 401 |  Required parameter is missing. |
| 402 |  Invalid parameter format. |
| 403 |  Invalid parameter value. |
| 404 |  Parameter value exceeds the maximum allowed limit. |
| 499 |  Unknown internal error. |