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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.storages.NoiseIndexGraphStorage;

/**
 * Created by ZWang on 13/06/2017.
 */
public class NoiseIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private static final Logger LOGGER = Logger.getLogger(NoiseIndexGraphStorageBuilder.class.getName());

    private NoiseIndexGraphStorage storage;
    private final Map<Long, Integer> osmId2noiseLevel = new HashMap<>();
    // currently noise level is only from 0 to 3
    private static final int MAX_LEVEL = 8;

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        // TODO Refactoring Check if the _noiseIndexFile exists
        String csvFile = parameters.get("filepath");
        readNoiseIndicesFromCSV(csvFile);
        storage = new NoiseIndexGraphStorage();

        return storage;
    }

    private void readNoiseIndicesFromCSV(String csvFile) throws IOException {
        try (BufferedReader csvBuffer = new BufferedReader(new FileReader(csvFile))) {
            // Jump the header line
            String row = csvBuffer.readLine();
            String[] rowValues = new String[2];
            while ((row = csvBuffer.readLine()) != null) {
                if (!parseCSVrow(row, rowValues)) 
                	continue;
                
                osmId2noiseLevel.put(Long.parseLong(rowValues[0]), Integer.parseInt(rowValues[1]));
            }
        } catch (IOException openFileEx) {
            LOGGER.error(openFileEx.getStackTrace());
            throw openFileEx;
        }
    }

    private boolean parseCSVrow(String row,  String[] rowValues) {
        if (Helper.isEmpty(row))
        	return false;
        
        int pos = row.indexOf(',');
        if (pos > 0) {
        	rowValues[0] = row.substring(0, pos).trim();
        	rowValues[1] = row.substring(pos + 1).trim();
        	// read, check and push "osm_id" and "noise level" values
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
        byte noiseLevel =  getNoiseLevel(way.getId());
    	storage.setEdgeValue(edge.getEdge(), noiseLevel);
    }

    private byte getNoiseLevel(long id) {
        Integer gi = osmId2noiseLevel.get(id);

        // No such @id key in the _noiseIndices, or the value of it is null
        // We set its noise level to zero (no noise)
        if (gi == null)
            return (byte) (0);
        if (gi > MAX_LEVEL)
        	throw new AssertionError("The noise level of osm way, id = "+ id + " is " + gi +", which is larger than than max level!");
        
        return (byte) (gi.intValue());
    }

    @Override
    public String getName() {
        return "NoiseIndex";
    }
}