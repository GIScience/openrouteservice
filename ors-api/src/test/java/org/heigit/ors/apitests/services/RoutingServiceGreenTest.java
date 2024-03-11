package org.heigit.ors.api.services;

import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.routing.APIEnums;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.requests.routing.RouteRequestOptions;
import org.heigit.ors.api.requests.routing.RequestProfileParams;
import org.heigit.ors.api.requests.routing.RequestProfileParamsWeightings;
import org.heigit.ors.api.requests.routing.RouteRequest;

import org.junit.jupiter.api.Test;
class RoutingGreenTest {

    public RoutingGreenTest() throws Exception {

    }
    @Test
    void RoutingGreenTestCargo(){
        EndpointsProperties endpointsProperties = new EndpointsProperties();
        RoutingService routingService  = new RoutingService(endpointsProperties);
        Coordinate start = new Coordinate(8.650547, 49.400285);
        Coordinate end = new Coordinate(9.121427,48.775995);

        try{
            RouteRequest request = new RouteRequest(start, end);
            request.setProfile(APIEnums.Profile.CYCLING_CARGO);

            RouteRequestOptions options = new RouteRequestOptions();
            RequestProfileParams params = new RequestProfileParams();
            RequestProfileParamsWeightings weightings = new RequestProfileParamsWeightings();

            weightings.setGreenIndex(0.5f);
            weightings.setQuietIndex(0.2f);
            weightings.setSteepnessDifficulty(3);

            params.setWeightings(weightings);
            params.setSurfaceQualityKnown(true);
            params.setAllowUnsuitable(true);

            options.setProfileParams(params);
            request.setRouteOptions(options);

            RouteResult[] result = routingService.generateRouteFromRequest(request);
        } catch (Exception e){
            int i = 7;
        }

    }
}
