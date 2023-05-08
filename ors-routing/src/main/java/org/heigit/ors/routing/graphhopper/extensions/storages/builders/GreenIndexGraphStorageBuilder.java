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
package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.storages.GreenIndexGraphStorage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lliu on 13/03/2017.
 */
public class GreenIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private static final Logger LOGGER = Logger.getLogger(GreenIndexGraphStorageBuilder.class.getName());

    private GreenIndexGraphStorage storage;
    private final Map<Long, Double> greenIndices = new HashMap<>();
    private static final int TOTAL_LEVEL = 64;
    private static final int DEFAULT_LEVEL = TOTAL_LEVEL - 1;
    private final Map<Byte, SlotRange> slots = new HashMap<>(TOTAL_LEVEL);

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        // TODO Refactoring Check if the _greenIndexFile exists
        String csvFile = parameters.get("filepath");
        readGreenIndicesFromCSV(csvFile);
        prepareGreenIndexSlots();
        storage = new GreenIndexGraphStorage();

        return storage;
    }

    private void prepareGreenIndexSlots() {
        double max = Collections.max(greenIndices.values());
        double min = Collections.min(greenIndices.values());
        double step = (max - min) / TOTAL_LEVEL;
        // Divide the range of raw green index values into TOTAL_LEVEL,
        // then map the raw value to [0..TOTAL_LEVEL - 1]
        for (byte i = 0; i < TOTAL_LEVEL; i++) {
            slots.put(i, new SlotRange(min + i * step, min + (i + 1) * step));
        }
    }

    private void readGreenIndicesFromCSV(String csvFile) throws IOException {
        try (BufferedReader csvBuffer = new BufferedReader(new FileReader(csvFile))) {
            String row;
            // Jump the header line
            row = csvBuffer.readLine();
            char separator = row.contains(";") ? ';': ',';
            String[] rowValues = new String[2];

            while ((row = csvBuffer.readLine()) != null)  {
                if (!parseCSVrow(row, separator, rowValues))
                	continue;

                greenIndices.put(Long.parseLong(rowValues[0]), Double.parseDouble(rowValues[1]));
            }
        } catch (IOException openFileEx) {
            LOGGER.error(openFileEx.getStackTrace());
            throw openFileEx;
        }
    }

    private boolean parseCSVrow(String row, char separator,  String[] rowValues) {
        if (Helper.isEmpty(row))
        	return false;

        int pos = row.indexOf(separator);
        if (pos > 0) {
        	rowValues[0] = row.substring(0, pos).trim();
        	rowValues[1] = row.substring(pos+1).trim();
        	// read, check and push "osm_id" and "ungreen_factor" values
            return !Helper.isEmpty(rowValues[0]) && !Helper.isEmpty(rowValues[1]);
        }
       	return false;
    }

    @Override
    public void processWay(ReaderWay way) {
        // do nothing
    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        storage.setEdgeValue(edge.getEdge(), calcGreenIndex(way.getId()));
    }

    private class SlotRange {
        double left = 0.0;
        double right = 0.0;

        SlotRange(double l, double r) {
            this.left = l;
            this.right = r;
        }

        boolean within(double val) {
            // check if the @val falls in [left, right] range
            return (val >= left) && (val <= right);
        }
    }

    private byte calcGreenIndex(long id) {
        Double gi = greenIndices.get(id);

        // No such @id key in the _greenIndices, or the value of it is null
        // We set its green level to TOTAL_LEVEL/2 indicating the middle value for such cases
        // TODO Refactoring this DEFAULT_LEVEL should be put in the ors-config.json file and
        // injected back in the code
        if (gi == null)
            return (byte) (DEFAULT_LEVEL);

        for (Map.Entry<Byte, SlotRange> s : slots.entrySet()) {
            if (s.getValue().within(gi))
                return s.getKey();
        }
        return (byte) (DEFAULT_LEVEL);
    }

    @Override
    public String getName() {
        return "GreenIndex";
    }
}
