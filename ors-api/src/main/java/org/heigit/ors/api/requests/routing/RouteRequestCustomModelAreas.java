package org.heigit.ors.api.requests.routing;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(type = "object", implementation = RouteRequestCustomModelGeoJSONFeature.class)
public interface RouteRequestCustomModelAreas extends Map<String, RouteRequestCustomModelGeoJSONFeature> {
    @Schema(hidden = true)
    boolean isEmpty();
}
