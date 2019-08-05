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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RouteRequestAlternativeRoutesTest {
    RouteRequestAlternativeRoutes ar;

    @Before
    public void setup() {
        ar = new RouteRequestAlternativeRoutes();
    }

    @Test
    public void testTargetCount() {
        Assert.assertFalse(ar.hasTargetCount());
        ar.setTargetCount(2);
        Assert.assertTrue(ar.hasTargetCount());
        Assert.assertEquals((Integer) 2, ar.getTargetCount());
    }

    @Test
    public void testWeightFactor() {
        Assert.assertFalse(ar.hasWeightFactor());
        ar.setWeightFactor(1.9);
        Assert.assertTrue(ar.hasWeightFactor());
        Assert.assertEquals((Double) 1.9, ar.getWeightFactor());
    }

    @Test
    public void testShareFactor() {
        Assert.assertFalse(ar.hasShareFactor());
        ar.setShareFactor(0.7);
        Assert.assertTrue(ar.hasShareFactor());
        Assert.assertEquals((Double) 0.7, ar.getShareFactor());
    }
}
