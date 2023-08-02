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
 *
 */
package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.storages.ShadowIndexGraphStorage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * This class builds the shadow index data as a new ShadowGraphStorage.
 */
public class ShadowIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private static final Logger LOGGER = Logger.getLogger(ShadowIndexGraphStorageBuilder.class.getName());
    private ShadowIndexGraphStorage shadowIndexStorage;
    private final Map<Long, Integer> osmShadowIndexLookup = new HashMap<>();
    private static final int MAX_LEVEL = 100;
    private static final int NO_DATA = 30;

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws IllegalStateException, IOException {
        if (shadowIndexStorage != null)
            throw new IllegalStateException("GraphStorageBuilder has been already initialized.");
        // TODO Check if the shadow index file exists
        String csvFile = parameters.get("filepath");
        LOGGER.info("Shadow Index File: " + csvFile);
        readShadowIndicesFromCSV(csvFile);
        shadowIndexStorage = new ShadowIndexGraphStorage();
        return shadowIndexStorage;
    }

    private void readShadowIndicesFromCSV(String csvFile) throws IOException {
        try (BufferedReader csvBuffer = new BufferedReader(new FileReader(csvFile))) {
            String row;
            String[] rowValues = new String[2];
            while ((row = csvBuffer.readLine()) != null) {
                if (!parseCSVrow(row, rowValues))
                    continue;
                osmShadowIndexLookup.put(Long.parseLong(rowValues[0]), Integer.parseInt(rowValues[1]));
            }
        } catch (IOException openFileEx) {
            LOGGER.error(openFileEx.getStackTrace());
            throw openFileEx;
        }
    }

    private boolean parseCSVrow(String row, String[] rowValues) {
        if (Helper.isEmpty(row))
            return false;
        int pos = row.indexOf(',');
        if (pos > 0) {
            rowValues[0] = row.substring(0, pos).trim();
            rowValues[1] = row.substring(pos + 1).trim();
            // read, check and push "osm_id" and "shadow level" values
            return !Helper.isEmpty(rowValues[0]) && !Helper.isEmpty(rowValues[1]);
        } else
            return false;
    }

    public void processWay(ReaderWay way) {
        // Nothing to do
    }

    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        shadowIndexStorage.setEdgeValue(edge.getEdge(), getShadowIndex(way.getId()));
    }

    private byte getShadowIndex(long id) {
        Integer shadowIndex = osmShadowIndexLookup.get(id);
        if (shadowIndex == null)
            return (byte) NO_DATA;
        if (shadowIndex > MAX_LEVEL) {
            LOGGER.warn("\nThe shadow index value of osm way, id = " + id + " is " + shadowIndex
                    + ", which is larger than than max level!");
            return (byte) MAX_LEVEL;
        }
        return (byte) (shadowIndex.intValue());
    }

    @Override
    public String getName() {
        return "ShadowIndex";
    }
}
