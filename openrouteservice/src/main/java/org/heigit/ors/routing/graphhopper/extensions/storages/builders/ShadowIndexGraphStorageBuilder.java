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
 *
 */
public class ShadowIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private static final Logger LOGGER = Logger.getLogger(ShadowIndexGraphStorageBuilder.class.getName());
    private ShadowIndexGraphStorage _storage;
    private Map<Long, Integer> osm_shadowindex_lookup = new HashMap<>();
    private int max_level = 100;
    private final int no_data = 30;

    public ShadowIndexGraphStorageBuilder() {

    }

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (_storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        // TODO Check if the shadow index file exists
        String csvFile = parameters.get("filepath");
        LOGGER.info("Shadow Index File: " + csvFile);
        readShadowIndicesFromCSV(csvFile);
        _storage = new ShadowIndexGraphStorage();

        return _storage;
    }

    private void readShadowIndicesFromCSV(String csvFile) throws IOException {
        try (BufferedReader csvBuffer = new BufferedReader(new FileReader(csvFile))) {
            String row;
            String[] rowValues = new String[2];
            while ((row = csvBuffer.readLine()) != null) {
                if (!parseCSVrow(row, rowValues))
                    continue;

                osm_shadowindex_lookup.put(Long.parseLong(rowValues[0]), Integer.parseInt(rowValues[1]));
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
        if (pos > 0)
        {
        	rowValues[0] = row.substring(0, pos).trim();
        	rowValues[1] = row.substring(pos+1, row.length()).trim();
        	// read, check and push "osm_id" and "shadow level" values
        	if (Helper.isEmpty(rowValues[0]) || Helper.isEmpty(rowValues[1])) 
        		return false;
        	
        	return true;
        }
        else
        	return false;
    }

    @Override
    public void processWay(ReaderWay way) {

    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        //_storage.setEdgeValue(edge.getEdge(), getShadowIndex(way.getId()));
        byte shadow_index =  getShadowIndex(way.getId());
    	_storage.setEdgeValue(edge.getEdge(), shadow_index);
    }

    private byte getShadowIndex(long id) {
        Integer shadow_index = osm_shadowindex_lookup.get(id);

        if (shadow_index == null)
            return (byte) no_data;

        if (shadow_index > max_level) {
            LOGGER.warn("\nThe shadow index value of osm way, id = " + id + " is " + shadow_index
                + ", which is larger than than max level!");
            return (byte) max_level;
        }

        return (byte) (shadow_index.intValue());
    }

    @Override
    public String getName() {
        return "ShadowIndex";
    }

    public ShadowIndexGraphStorage get_storage() {
        return _storage;
    }

}