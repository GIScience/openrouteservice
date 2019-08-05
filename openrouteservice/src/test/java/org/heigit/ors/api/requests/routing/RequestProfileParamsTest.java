package heigit.ors.api.requests.routing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RequestProfileParamsTest {
    RequestProfileParams params;

    public RequestProfileParamsTest() {
        init();
    }

    @Before
    public void init() {
        params = new RequestProfileParams();
    }

    @Test
    public void testWeightings() {
        RequestProfileParamsWeightings weightings = new RequestProfileParamsWeightings();

        Assert.assertFalse(params.hasWeightings());

        params.setWeightings(weightings);

        Assert.assertEquals(weightings, params.getWeightings());
        Assert.assertTrue(params.hasWeightings());
    }

    @Test
    public void testRestrictions() {
        RequestProfileParamsRestrictions restrictions = new RequestProfileParamsRestrictions();

        Assert.assertFalse(params.hasRestrictions());

        params.setRestrictions(restrictions);

        Assert.assertEquals(restrictions, params.getRestrictions());
        Assert.assertTrue(params.hasRestrictions());
    }
}
