package org.heigit.ors.api.requests.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestProfileParamsTest {
    RequestProfileParams params;

    public RequestProfileParamsTest() {
        init();
    }

    @BeforeEach
    void init() {
        params = new RequestProfileParams();
    }

    @Test
    void testWeightings() {
        RequestProfileParamsWeightings weightings = new RequestProfileParamsWeightings();

        assertFalse(params.hasWeightings());

        params.setWeightings(weightings);

        assertEquals(weightings, params.getWeightings());
        assertTrue(params.hasWeightings());
    }

    @Test
    void testRestrictions() {
        RequestProfileParamsRestrictions restrictions = new RequestProfileParamsRestrictions();

        assertFalse(params.hasRestrictions());

        params.setRestrictions(restrictions);

        assertEquals(restrictions, params.getRestrictions());
        assertTrue(params.hasRestrictions());
    }

    @Test
    void testRestPenalty() {
        assertFalse(params.hasRestPenalty());

        params.setRestPenalty(1.5);

        assertEquals(1.5, params.getRestPenalty());
        assertTrue(params.hasRestPenalty());
    }

    @Test
    void testRestPenaltyNullCountsAsNotSet() {
        params.setRestPenalty(null);

        assertFalse(params.hasRestPenalty());
    }
}
