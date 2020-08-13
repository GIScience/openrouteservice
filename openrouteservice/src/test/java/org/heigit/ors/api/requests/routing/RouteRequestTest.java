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

import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.exceptions.ParameterValueException;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import org.junit.rules.ExpectedException;

public class RouteRequestTest {
    RouteRequest request;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() throws ParameterValueException {
        request = new RouteRequest(new Double[][] {new Double[] {1.0,1.0}, new Double[] {2.0,2.0}});
    }

    @Test (expected = ParameterValueException.class)
    public void expectErrorCoordinatesArrayTooFew() throws ParameterValueException {
        request = new RouteRequest(new Double[][] {new Double[] {1.0,1.0}});
    }

    @Test (expected = ParameterValueException.class)
    public void expectErrorCoordinatesArraySingleTooFew() throws ParameterValueException {
        request = new RouteRequest(new Double[][] {new Double[] {1.0}, new Double[] {1.0}});
    }

    @Test (expected = ParameterValueException.class)
    public void expectErrorCoordinatesArraySingleTooMany() throws ParameterValueException {
        request = new RouteRequest(new Double[][] {new Double[] {1.0, 1.0, 1.0}, new Double[] {1.0, 1.0, 1.0}});
    }

    @Test (expected = ParameterValueException.class)
    public void expectErrorNullStart() throws ParameterValueException {
        request = new RouteRequest(null, new Coordinate());
    }
    @Test (expected = ParameterValueException.class)
    public void expectErrorNullEnd() throws ParameterValueException {
        request = new RouteRequest(new Coordinate(), null);
    }

    @Test
    public void testHasIncludeRoundaboutExitInfo() {
        request.setIncludeRoundaboutExitInfo(true);
        Assert.assertTrue(request.hasIncludeRoundaboutExitInfo());
    }

    @Test
    public void testHasAttributes() {
        request.setAttributes(new APIEnums.Attributes[] {APIEnums.Attributes.AVERAGE_SPEED});
        Assert.assertTrue(request.hasAttributes());
    }

    @Test
    public void testHasMaximumSearchRadii() {
        request.setMaximumSearchRadii(new Double[] { 1.0 });
        Assert.assertTrue(request.hasMaximumSearchRadii());
    }

    @Test
    public void testHasBearings() {
        request.setBearings(new Double[][] {new Double[] {0.0, 90.0}});
        Assert.assertTrue(request.hasBearings());
    }

    @Test
    public void testHasUseElevation() {
        request.setUseElevation(true);
        Assert.assertTrue(request.hasUseElevation());
    }

    @Test
    public void testHasRouteOptions() {
        request.setRouteOptions(new RouteRequestOptions());
        Assert.assertTrue(request.hasRouteOptions());
    }

    @Test
    public void testHasUseContractionHierarchies() {
        request.setUseContractionHierarchies(true);
        Assert.assertTrue(request.hasUseContractionHierarchies());
    }

    @Test
    public void testHasExtraInfo() {
        request.setExtraInfo(new APIEnums.ExtraInfo[] { APIEnums.ExtraInfo.SURFACE });
        Assert.assertTrue(request.hasExtraInfo());
    }

    @Test
    public void testHasSuppressWarnings() {
        request.setSuppressWarnings(true);
        Assert.assertTrue(request.hasSuppressWarnings());
    }

    @Test
    public void testHasSkipSegments() {
        List<Integer> testSegments = new ArrayList<>();
        testSegments.add(0, 1);
        Assert.assertFalse(request.hasSkipSegments());
        request.setSkipSegments(testSegments);
        Assert.assertTrue(request.hasSkipSegments());
    }

    @Test
    public void testHasAlternativeRoutes() {
        RouteRequestAlternativeRoutes ar = new RouteRequestAlternativeRoutes();
        Assert.assertFalse(request.hasAlternativeRoutes());
        request.setAlternativeRoutes(ar);
        Assert.assertTrue(request.hasAlternativeRoutes());
    }

    @Test
    public void testHasUserWeights() throws ParameterValueException {
      JSONObject wc = new JSONObject();
      Assert.assertFalse(request.hasUserWeights());
      request.setUserWeights(wc);
      Assert.assertTrue(request.hasUserWeights());
    }

    @Test
    public void testSetLargeUserWeights() throws ParameterValueException {
        JSONObject wc = new JSONObject();
        StringBuilder foo = new StringBuilder(50*100000); // this string should have a size of 2*50*100,000 (~9.5 MiB)
        for (int i = 0; i < 100000; i++) {
            foo.append("fooooooooooooooooooooooooooooooooooooooooooooooooo"); // 50 characters
        }
        for (int i = 0; i < 2; i++) {
          wc.put("foo" + i, foo);
        }
        thrown.expect(ParameterValueException.class);
        thrown.expectMessage("Parameter value too large.");
        request.setUserWeights(wc);
    }
}
