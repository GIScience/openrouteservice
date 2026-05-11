/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.ev.Border;
import com.graphhopper.routing.ev.Country;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvoidBordersEdgeFilterTest {
    private final PMap properties = new PMap();
    private final EncodingManager encodingManager = EncodingManager.create(new ORSDefaultFlagEncoderFactory().createFlagEncoder(FlagEncoderNames.CAR_ORS, properties));
    private final RouteSearchParameters _searchParams;

    public AvoidBordersEdgeFilterTest() {
        encodingManager.addEncodedValue(Border.create(), false);
        encodingManager.addEncodedValue(Country.create(), false);

        CountryBordersReader countryBordersReader = new CountryBordersReader();
        countryBordersReader.setIsoCodes(Map.of("XXA", (short) 1, "XXB", (short) 2, "XXC", (short) 3));

        _searchParams = new RouteSearchParameters();
    }

    private VirtualEdgeIteratorState generateEdge(int id, Border border, Country country) {
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        encodingManager.getEnumEncodedValue(Border.KEY, Border.class).setEnum(false, edgeFlags, border);
        encodingManager.getEnumEncodedValue(Country.KEY, Country.class).setEnum(false, edgeFlags, country);
        int edgeKey = GHUtility.createEdgeKey(id, false);
        return new VirtualEdgeIteratorState(0, edgeKey, 1, 2, 10,
                edgeFlags, "test", Helper.createPointList(51, 0, 51, 1), false);
    }

    @Test
    void TestAvoidAllBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.ALL);
        _searchParams.setAvoidCountries(new int[]{});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, encodingManager);

        VirtualEdgeIteratorState ve1 = generateEdge(1, Border.CONTROLLED, Country.XXA);
        VirtualEdgeIteratorState ve2 = generateEdge(2, Border.OPEN, Country.XXB);
        VirtualEdgeIteratorState ve3 = generateEdge(3, Border.NONE, Country.XXC);

        assertFalse(filter.accept(ve1));
        assertFalse(filter.accept(ve2));
        assertTrue(filter.accept(ve3));
    }

    @Test
    void TestAvoidControlledBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        _searchParams.setAvoidCountries(new int[]{});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, encodingManager);

        VirtualEdgeIteratorState ve1 = generateEdge(1, Border.CONTROLLED, Country.XXA);
        VirtualEdgeIteratorState ve2 = generateEdge(2, Border.OPEN, Country.XXB);
        VirtualEdgeIteratorState ve3 = generateEdge(3, Border.NONE, Country.XXC);

        assertFalse(filter.accept(ve1));
        assertTrue(filter.accept(ve2));
        assertTrue(filter.accept(ve3));
    }

    @Test
    void TestAvoidNoBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.NONE);
        _searchParams.setAvoidCountries(new int[]{});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, encodingManager);

        VirtualEdgeIteratorState ve1 = generateEdge(1, Border.CONTROLLED, Country.XXA);
        VirtualEdgeIteratorState ve2 = generateEdge(2, Border.OPEN, Country.XXB);
        VirtualEdgeIteratorState ve3 = generateEdge(3, Border.NONE, Country.XXC);

        assertTrue(filter.accept(ve1));
        assertTrue(filter.accept(ve2));
        assertTrue(filter.accept(ve3));
    }

    @Test
    void TestAvoidSpecificBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.NONE);
        _searchParams.setAvoidCountries(new int[]{1, 3});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, encodingManager);

        VirtualEdgeIteratorState ve1 = generateEdge(1, Border.CONTROLLED, Country.XXA);
        VirtualEdgeIteratorState ve2 = generateEdge(2, Border.OPEN, Country.XXB);
        VirtualEdgeIteratorState ve3 = generateEdge(3, Border.NONE, Country.XXC);

        assertFalse(filter.accept(ve1));
        assertTrue(filter.accept(ve2));
        assertFalse(filter.accept(ve3));
    }

    @Test
    void TestAvoidSpecificAndControlledBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        _searchParams.setAvoidCountries(new int[]{2});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, encodingManager);

        VirtualEdgeIteratorState ve1 = generateEdge(1, Border.CONTROLLED, Country.XXA);
        VirtualEdgeIteratorState ve2 = generateEdge(2, Border.OPEN, Country.XXB);
        VirtualEdgeIteratorState ve3 = generateEdge(3, Border.NONE, Country.XXC);

        assertFalse(filter.accept(ve1));
        assertFalse(filter.accept(ve2));
        assertTrue(filter.accept(ve3));
    }
}
