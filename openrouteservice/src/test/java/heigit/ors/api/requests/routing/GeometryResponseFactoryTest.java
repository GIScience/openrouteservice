package heigit.ors.api.requests.routing;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXRouteResponse;
import heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects.GeoJSONRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONRouteResponse;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.api.responses.routing.RouteResponseFactory;
import heigit.ors.util.mockupUtil.RouteResultMockup;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class GeometryResponseFactoryTest {
    @Test
    public void testResponseCreatedWithCorrectType() throws Exception {
        List<List<Double>> coords = new ArrayList<>();
        List<Double> coord1 = new ArrayList<>();
        coord1.add(24.5);
        coord1.add(39.2);
        coords.add(coord1);
        List<Double> coord2 = new ArrayList<>();
        coord2.add(27.4);
        coord2.add(38.6);
        RouteRequest request = new RouteRequest(coords);
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
