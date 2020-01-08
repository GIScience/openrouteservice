package org.heigit.ors.api.requests.routing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;

public class RouteRequestRoundTripOptionsTest {
    RouteRequestRoundTripOptions options;

    @Before
    public void init() {
        options = new RouteRequestRoundTripOptions();
    }

    @Test
    public void testSetLength() {
        Assert.assertFalse(options.hasLength());
        options.setLength(123.4f);
        Assert.assertTrue(options.hasLength());
        Assert.assertEquals((Float)123.4f, options.getLength());
    }

    @Test
    public void testSetPoints() {
        Assert.assertFalse(options.hasPoints());
        options.setPoints(12);
        Assert.assertTrue(options.hasPoints());
        Assert.assertEquals((Integer) 12, options.getPoints());
    }

    @Test
    public void testSetSeed() {
        Assert.assertFalse(options.hasSeed());
        options.setSeed(1234567890l);
        Assert.assertTrue(options.hasSeed());
        Assert.assertEquals((Long) 1234567890l, options.getSeed());
    }
}
