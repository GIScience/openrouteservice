package org.heigit.ors.api.responses.routing.gpx;

import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.util.mockuputil.RouteResultMockup;

public class GPXMetadataTest {
    RouteResult[] routeResult;

    public GPXMetadataTest() {
        try {
            routeResult = RouteResultMockup.create(RouteResultMockup.routeResultProfile.STANDARD_HEIDELBERG);
        } catch (Exception e) {

        }
    }
}
