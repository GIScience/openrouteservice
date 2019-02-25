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

package heigit.ors.api.requests.routing;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.exceptions.ParameterValueException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RouteRequestTest {
    RouteRequest request;

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
}
