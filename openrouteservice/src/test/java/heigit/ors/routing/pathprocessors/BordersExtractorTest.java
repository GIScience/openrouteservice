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
package heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.DAType;
import com.graphhopper.storage.GHDirectory;
import com.graphhopper.util.Helper;
import heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BordersExtractorTest {
    private final FlagEncoder encoder = new EncodingManager(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.CAR_ORS, 4).getEncoder(FlagEncoderNames.CAR_ORS);
    private final BordersGraphStorage _graphstorage;

    public BordersExtractorTest() {
        // Initialise a graph storage with dummy data
        _graphstorage = new BordersGraphStorage();
        _graphstorage.init(null, new GHDirectory("", DAType.RAM_STORE));
        _graphstorage.create(3);

        // (edgeId, borderType, startCountry, endCountry)

        _graphstorage.setEdgeValue(1, BordersGraphStorage.CONTROLLED_BORDER, (short)1, (short)2);
        _graphstorage.setEdgeValue(2, BordersGraphStorage.OPEN_BORDER, (short)3, (short)4);
        _graphstorage.setEdgeValue(3, BordersGraphStorage.NO_BORDER, (short)5, (short)5);
    }

    private VirtualEdgeIteratorState generateEdge(int id) {
        return new VirtualEdgeIteratorState(0, id, id, 1, 2, 10,
                encoder.setProperties(10, true, true), "test", Helper.createPointList(51,0,51,1));
    }

    @Test
    public void TestDetectAnyBorder() {
        VirtualEdgeIteratorState ve1 = generateEdge(1);
        VirtualEdgeIteratorState ve2 = generateEdge(2);
        VirtualEdgeIteratorState ve3 = generateEdge(3);

        BordersExtractor be = new BordersExtractor(_graphstorage, null, new int[0]);
        assertEquals(true, be.isBorder(1));
        assertEquals(true, be.isBorder(2));
        assertEquals(false, be.isBorder(3));
    }

    @Test
    public void TestDetectControlledBorder() {
        VirtualEdgeIteratorState ve1 = generateEdge(1);
        VirtualEdgeIteratorState ve2 = generateEdge(2);
        VirtualEdgeIteratorState ve3 = generateEdge(3);

        BordersExtractor be = new BordersExtractor(_graphstorage, null, new int[0]);
        assertEquals(true, be.isControlledBorder(1));
        assertEquals(false, be.isControlledBorder(2));
        assertEquals(false, be.isControlledBorder(3));
    }

    @Test
    public void TestDetectOpenBorder() {
        VirtualEdgeIteratorState ve1 = generateEdge(1);
        VirtualEdgeIteratorState ve2 = generateEdge(2);
        VirtualEdgeIteratorState ve3 = generateEdge(3);

        BordersExtractor be = new BordersExtractor(_graphstorage, null, new int[0]);
        assertEquals(false, be.isOpenBorder(1));
        assertEquals(true, be.isOpenBorder(2));
        assertEquals(false, be.isOpenBorder(3));
    }

    @Test
    public void TestAvoidCountry() {
        VirtualEdgeIteratorState ve1 = generateEdge(1);
        VirtualEdgeIteratorState ve2 = generateEdge(2);
        VirtualEdgeIteratorState ve3 = generateEdge(3);

        BordersExtractor be = new BordersExtractor(_graphstorage, null, new int[] {2, 4});
        assertEquals(true, be.restrictedCountry(1));
        assertEquals(true, be.restrictedCountry(2));
        assertEquals(false, be.restrictedCountry(3));
    }
}