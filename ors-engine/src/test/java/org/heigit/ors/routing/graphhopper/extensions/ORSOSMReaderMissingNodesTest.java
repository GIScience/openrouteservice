/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.OSMReaderConfig;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.GHPoint;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ORSOSMReaderMissingNodesTest {
    private static final double[] LATITUDES = {49.400, 49.401, 49.402, 49.403, 49.404};
    private static final double[] LONGITUDES = {8.600, 8.601, 8.604, 8.601, 8.600};

    @TempDir
    Path directory;

    private GraphHopperStorage storage;
    private Map<String, Object> wayTags;

    @AfterEach
    void closeStorage() {
        if (storage != null)
            storage.close();
    }

    @ParameterizedTest
    @CsvSource({
            "ferry, 1", "ferry, 3", "ferry, 5",
            "shuttle_train, 1", "shuttle_train, 3", "shuttle_train, 5",
            "road, 1", "road, 3", "road, 5"
    })
    void missingCoordinatesDoNotAbortImport(String route, int missingNode) throws IOException {
        readWay(route, missingNode);

        assertNotNull(wayTags);
        assertFalse(wayTags.containsKey("exact_distance"));
        assertFalse(wayTags.containsKey("exact_center"));
        assertEquals(missingNode == 3, wayTags.containsKey("estimated_way_distance"));
        assertEquals(missingNode == 3 ? 2 : 1, storage.getEdges());

        if (missingNode == 3) {
            var edges = storage.getAllEdges();
            while (edges.next()) {
                double fromLat = storage.getNodeAccess().getLat(edges.getBaseNode());
                double toLat = storage.getNodeAccess().getLat(edges.getAdjNode());
                assertTrue(fromLat < LATITUDES[2] && toLat < LATITUDES[2]
                        || fromLat > LATITUDES[2] && toLat > LATITUDES[2],
                        "An edge must not connect the two sides of a missing node");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ferry", "shuttle_train", "road"})
    void completeWaysKeepTheirDistanceTags(String route) throws IOException {
        readWay(route, 0);

        var distanceCalc = DistanceCalcEarth.DIST_EARTH;
        double estimatedDistance = distanceCalc.calcDist(LATITUDES[0], LONGITUDES[0], LATITUDES[4], LONGITUDES[4]);
        assertEquals(estimatedDistance, (Double) wayTags.get("estimated_way_distance"), 1e-6);
        assertEquals(1, storage.getEdges());

        if (!route.equals("road")) {
            double exactDistance = 0;
            for (int i = 1; i < LATITUDES.length; i++)
                exactDistance += distanceCalc.calcDist(LATITUDES[i - 1], LONGITUDES[i - 1], LATITUDES[i], LONGITUDES[i]);
            assertEquals(exactDistance, (Double) wayTags.get("exact_distance"), 0.1);
            assertTrue(exactDistance > estimatedDistance);
            GHPoint center = (GHPoint) wayTags.get("exact_center");
            assertEquals(49.402, center.getLat(), 1e-6);
            assertEquals(8.6012, center.getLon(), 1e-6);
        } else {
            assertFalse(wayTags.containsKey("exact_distance"));
            assertFalse(wayTags.containsKey("exact_center"));
        }
    }

    private void readWay(String route, int missingNode) throws IOException {
        StringBuilder xml = new StringBuilder("<osm version=\"0.6\">\n");
        for (int i = 0; i < LATITUDES.length; i++) {
            if (i + 1 != missingNode)
                xml.append("<node id=\"").append(i + 1).append("\" lat=\"").append(LATITUDES[i])
                        .append("\" lon=\"").append(LONGITUDES[i]).append("\"/>\n");
        }
        xml.append("<way id=\"10\"><nd ref=\"1\"/><nd ref=\"2\"/><nd ref=\"3\"/><nd ref=\"4\"/><nd ref=\"5\"/>");
        if (route.equals("road"))
            xml.append("<tag k=\"highway\" v=\"residential\"/>");
        else
            xml.append("<tag k=\"route\" v=\"").append(route).append("\"/><tag k=\"motorcar\" v=\"yes\"/><tag k=\"duration\" v=\"00:10\"/>");
        xml.append("</way></osm>");
        Path source = directory.resolve("extract.osm");
        Files.writeString(source, xml);

        EncodingManager encodingManager = new EncodingManager.Builder()
                .add(new ORSDefaultFlagEncoderFactory().createFlagEncoder(FlagEncoderNames.CAR_ORS, new PMap()))
                .add(Subnetwork.create(FlagEncoderNames.CAR_ORS))
                .build();
        storage = new ORSGraphHopperStorage(new RAMDirectory(), encodingManager, false, false, -1);
        GraphProcessContext context = mock(GraphProcessContext.class);
        when(context.getStorageBuilders()).thenReturn(Collections.emptyList());

        ORSOSMReader reader = new ORSOSMReader(storage, new OSMReaderConfig(), context) {
            @Override
            protected void preprocessWay(GHPoint first, GHPoint last, ReaderWay way) {
                super.preprocessWay(first, last, way);
                wayTags = new HashMap<>(way.getTags());
            }
        };
        reader.setFile(source.toFile());
        reader.readGraph();
    }
}
