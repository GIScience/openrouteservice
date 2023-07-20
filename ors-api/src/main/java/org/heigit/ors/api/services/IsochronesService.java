package org.heigit.ors.api.services;

import org.heigit.ors.api.EndpointsProperties;

public class IsochronesService extends AbstractApiService {

    private final EndpointsProperties endpointsProperties;

    public IsochronesService(EndpointsProperties endpointsProperties) {
        this.endpointsProperties = endpointsProperties;
    }

    @Override
    EndpointsProperties getEndpointsProperties() {
        return this.endpointsProperties;
    }

    double getMaximumAvoidPolygonArea() {
//        return getEndpointsProperties().getIsochrone().getMaximumAvoidPolygonArea();
        return 0;
    }

    double getMaximumAvoidPolygonExtent() {
//        return getEndpointsProperties().getIsochrone().getMaximumAvoidPolygonExtent();
        return 0;
    }
}
