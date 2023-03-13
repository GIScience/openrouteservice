package org.heigit.ors.api.requests.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteRequestRoundTripOptionsTest {
    RouteRequestRoundTripOptions options;

    @BeforeEach
    void init() {
        options = new RouteRequestRoundTripOptions();
    }

    @Test
    void testSetLength() {
        assertFalse(options.hasLength());
        options.setLength(123.4f);
        assertTrue(options.hasLength());
        assertEquals((Float)123.4f, options.getLength());
    }

    @Test
    void testSetPoints() {
        assertFalse(options.hasPoints());
        options.setPoints(12);
        assertTrue(options.hasPoints());
        assertEquals((Integer) 12, options.getPoints());
    }

    @Test
    void testSetSeed() {
        assertFalse(options.hasSeed());
        options.setSeed(1234567890l);
        assertTrue(options.hasSeed());
        assertEquals((Long) 1234567890l, options.getSeed());
    }
}
