package heigit.ors.api.requests.routing;

import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXRouteResponse;
import heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects.GeoJSONRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.api.responses.routing.RouteResponseFactory;
import heigit.ors.util.mockupUtil.RouteResultMockup;
import org.junit.Assert;
import org.junit.Test;

public class GeometryResponseFactoryTest {
    @Test
    public void testResponseCreatedWithCorrectType() throws Exception {
        RouteRequest request = new RouteRequest(new Double[] {58.0,4.0}, new Double[] { 58.01, 4.01});
        request.setResponseType(APIRoutingEnums.RouteResponseType.JSON);

        RouteResponse test = RouteResponseFactory.constructResponse(RouteResultMockup.create(RouteResultMockup.routeResultProfile.standardHeidelberg), request);

        Assert.assertTrue(test instanceof JSONRouteResponse);

        request.setResponseType(APIRoutingEnums.RouteResponseType.GEOJSON);

        test = RouteResponseFactory.constructResponse(RouteResultMockup.create(RouteResultMockup.routeResultProfile.standardHeidelberg), request);

        Assert.assertTrue(test instanceof GeoJSONRouteResponse);

        request.setResponseType(APIRoutingEnums.RouteResponseType.GPX);

        test = RouteResponseFactory.constructResponse(RouteResultMockup.create(RouteResultMockup.routeResultProfile.standardHeidelberg), request);

        Assert.assertTrue(test instanceof GPXRouteResponse);
    }
}
