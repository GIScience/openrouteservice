# Error Codes

## HTTP Status Codes

The following table describes the supported HTTP status codes.

| HTTP Status Code | Description                                                                                                                                |
|:----------------:|--------------------------------------------------------------------------------------------------------------------------------------------|
| 200              | Standard response for successfully processed requests.                                                                                     |
| 400              | The request is incorrect and therefore can not be processed.                                                                               |
| 404              | The end point is not available, or a given request completed successfully but found no results.                                            |
| 405              | The specified HTTP method is not supported. For more details, refer to the Endpoint documentation.                                         |
| 413              | The request is larger than the server is able to process, the data provided in the request exceeds the capacity limit.                     |
| 500              | An unexpected error was encountered and more detailed internal error code is provided (see [Internal Error Codes](#internal-error-codes)). |
| 501              | Indicates that the server does not support the functionality needed to fulfill the request.                                                |
| 503              | The server is currently unavailable due to overload or maintenance.                                                                        |



## Internal Error Codes

The following sections describes the list of possible internal error codes that might be provided by different ORS
Endpoints.

### Routing API

[//]: # (keep in sync with org.heigit.ors.routing.RoutingErrorCodes)

| Error Code | Description                                        |
|:----------:|----------------------------------------------------|
|    2000    | Unable to parse JSON request.                      |x
|    2001    | Required parameter is missing.                     |x
|    2002    | Invalid parameter format.                          |x
|    2003    | Invalid parameter value.                           |x
|    2004    | Parameter value exceeds the maximum allowed limit. |
|    2006    | Unable to parse the request to the export handler. |
|    2007    | Unsupported export format.                         |x
|    2008    | Empty Element.                                     |
|    2009    | Route could not be found between locations.        |
|    2010    | Point was not found.                               |
|    2011    | Incompatible parameters.                           |
|    2012    | Unknown parameter.                                 |x
|    2013    | Entry not reached.                                 |
|    2014    | Exit not reached.                                  |
|    2015    | Entry not reached.                                 |
|    2016    | No route between entry and exit found.             |
|    2017    | Maximum number of nodes exceeded.                  |
|    2099    | Unknown internal error.                            |

### Isochrones API

[//]: # (keep in sync with org.heigit.ors.isochrones.IsochronesErrorCodes)

| Error Code | Description                                        |
|:----------:|----------------------------------------------------|
|    3000    | Unable to parse JSON request.                      |
|    3001    | Required parameter is missing.                     |
|    3002    | Invalid parameter format.                          |
|    3003    | Invalid parameter value.                           |
|    3004    | Parameter value exceeds the maximum allowed limit. |
|    3005    | Requested feature is not supported.                |
|    3006    | Unable to parse the request to the export handler. |
|    3007    | Unsupported export format.                         |
|    3008    | Empty Element.                                     |
|    3011    | Unknown parameter.                                 |
|    3012    | Parameter value exceeds the minimum allowed limit.  |
|    3099    | Unknown internal error.                            |

### POIs API

[//]: # (keep in sync with openpoiservice https://github.com/GIScience/openpoiservice/blob/master/openpoiservice/server/api/__init__.py)

| Error Code | Description                                         |
|:----------:|-----------------------------------------------------|
|    4000    | Invalid JSON object in request.                     |
|    4001    | Category or category group ids missing.             |
|    4002    | Geometry is missing.                                |
|    4003    | Bounding box and or geojson not present in request. |
|    4004    | Buffer is missing.                                  |
|    4005    | Geometry length does not meet the restrictions.     |
|    4006    | Unsupported HTTP method.                            |
|    4007    | GeoJSON parsing error.                              |
|    4008    | Geometry size does not meet the restrictions.       |
|    4099    | Unknown internal error.                             |

### Matrix API

[//]: # (keep in sync with org.heigit.ors.matrix.MatrixErrorCodes)

| Error Code | Description                                        |
|:----------:|----------------------------------------------------|
|    6000    | Unable to parse JSON request.                      |
|    6001    | Required parameter is missing.                     |
|    6002    | Invalid parameter format.                          |
|    6003    | Invalid parameter value.                           |
|    6004    | Parameter value exceeds the maximum allowed limit. |
|    6006    | Unable to parse the request to the export handler. |
|    6007    | Unsupported export format.                         |
|    6008    | Empty Element.                                     |
|    6010    | Point not found.                                   |
|    6011    | Unknown parameter.                                 |
|    6020    | Maximum number of visited nodes exceeded.          |
|    6099    | Unknown internal error.                            |

### Export API

[//]: # (keep in sync with org.heigit.ors.export.ExportErrorCodes)

| Error Code | Description                    |
|:----------:|--------------------------------|
|    7000    | Unable to parse JSON request.  |
|    7001    | Required parameter is missing. |
|    7002    | Invalid parameter format.      |
|    7003    | Invalid parameter value.       |
|    7004    | Unknown parameter.             |
|    7005    | Mismatched input.              |
|    7006    | Unsupported export format.     |
|    7099    | Unknown internal error.        |

### Snapping API 

[//]: # (keep in sync with org.heigit.ors.snapping.SnappingErrorCodes)

| Error Code | Description                    |
|:----------:|--------------------------------|
|    8000    | Unable to parse JSON request.  |
|    8001    | Required parameter is missing. |
|    8002    | Invalid parameter format.      |
|    8003    | Invalid parameter value.       |
|    8004    | Unknown parameter.             |
|    8006    | Unsupported export format.     |
|    8010    | Point not found.               |
|    8099    | Unknown internal error.        |
