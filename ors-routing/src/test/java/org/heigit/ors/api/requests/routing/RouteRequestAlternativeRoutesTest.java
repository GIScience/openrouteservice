/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api.requests.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteRequestAlternativeRoutesTest {
    RouteRequestAlternativeRoutes ar;

    @BeforeEach
    void setup() {
        ar = new RouteRequestAlternativeRoutes();
    }

    @Test
    void testTargetCount() {
        assertFalse(ar.hasTargetCount());
        ar.setTargetCount(2);
        assertTrue(ar.hasTargetCount());
        assertEquals((Integer) 2, ar.getTargetCount());
    }

    @Test
    void testWeightFactor() {
        assertFalse(ar.hasWeightFactor());
        ar.setWeightFactor(1.9);
        assertTrue(ar.hasWeightFactor());
        assertEquals((Double) 1.9, ar.getWeightFactor());
    }

    @Test
    void testShareFactor() {
        assertFalse(ar.hasShareFactor());
        ar.setShareFactor(0.7);
        assertTrue(ar.hasShareFactor());
        assertEquals((Double) 0.7, ar.getShareFactor());
    }
}
