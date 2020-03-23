package org.heigit.ors.api.util;

import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.routing.RoutingRequest;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class SystemMessageTest {
    @Test
    public void testGetSystemMessage() throws ParameterValueException {
        System.setProperty("ors_app_config", "target/test-classes/app.config.test");

        RoutingRequest v1RouteRequest = new RoutingRequest();
        Assert.assertEquals("This message would be sent with every request on API v1 from January 2020 until June 2050", SystemMessage.getSystemMessage(v1RouteRequest));

        RouteRequest routeRequest = new RouteRequest(new Double[][] {new Double[] {1.0,1.0}, new Double[] {2.0,2.0}});
        routeRequest.setProfile(APIEnums.Profile.CYCLING_REGULAR);
        routeRequest.setRoutePreference(APIEnums.RoutePreference.FASTEST);
        Assert.assertEquals("This message would be sent with every routing bike fastest request", SystemMessage.getSystemMessage(routeRequest));

        IsochronesRequest isochronesRequest = new IsochronesRequest();
        Assert.assertEquals("This message would be sent with every request for geojson response", SystemMessage.getSystemMessage(isochronesRequest));

        MatrixRequest matrixRequest = new MatrixRequest(new ArrayList<>());
        Assert.assertEquals("This message would be sent with every request", SystemMessage.getSystemMessage(matrixRequest));

        Assert.assertEquals("This message would be sent with every request", SystemMessage.getSystemMessage(null));
        Assert.assertEquals("This message would be sent with every request", SystemMessage.getSystemMessage("not a valid request parameter object"));
    }
}
