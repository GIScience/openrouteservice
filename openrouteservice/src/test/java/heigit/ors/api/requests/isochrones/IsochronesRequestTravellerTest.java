package heigit.ors.api.requests.isochrones;

import heigit.ors.api.requests.routing.RouteRequestOptions;
import heigit.ors.exceptions.ParameterValueException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IsochronesRequestTravellerTest {
    IsochronesRequestTraveller traveller;

    @Before
    public void setUp() throws Exception {
        traveller = new IsochronesRequestTraveller(new Double[]{1.0,2.0});
    }

    @Test (expected = ParameterValueException.class)
    public void tooSmallLocationTest() throws ParameterValueException {
        new IsochronesRequestTraveller(new Double[]{1.0});
    }

    @Test (expected = ParameterValueException.class)
    public void tooLargeLocationTest() throws ParameterValueException {
        new IsochronesRequestTraveller(new Double[]{1.0, 1.0, 1.0});
    }

    @Test
    public void optionsTest() {
        Assert.assertFalse(traveller.hasIsochronesOptions());
        RouteRequestOptions opts = new RouteRequestOptions();
        opts.setMaximumSpeed(120.0);
        traveller.setIsochronesOptions(opts);
        Assert.assertTrue(traveller.hasIsochronesOptions());
        Assert.assertEquals(120.0, traveller.getIsochronesOptions().getMaximumSpeed(), 0.0);
    }

    @Test
    public void idTest() {
        Assert.assertFalse(traveller.hasId());
        traveller.setId("1");
        Assert.assertTrue(traveller.hasId());
        Assert.assertEquals("1", traveller.getId());
    }
}