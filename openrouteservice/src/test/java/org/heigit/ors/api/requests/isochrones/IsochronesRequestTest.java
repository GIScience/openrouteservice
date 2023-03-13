package org.heigit.ors.api.requests.isochrones;

import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.routing.RouteRequestOptions;
import org.heigit.ors.exceptions.ParameterValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class IsochronesRequestTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void idTest() {
        IsochronesRequest request = new IsochronesRequest();

        assertFalse(request.hasId());
        request.setId("1");
        assertTrue(request.hasId());
        assertEquals("1", request.getId());
    }

    @Test
    void smoothingTest() {
        IsochronesRequest request = new IsochronesRequest();

        assertFalse(request.hasSmoothing());
        request.setSmoothing(1.5);
        assertTrue(request.hasSmoothing());
        assertEquals(1.5, request.getSmoothing(), 0.0);
    }

    @Test
    void attributesTest() {
        IsochronesRequest request = new IsochronesRequest();

        assertFalse(request.hasAttributes());
        request.setAttributes(new IsochronesRequestEnums.Attributes[]{IsochronesRequestEnums.Attributes.AREA});
        assertTrue(request.hasAttributes());
        assertEquals(1, request.getAttributes().length);
        assertEquals(IsochronesRequestEnums.Attributes.AREA, request.getAttributes()[0]);
    }

    @Test
    void getIdTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertNull(request.getId());
    }

    @Test
    void setIdTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setId("foo");
        assertEquals("foo", request.getId());
    }

    @Test
    void hasIdTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertFalse(request.hasId());
        request.setId("foo");
        assertTrue(request.hasId());
    }

    @Test
    void setAreaUnitTest() throws ParameterValueException {
        IsochronesRequest request = new IsochronesRequest();
        request.setAreaUnit(APIEnums.Units.forValue("km"));
        assertEquals(APIEnums.Units.KILOMETRES, request.getAreaUnit());
    }

    @Test
    void getSmoothingTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertNull(request.getSmoothing());
    }

    @Test
    void setSmoothingTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setSmoothing(0.1);
        assertEquals(0.1, request.getSmoothing(), 0);
    }

    @Test
    void hasSmoothingTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertFalse(request.hasSmoothing());
        request.setSmoothing(0.1);
        assertTrue(request.hasSmoothing());
    }

    @Test
    void getResponseTypeTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertEquals(APIEnums.RouteResponseType.GEOJSON, request.getResponseType());
    }

    @Test
    void setResponseTypeTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setResponseType(APIEnums.RouteResponseType.JSON);
        assertEquals(APIEnums.RouteResponseType.JSON, request.getResponseType());

    }

    @Test
    void setIntersectionTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setIntersections(true);
        assertTrue(request.getIntersections());
    }

    @Test
    void setRangeUnitsTest() throws ParameterValueException {
        IsochronesRequest request = new IsochronesRequest();
        request.setRangeUnit(APIEnums.Units.forValue("km"));
        assertEquals(APIEnums.Units.KILOMETRES, request.getRangeUnit());
    }

    @Test
    void getAttributesTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertNull(request.getAttributes());
    }

    @Test
    void setAttributesTest() throws ParameterValueException {
        IsochronesRequest request = new IsochronesRequest();
        IsochronesRequestEnums.Attributes[] attributes = new IsochronesRequestEnums.Attributes[1];
        attributes[0] = IsochronesRequestEnums.Attributes.forValue("reachfactor");
        request.setAttributes(attributes);
        assertNotNull(request.getAttributes());
        assertEquals(IsochronesRequestEnums.Attributes.REACH_FACTOR, request.getAttributes()[0]);
    }

    @Test
    void hasAttributesTest() throws ParameterValueException {
        IsochronesRequest request = new IsochronesRequest();
        IsochronesRequestEnums.Attributes[] attributes = new IsochronesRequestEnums.Attributes[1];
        attributes[0] = IsochronesRequestEnums.Attributes.forValue("reachfactor");
        assertFalse(request.hasAttributes());
        request.setAttributes(attributes);
        assertTrue(request.hasAttributes());
    }

    @Test
    void getLocationTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertEquals(Double[][].class, request.getLocations().getClass());
    }

    @Test
    void setLocationTest() {
        IsochronesRequest request = new IsochronesRequest();
        Double[][] double_array = {{1.0, 2.0}, {1.0, 3.0}};
        request.setLocations(double_array);
        assertArrayEquals(double_array, request.getLocations());
    }

    @Test
    void setLocationTypeTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setLocationType(IsochronesRequestEnums.LocationType.DESTINATION);
        assertEquals(IsochronesRequestEnums.LocationType.DESTINATION, request.getLocationType());
    }

    @Test
    void getProfileTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertNull(request.getProfile());
    }

    @Test
    void setProfileTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        assertEquals(APIEnums.Profile.DRIVING_CAR, request.getProfile());
    }

    @Test
    void getIsochronesOptionsTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertNull(request.getIsochronesOptions());
    }

    @Test
    void setIsochronesOptionsTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setIsochronesOptions(new RouteRequestOptions());
        assertEquals(RouteRequestOptions.class, request.getIsochronesOptions().getClass());

    }

    @Test
    void hasIsochronesOptionsTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setIsochronesOptions(new RouteRequestOptions());
        assertTrue(request.hasOptions());
    }

    @Test
    void getRangeTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertNull(request.getRange());
    }

    @Test
    void setRangeTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setRange(new ArrayList<>());
        assertNotNull(request.getRange());
    }

    @Test
    void setRangeTypeTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setRangeType(IsochronesRequestEnums.RangeType.DISTANCE);
        assertEquals(IsochronesRequestEnums.RangeType.DISTANCE, request.getRangeType());
    }

    @Test
    void getIntervalTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertNull(request.getInterval());
    }

    @Test
    void setIntervalTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setInterval(Double.valueOf("0.0"));
        assertEquals(Double.valueOf("0.0"), request.getInterval());
    }

    @Test
    void detailedOptionsTest() {
        IsochronesRequest request = new IsochronesRequest();
        assertFalse(request.hasOptions());
        RouteRequestOptions opts = new RouteRequestOptions();
        request.setIsochronesOptions(opts);
        assertTrue(request.hasOptions());
    }
}