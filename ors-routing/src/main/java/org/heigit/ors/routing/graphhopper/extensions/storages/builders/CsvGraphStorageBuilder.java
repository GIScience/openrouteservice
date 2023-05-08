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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.storages.CsvGraphStorage;

public class CsvGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private static final Logger LOGGER = Logger.getLogger(CsvGraphStorageBuilder.class.getName());
    private CsvGraphStorage storage;
    private Map<Long, Integer[]> id2Value = new HashMap<>();
    private static final int MAX_VALUE = 100;
    private final byte defaultValue = 0; // TODO: make configurable
    private String[] columnNames;

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        // TODO Check if the CSV file exists
        String csvFile = parameters.get("filepath");
        readFromCSV(csvFile);
        storage = new CsvGraphStorage(columnNames);

        return storage;
    }

    private void readFromCSV(String csvFile) throws IOException {
        try (BufferedReader csvBuffer = new BufferedReader(new FileReader(csvFile))) {
            // Header line
            String row = csvBuffer.readLine();
            columnNames = Arrays.stream(row.split(",")).skip(1).toArray(String[]::new);
            LOGGER.info(columnNames.length + " CSV column names read: " + Arrays.toString(columnNames));
            // Body
            while ((row = csvBuffer.readLine()) != null) {
                String[] idAndTail = row.split(",", 2);
                if (idAndTail.length != 2) continue;

                Long id = Long.parseLong(idAndTail[0].trim());
                Integer[] values = Arrays.stream(idAndTail[1].split(","))
                        .map(String::trim)
                        .map(x -> (int)(Float.parseFloat(x) * 100))
                        .toArray(Integer[]::new);
                id2Value.put(id, values);
            }
        } catch (IOException openFileEx) {
            LOGGER.error(openFileEx.getStackTrace());
            throw openFileEx;
        }
    }

    @Override
    public void processWay(ReaderWay way) {
        // do nothing
    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        byte[] values =  getValues(way.getId());
    	storage.setEdgeValue(edge.getEdge(), values);
    }

    private byte[] getValues(long id) {
        Integer[] gi = id2Value.get(id);

        byte[] byteValues = new byte[columnNames.length];

        // if id not present, fill with default value
        if (gi == null) {
            Arrays.fill(byteValues, defaultValue);
        } else {
            int index = 0;
            for (Integer i: gi) {
                if (i > MAX_VALUE) {
                    throw new AssertionError("Value too large (way id " + id
                            + " at index " + index + "):" + i + " > " + MAX_VALUE);
                }
                byteValues[index] = i.byteValue();
                index++;
            }
        }
        return byteValues;
    }

    @Override
    public String getName() {
        return "csv";
    }
}