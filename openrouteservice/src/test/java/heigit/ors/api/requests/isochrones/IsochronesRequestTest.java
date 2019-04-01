package heigit.ors.api.requests.isochrones;

import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.routing.RouteRequestOptions;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static heigit.ors.util.HelperFunctions.fakeArrayLocations;

public class IsochronesRequestTest {

    @Before
    public void setUp() {
    }

    @Test
    public void idTest() {
        IsochronesRequest request = new IsochronesRequest();

        Assert.assertFalse(request.hasId());
        request.setId("1");
        Assert.assertTrue(request.hasId());
        Assert.assertEquals("1", request.getId());
    }

    @Test
    public void smoothingTest() {
        IsochronesRequest request = new IsochronesRequest();

        Assert.assertFalse(request.hasSmoothing());
        request.setSmoothing(1.5);
        Assert.assertTrue(request.hasSmoothing());
        Assert.assertEquals(1.5, request.getSmoothing(), 0.0);
    }

    @Test
    public void attributesTest() {
        IsochronesRequest request = new IsochronesRequest();

        Assert.assertFalse(request.hasAttributes());
        request.setAttributes(new IsochronesRequestEnums.Attributes[]{IsochronesRequestEnums.Attributes.AREA});
        Assert.assertTrue(request.hasAttributes());
        Assert.assertEquals(request.getAttributes().length, 1);
        Assert.assertEquals(IsochronesRequestEnums.Attributes.AREA, request.getAttributes()[0]);
    }

    @Test
    public void getIdTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertNull(request.getId());
    }

    @Test
    public void setIdTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setId("foo");
        Assert.assertEquals("foo", request.getId());
    }

    @Test
    public void hasIdTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertFalse(request.hasId());
        request.setId("foo");
        Assert.assertTrue(request.hasId());
    }

    @Test
    public void setAreaUnitTest() throws ParameterValueException {
        IsochronesRequest request = new IsochronesRequest();
        request.setAreaUnit(APIEnums.Units.forValue("km"));
        Assert.assertEquals(APIEnums.Units.KILOMETRES, request.getAreaUnit());
    }

    @Test
    public void getSmoothingTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertNull(request.getSmoothing());
    }

    @Test
    public void setSmoothingTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setSmoothing(0.1);
        Assert.assertEquals(0.1, request.getSmoothing(), 0);
    }

    @Test
    public void hasSmoothingTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertFalse(request.hasSmoothing());
        request.setSmoothing(0.1);
        Assert.assertTrue(request.hasSmoothing());
    }

    @Test
    public void getResponseTypeTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertEquals(APIEnums.RouteResponseType.GEOJSON, request.getResponseType());
    }

    @Test
    public void setResponseTypeTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setResponseType(APIEnums.RouteResponseType.JSON);
        Assert.assertEquals(APIEnums.RouteResponseType.JSON, request.getResponseType());

    }

    @Test
    public void setIntersectionTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setIntersections(true);
        Assert.assertTrue(request.getIntersections());
    }

    @Test
    public void setRangeUnitsTest() throws ParameterValueException {
        IsochronesRequest request = new IsochronesRequest();
        request.setRangeUnit(APIEnums.Units.forValue("km"));
        Assert.assertEquals(APIEnums.Units.KILOMETRES, request.getRangeUnit());
    }

    @Test
    public void getAttributesTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertNull(request.getAttributes());
    }

    @Test
    public void setAttributesTest() throws ParameterValueException {
        IsochronesRequest request = new IsochronesRequest();
        IsochronesRequestEnums.Attributes[] attributes = new IsochronesRequestEnums.Attributes[1];
        attributes[0] = IsochronesRequestEnums.Attributes.forValue("reachfactor");
        request.setAttributes(attributes);
        Assert.assertNotNull(request.getAttributes());
        Assert.assertEquals(IsochronesRequestEnums.Attributes.REACH_FACTOR, request.getAttributes()[0]);
    }

    @Test
    public void hasAttributesTest() throws ParameterValueException {
        IsochronesRequest request = new IsochronesRequest();
        IsochronesRequestEnums.Attributes[] attributes = new IsochronesRequestEnums.Attributes[1];
        attributes[0] = IsochronesRequestEnums.Attributes.forValue("reachfactor");
        Assert.assertFalse(request.hasAttributes());
        request.setAttributes(attributes);
        Assert.assertTrue(request.hasAttributes());
    }

    @Test
    public void getLocationTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertEquals(Double[][].class, request.getLocations().getClass());
    }

    @Test
    public void setLocationTest() {
        IsochronesRequest request = new IsochronesRequest();
        Double[][] double_array = {{1.0, 2.0}, {1.0, 3.0}};
        request.setLocations(double_array);
        Assert.assertArrayEquals(double_array, request.getLocations());
    }

    @Test
    public void setLocationTypeTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setLocationType(IsochronesRequestEnums.LocationType.DESTINATION);
        Assert.assertEquals(IsochronesRequestEnums.LocationType.DESTINATION, request.getLocationType());
    }

    @Test
    public void getProfileTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertNull(request.getProfile());
    }

    @Test
    public void setProfileTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setProfile(APIEnums.Profile.DRIVING_CAR);
        Assert.assertEquals(APIEnums.Profile.DRIVING_CAR, request.getProfile());
    }

    @Test
    public void getIsochronesOptionsTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertNull(request.getIsochronesOptions());
    }

    @Test
    public void setIsochronesOptionsTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setIsochronesOptions(new RouteRequestOptions());
        Assert.assertEquals(RouteRequestOptions.class, request.getIsochronesOptions().getClass());

    }

    @Test
    public void hasIsochronesOptionsTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setIsochronesOptions(new RouteRequestOptions());
        Assert.assertTrue(request.hasOptions());
    }

    @Test
    public void getRangeTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertNull(request.getRange());
    }

    @Test
    public void setRangeTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setRange(new ArrayList<>());
        Assert.assertNotNull(request.getRange());
    }

    @Test
    public void setRangeTypeTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setRangeType(IsochronesRequestEnums.RangeType.DISTANCE);
        Assert.assertEquals(IsochronesRequestEnums.RangeType.DISTANCE, request.getRangeType());
    }

    @Test
    public void getIntervalTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertNull(request.getInterval());
    }

    @Test
    public void setIntervalTest() {
        IsochronesRequest request = new IsochronesRequest();
        request.setInterval(new Double("0.0"));
        Assert.assertEquals(new Double("0.0"), request.getInterval());
    }

    @Test
    public void detailedOptionsTest() {
        IsochronesRequest request = new IsochronesRequest();
        Assert.assertFalse(request.hasOptions());
        RouteRequestOptions opts = new RouteRequestOptions();
        request.setIsochronesOptions(opts);
        Assert.assertTrue(request.hasOptions());
    }
}