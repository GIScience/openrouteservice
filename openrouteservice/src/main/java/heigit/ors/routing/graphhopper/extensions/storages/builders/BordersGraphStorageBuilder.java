/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for building the Borders graph extension that allows restricting routes regarding border crossings
 *
 * @author Adam Rousell
 */
public class BordersGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private BordersGraphStorage _storage;
    private Map<Long, Short[]> _borders = new HashMap<>();
    private short DEFAULT_BORDER_TYPE = 0;

    public BordersGraphStorageBuilder() {

    }

    /**
     * Initialize the Borders graph extension <br/><br/>
     * <p>
     * If the storage has not yet been initialized, it reads the Borders CSV file and stores the information ready
     * for generating the graph
     *
     * @param graphhopper
     * @return
     * @throws Exception
     */
    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (_storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        String csvFile = _parameters.get("filepath");
        readBorderWaysFromCSV(csvFile);
        _storage = new BordersGraphStorage();
        return _storage;
    }

    /**
     * Read information from the Borders CSV file.<br/><br/>
     * <p>
     * This method reads the information from the Borders CSV file (path specified in app.config) and stores the
     * border crossings contained in a map to be used when building the graph.
     *
     * @param csvFile The path to the CSV file containing the border crossing info.
     * @throws IOException
     */
    private void readBorderWaysFromCSV(String csvFile) throws IOException {
        BufferedReader csvBuffer = null;

        // The csv data should be as follows:
        // osm id (long), border type (hard, soft)

        try {
            String row;
            csvBuffer = new BufferedReader(new FileReader(csvFile));
            // Jump the header line
            row = csvBuffer.readLine();
            char separator = row.contains(";") ? ';' : ',';
            String[] rowValues = new String[4];

            while ((row = csvBuffer.readLine()) != null) {
                if (!parseCSVrow(row, separator, rowValues))
                    continue;

                _borders.put(Long.parseLong(rowValues[0]), new Short[]{Short.parseShort(rowValues[3]), Short.parseShort(rowValues[1]), Short.parseShort(rowValues[2])});
            }

        } catch (IOException openFileEx) {
            openFileEx.printStackTrace();
            throw openFileEx;
        } finally {
            if (csvBuffer != null)
                csvBuffer.close();
        }
    }

    /**
     * Parse a row of the CSV data and add the information to the passed String array
     *
     * @param row       The complete row read from the CSV
     * @param separator The seperator value used by the CSV to seperate columns
     * @param rowValues The array that obtained values should be written to
     * @return A boolean signifying whether the row is valid
     */
    private boolean parseCSVrow(String row, char separator, String[] rowValues) {
        if (Helper.isEmpty(row))
            return false;

        String[] split = row.split(Character.toString(separator));
        // check to see how many columns - its should be the same length as the rowValues array
        if (split.length != rowValues.length) {
            return false;
        }

        // We have the same length of columns as expected, so now match to the rowValues
        for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = split[i].trim().replace("\"", "");
            if (Helper.isEmpty(rowValues[i]))
                return false;
        }

        // If we got this far, then it is ok
        return true;
    }

    @Override
    public void processWay(ReaderWay way) {

    }

    /**
     * Method to process the edge and store it in the graph.<br/><br/>
     * <p>
     * It uses the OSM way ID to obtain data from the border crossings data read previously and calls the setEdgeValues
     * method within the BordersGraphStorage object.
     *
     * @param way  The OSM way obtained from the OSM reader. This way corresponds to the edge to be processed
     * @param edge The graph edge to be process
     */
    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        _storage.setEdgeValue(edge.getEdge(),
                calcBorderCrossing(way.getId(), BordersGraphStorage.Property.TYPE),
                calcBorderCrossing(way.getId(), BordersGraphStorage.Property.START),
                calcBorderCrossing(way.getId(), BordersGraphStorage.Property.END)
        );
    }

    /**
     * Method to calculate the values of the edge which should be stored in the graph ready for usage when determining
     * dynamic weights.<br/><br/>
     * <p>
     * The method takes an OSM way ID (that corresponds to the edge of the graph) and obtians the corresponding data
     * from the border data read during initialisation of the StorageBuilder.<br/><br/>
     * <p>
     * If there is no corresponding way in the border crossing data, then the vlaue is set to 0.
     *
     * @param id   OSM Way ID of the edge
     * @param prop The property to be obtained from the border array (TYPE, START, END)
     * @return The corresponding value obtained from the border data
     */
    private short calcBorderCrossing(long id, BordersGraphStorage.Property prop) {
        //TODO: Change to short
        Short[] border = _borders.get(id);
        if (border != null) {
            // determine the property we are obtaining
            switch (prop) {
                case TYPE:
                    if (border[0] == null)
                        return DEFAULT_BORDER_TYPE;
                    else {
                        return border[0];
                    }
                case START:
                    if (border[1] == null)
                        return 0;
                    else {
                        return border[1];
                    }
                case END:
                    if (border[2] == null)
                        return 0;
                    else {
                        return border[2];
                    }
            }
        }

        return 0;
    }

    /**
     * Method identifying the name of the extension which is used in various building processes
     *
     * @return The name of this extension.
     */
    @Override
    public String getName() {
        return "Borders";
    }
}
