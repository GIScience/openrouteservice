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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.DAType;
import com.graphhopper.storage.GHDirectory;
import com.graphhopper.util.Helper;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AvoidBordersEdgeFilterTest {

    private final FlagEncoder encoder = new EncodingManager(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.CAR_ORS, 4).getEncoder(FlagEncoderNames.CAR_ORS);
    private final BordersGraphStorage _graphStorage;

    private final RouteSearchParameters _searchParams;

    public AvoidBordersEdgeFilterTest() {
        // Initialise a graph storage with dummy data
        _graphStorage = new BordersGraphStorage();
        _graphStorage.init(null, new GHDirectory("", DAType.RAM_STORE));
        _graphStorage.create(3);


        // (edgeId, borderType, startCountry, endCountry)

        _graphStorage.setEdgeValue(1, BordersGraphStorage.CONTROLLED_BORDER, (short)1, (short)2);
        _graphStorage.setEdgeValue(2, BordersGraphStorage.OPEN_BORDER, (short)3, (short)4);
        _graphStorage.setEdgeValue(3, BordersGraphStorage.NO_BORDER, (short)5, (short)5);

        _searchParams = new RouteSearchParameters();

    }

    private VirtualEdgeIteratorState generateEdge(int id) {
        return new VirtualEdgeIteratorState(0, id, id, 1, 2, 10,
                encoder.setProperties(10, true, true), "test", Helper.createPointList(51,0,51,1));
    }

    @Test
    public void TestAvoidAllBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.ALL);
        _searchParams.setAvoidCountries(new int[] {});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, _graphStorage);

        VirtualEdgeIteratorState ve1 = generateEdge(1);
        VirtualEdgeIteratorState ve2 = generateEdge(2);
        VirtualEdgeIteratorState ve3 = generateEdge(3);

        assertFalse(filter.accept(ve1));
        assertFalse(filter.accept(ve2));
        assertTrue(filter.accept(ve3));

    }

    @Test
    public void TestAvoidControlledBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        _searchParams.setAvoidCountries(new int[] {});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, _graphStorage);

        VirtualEdgeIteratorState ve1 = generateEdge(1);
        VirtualEdgeIteratorState ve2 = generateEdge(2);
        VirtualEdgeIteratorState ve3 = generateEdge(3);

        assertFalse(filter.accept(ve1));
        assertTrue(filter.accept(ve2));
        assertTrue(filter.accept(ve3));
    }

    @Test
    public void TestAvoidNoBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.NONE);
        _searchParams.setAvoidCountries(new int[] {});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, _graphStorage);

        VirtualEdgeIteratorState ve1 = generateEdge(1);
        VirtualEdgeIteratorState ve2 = generateEdge(2);
        VirtualEdgeIteratorState ve3 = generateEdge(3);

        assertTrue(filter.accept(ve1));
        assertTrue(filter.accept(ve2));
        assertTrue(filter.accept(ve3));
    }

    @Test
    public void TestAvoidSpecificBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.NONE);
        _searchParams.setAvoidCountries(new int[] {1, 5});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, _graphStorage);

        VirtualEdgeIteratorState ve1 = generateEdge(1);
        VirtualEdgeIteratorState ve2 = generateEdge(2);
        VirtualEdgeIteratorState ve3 = generateEdge(3);

        assertFalse(filter.accept(ve1));
        assertTrue(filter.accept(ve2));
        assertFalse(filter.accept(ve3));
    }

    @Test
    public void TestAvoidSpecificAndControlledBorders() {
        _searchParams.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        _searchParams.setAvoidCountries(new int[] {3});

        AvoidBordersEdgeFilter filter = new AvoidBordersEdgeFilter(_searchParams, _graphStorage);

        VirtualEdgeIteratorState ve1 = generateEdge(1);
        VirtualEdgeIteratorState ve2 = generateEdge(2);
        VirtualEdgeIteratorState ve3 = generateEdge(3);

        assertFalse(filter.accept(ve1));
        assertFalse(filter.accept(ve2));
        assertTrue(filter.accept(ve3));
    }
}
