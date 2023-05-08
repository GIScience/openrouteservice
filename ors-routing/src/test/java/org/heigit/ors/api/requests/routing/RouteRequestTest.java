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

import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.exceptions.ParameterValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteRequestTest {
    RouteRequest request;

    @BeforeEach
    void setup() throws ParameterValueException {
        request = new RouteRequest(new Double[][] {new Double[] {1.0,1.0}, new Double[] {2.0,2.0}});
    }

    @Test
    void expectErrorCoordinatesArrayTooFew() throws ParameterValueException {
        assertThrows(ParameterValueException.class, () -> {
            request = new RouteRequest(new Double[][]{new Double[]{1.0, 1.0}});
        });
    }

    @Test
    void expectErrorCoordinatesArraySingleTooFew() throws ParameterValueException {
        assertThrows(ParameterValueException.class, () -> {
            request = new RouteRequest(new Double[][]{new Double[]{1.0}, new Double[]{1.0}});
        });
    }

    @Test
    void expectErrorCoordinatesArraySingleTooMany() throws ParameterValueException {
        assertThrows(ParameterValueException.class, () -> {
            request = new RouteRequest(new Double[][]{new Double[]{1.0, 1.0, 1.0}, new Double[]{1.0, 1.0, 1.0}});
        });
    }

    @Test
    void expectErrorNullStart() throws ParameterValueException {
        assertThrows(ParameterValueException.class, () -> {
            request = new RouteRequest(null, new Coordinate());
        });
    }

    @Test
    void expectErrorNullEnd() throws ParameterValueException {
        assertThrows(ParameterValueException.class, () -> {
            request = new RouteRequest(new Coordinate(), null);
        });
    }

    @Test
    void testHasIncludeRoundaboutExitInfo() {
        request.setIncludeRoundaboutExitInfo(true);
        assertTrue(request.hasIncludeRoundaboutExitInfo());
    }

    @Test
    void testHasAttributes() {
        request.setAttributes(new APIEnums.Attributes[] {APIEnums.Attributes.AVERAGE_SPEED});
        assertTrue(request.hasAttributes());
    }

    @Test
    void testHasMaximumSearchRadii() {
        request.setMaximumSearchRadii(new Double[] { 1.0 });
        assertTrue(request.hasMaximumSearchRadii());
    }

    @Test
    void testHasBearings() {
        request.setBearings(new Double[][] {new Double[] {0.0, 90.0}});
        assertTrue(request.hasBearings());
    }

    @Test
    void testHasUseElevation() {
        request.setUseElevation(true);
        assertTrue(request.hasUseElevation());
    }

    @Test
    void testHasRouteOptions() {
        request.setRouteOptions(new RouteRequestOptions());
        assertTrue(request.hasRouteOptions());
    }

    @Test
    void testHasUseContractionHierarchies() {
        request.setUseContractionHierarchies(true);
        assertTrue(request.hasUseContractionHierarchies());
    }

    @Test
    void testHasExtraInfo() {
        request.setExtraInfo(new APIEnums.ExtraInfo[] { APIEnums.ExtraInfo.SURFACE });
        assertTrue(request.hasExtraInfo());
    }

    @Test
    void testHasSuppressWarnings() {
        request.setSuppressWarnings(true);
        assertTrue(request.hasSuppressWarnings());
    }

    @Test
    void testHasSkipSegments() {
        List<Integer> testSegments = new ArrayList<>();
        testSegments.add(0, 1);
        assertFalse(request.hasSkipSegments());
        request.setSkipSegments(testSegments);
        assertTrue(request.hasSkipSegments());
    }

    @Test
    void testHasAlternativeRoutes() {
        RouteRequestAlternativeRoutes ar = new RouteRequestAlternativeRoutes();
        assertFalse(request.hasAlternativeRoutes());
        request.setAlternativeRoutes(ar);
        assertTrue(request.hasAlternativeRoutes());
    }
}
