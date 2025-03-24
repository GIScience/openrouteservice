package org.heigit.ors.api.responses.routing.gpx;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.config.SystemMessageProperties;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.util.mockuputil.RouteResultMockup;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GPXRouteResponseTest {

    @Autowired
    private EndpointsProperties endpointsProperties;

    @Autowired
    private SystemMessageProperties systemMessageProperties;

    @Test
    void TestGetGpxRouteElements() throws Exception {

        RouteResult[] result = RouteResultMockup.create(RouteResultMockup.routeResultProfile.STANDARD_HEIDELBERG);
        List<List<Double>> coordinates = List.of(
                Arrays.asList(49.3988, 8.6724),
                Arrays.asList(49.399, 8.673)
        );
        RouteRequest request = new RouteRequest(coordinates);
        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        request.setResponseType(APIEnums.RouteResponseType.GPX);

        GPXRouteResponse response = new GPXRouteResponse(result, request, systemMessageProperties, endpointsProperties);

        assertEquals(response.getGpxRouteElements().toArray().length, result.length);

        BoundingBox bounds = response.getMetadata().getBounds();

        JAXBContext context = JAXBContext.newInstance(GPXBounds.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter writer = new StringWriter();
        marshaller.marshal(bounds, writer);

        String xmlBounds = writer.toString();
        assertTrue(xmlBounds.contains("minlat="), "minlat should appear with correct capitalization.");
        assertTrue(xmlBounds.contains("maxlat="), "maxlat should appear with correct capitalization.");
        assertTrue(xmlBounds.contains("minlon="), "minlon should appear with correct capitalization.");
        assertTrue(xmlBounds.contains("maxlon="), "maxlon should appear with correct capitalization.");

        assertEquals("openrouteservice", response.getGpxCreator());

        assertEquals("https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd", response.getXmlnsLink());

        assertEquals("1.1", response.getGpxVersion());
    }
}
