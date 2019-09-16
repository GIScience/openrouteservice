package org.heigit.ors.api.responses.routing.gpx;

import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.util.mockupUtil.RouteResultMockup;
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
