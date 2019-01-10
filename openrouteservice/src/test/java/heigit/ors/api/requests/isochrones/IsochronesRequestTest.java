package heigit.ors.api.requests.isochrones;

import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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