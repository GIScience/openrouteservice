package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.routing.RouteResult;
import heigit.ors.util.mockupUtil.RouteResultMockup;
import org.junit.Test;

public class GPXMetadataTest {
    RouteResult routeResult[];

    public GPXMetadataTest() {
        try {
            routeResult = RouteResultMockup.create(RouteResultMockup.routeResultProfile.standardHeidelberg);
        } catch (Exception e) {

        }
    }
}
