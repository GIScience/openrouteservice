package org.heigit.ors.api.util;

import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RoutingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("unittest")
class SystemMessageTest {

    @Autowired
    private final SystemMessageProperties systemMessageProperties = new SystemMessageProperties();

    @Test
    void testGetSystemMessage() throws ParameterValueException {

        RoutingRequest v1RouteRequest = new RoutingRequest();
        assertEquals("This message would be sent with every request on API v1 from January 2020 until June 2050", SystemMessage.getSystemMessage(v1RouteRequest, systemMessageProperties));

        RouteRequest routeRequest = new RouteRequest(new Double[][]{new Double[]{1.0, 1.0}, new Double[]{2.0, 2.0}});
        routeRequest.setProfile(APIEnums.Profile.CYCLING_REGULAR);
        routeRequest.setRoutePreference(APIEnums.RoutePreference.FASTEST);
        assertEquals("This message would be sent with every routing bike fastest request", SystemMessage.getSystemMessage(routeRequest, systemMessageProperties));

        IsochronesRequest isochronesRequest = new IsochronesRequest();
        assertEquals("This message would be sent with every request for geojson response", SystemMessage.getSystemMessage(isochronesRequest, systemMessageProperties));

        MatrixRequest matrixRequest = new MatrixRequest(new ArrayList<>());
        assertEquals("This message would be sent with every request", SystemMessage.getSystemMessage(matrixRequest, systemMessageProperties));

        assertEquals("This message would be sent with every request", SystemMessage.getSystemMessage(null, systemMessageProperties));
        assertEquals("This message would be sent with every request", SystemMessage.getSystemMessage("not a valid request parameter object", systemMessageProperties));
    }
}
