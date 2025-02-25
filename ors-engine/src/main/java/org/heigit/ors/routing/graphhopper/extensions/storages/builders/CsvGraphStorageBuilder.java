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
import org.apache.log4j.Logger;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.heigit.ors.routing.graphhopper.extensions.storages.CsvGraphStorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CsvGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private static final Logger LOGGER = Logger.getLogger(CsvGraphStorageBuilder.class.getName());
    private CsvGraphStorage storage;
    private final Map<Long, Integer[]> id2Value = new HashMap<>();
    private static final int MAX_VALUE = 100;
    private static final int MIN_VALUE = -100;
    private final byte defaultValue = 50; // TODO: make configurable
    private String[] columnNames;

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        ExtendedStorageProperties parameters;
        parameters = this.parameters;

        File expectedStorageFileLocation = Path.of(graphhopper.getGraphHopperLocation() + "/ext_csv").toFile();
        if (!expectedStorageFileLocation.exists()) {
            String csvFile = parameters.getFilepath().toString();
            readFromCSV(csvFile);
            parameters.setColumnNames(columnNames);
        } else {
            columnNames = parameters.getColumnNames();
        }
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
                        .map(x -> (int) (Float.parseFloat(x) * 100))
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
        byte[] values = getValues(way.getId());
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
            for (Integer i : gi) {
                if (i > MAX_VALUE || i < MIN_VALUE) {
                    throw new AssertionError("Value out of range (way id " + id
                            + " at index " + index + "):" + i + " not in [" + MIN_VALUE + "," + MAX_VALUE + "]");
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