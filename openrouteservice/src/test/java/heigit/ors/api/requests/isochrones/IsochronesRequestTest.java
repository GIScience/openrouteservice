package heigit.ors.api.requests.isochrones;

import heigit.ors.isochrones.Isochrone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IsochronesRequestTest {

    @Before
    public void setUp() throws Exception {
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
        request.setAttributes(new IsochronesRequestEnums.Attributes[] { IsochronesRequestEnums.Attributes.AREA});
        Assert.assertTrue(request.hasAttributes());
        Assert.assertEquals(request.getAttributes().length, 1);
        Assert.assertEquals(IsochronesRequestEnums.Attributes.AREA, request.getAttributes()[0]);
    }
}