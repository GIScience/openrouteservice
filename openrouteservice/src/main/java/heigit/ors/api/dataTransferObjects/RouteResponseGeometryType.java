package heigit.ors.api.dataTransferObjects;

import io.swagger.annotations.ApiModel;

@ApiModel
public enum RouteResponseGeometryType {
    GEOJSON,
    ENCODED_POLYLINE,
    GPX
}
