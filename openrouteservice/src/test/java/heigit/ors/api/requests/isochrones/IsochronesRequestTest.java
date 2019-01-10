package heigit.ors.api.requests.isochrones;

import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static heigit.ors.util.HelperFunctions.fakeArrayLocations;

public class IsochronesRequestTest {
    IsochronesRequest request;

    @Before
    public void setUp() {
        request = new IsochronesRequest();


    }

    @Test
    public void idTest() {
        Assert.assertFalse(request.hasId());
        request.setId("1");
        Assert.assertTrue(request.hasId());
        Assert.assertEquals("1", request.getId());
    }

    @Test
    public void smoothingTest() {
        Assert.assertFalse(request.hasSmoothing());
        request.setSmoothing(1.5);
        Assert.assertTrue(request.hasSmoothing());
        Assert.assertEquals(1.5, request.getSmoothing(), 0.0);
    }

    @Test
    public void attributesTest() {
        Assert.assertFalse(request.hasAttributes());
        request.setAttributes(new IsochronesRequestEnums.Attributes[]{IsochronesRequestEnums.Attributes.AREA});
        Assert.assertTrue(request.hasAttributes());
        Assert.assertEquals(request.getAttributes().length, 1);
        Assert.assertEquals(IsochronesRequestEnums.Attributes.AREA, request.getAttributes()[0]);
    }

    @Test
    public void getId() {
    }

    @Test
    public void setId() {
    }

    @Test
    public void hasId() {
    }

    @Test
    public void getAreaUnit() {
    }

    @Test
    public void setAreaUnit() {
    }

    @Test
    public void getSmoothing() {
    }

    @Test
    public void setSmoothing() {
    }

    @Test
    public void hasSmoothing() {
    }

    @Test
    public void getResponseType() {
    }

    @Test
    public void setResponseType() {
    }

    @Test
    public void getIntersections() {
    }

    @Test
    public void setIntersection() {
    }

    @Test
    public void getRangeUnits() {
    }

    @Test
    public void setRangeUnits() {
    }

    @Test
    public void getAttributes() {
    }

    @Test
    public void setAttributes() {
    }

    @Test
    public void hasAttributes() {
    }

    @Test
    public void getCalcMethod() {
    }

    @Test
    public void setCalcMethod() {
    }

    @Test
    public void getLocation() {
    }

    @Test
    public void setLocation() {
    }

    @Test
    public void getLocationType() {
    }

    @Test
    public void setLocationType() {
    }

    @Test
    public void getProfile() {
    }

    @Test
    public void setProfile() {
    }

    @Test
    public void getIsochronesOptions() {
    }

    @Test
    public void setIsochronesOptions() {
    }

    @Test
    public void hasIsochronesOptions() {
    }

    @Test
    public void getRange() {
    }

    @Test
    public void setRange() {
    }

    @Test
    public void getRangeType() {
    }

    @Test
    public void setRangeType() {
    }

    @Test
    public void getInterval() {
    }

    @Test
    public void setInterval() {
    }


    @Test(expected = ParameterValueException.class)
    public void tooSmallLocationTest() throws ParameterValueException {
        Double[][] double_array = {{1.0}, {1.0, 3.0}};
        new IsochronesRequest(double_array);
    }

    @Test(expected = ParameterValueException.class)
    public void exceedingLocationMaximumTest() throws ParameterValueException {
        Double[][] exceedingLocationsMaximumCoords = fakeArrayLocations(IsochronesServiceSettings.getMaximumLocations() + 1, 2);
        new IsochronesRequest(exceedingLocationsMaximumCoords);
    }


    @Test(expected = ParameterValueException.class)
    public void tooLargeLocationTest() throws ParameterValueException {
        Double[][] exceedingLocationsMaximumCoords = {{1.0, 3.0, 4.0}, {1.0, 3.0}};
        new IsochronesRequest(exceedingLocationsMaximumCoords);
    }

}