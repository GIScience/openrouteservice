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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import heigit.ors.routing.graphhopper.extensions.storages.NoiseIndexGraphStorage;

/**
 * Created by ZWang on 13/06/2017.
 */
public class NoiseIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private NoiseIndexGraphStorage _storage;
    private Map<Long, Integer> osmId2noiseLevel = new HashMap<>();
    // currently noise level is only from 0 to 3
    private int max_level = 8;

    public NoiseIndexGraphStorageBuilder() {

    }

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (_storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        // TODO Check if the _noiseIndexFile exists
        String csvFile = _parameters.get("filepath");
        readNoiseIndicesFromCSV(csvFile);
        _storage = new NoiseIndexGraphStorage();

        return _storage;
    }

    private void readNoiseIndicesFromCSV(String csvFile) throws IOException {
        BufferedReader csvBuffer = null;
        try {
            String row;
            csvBuffer = new BufferedReader(new FileReader(csvFile));
            // Jump the header line
            csvBuffer.readLine();
            String[] rowValues = new String[2]; 
            while ((row = csvBuffer.readLine()) != null) 
            {
                if (!parseCSVrow(row, rowValues)) 
                	continue;
                
                osmId2noiseLevel.put(Long.parseLong(rowValues[0]), Integer.parseInt(rowValues[1]));
            }

        } catch (IOException openFileEx) {
            openFileEx.printStackTrace();
            throw openFileEx;
        } finally {
            if (csvBuffer != null) 
            	csvBuffer.close();
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
        	// read, check and push "osm_id" and "noise level" values
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
        //_storage.setEdgeValue(edge.getEdge(), getNoiseLevel(way.getId()));
        byte noise_level =  getNoiseLevel(way.getId());
    	_storage.setEdgeValue(edge.getEdge(), noise_level);     
    }

    private byte getNoiseLevel(long id) {
        Integer gi = osmId2noiseLevel.get(id);

        // No such @id key in the _noiseIndices, or the value of it is null
        // We set its noise level to zero (no noise)
        if (gi == null)
            return (byte) (0);
        if (gi.intValue() > max_level)
        	new AssertionError("The noise level of osm way, id = "+ id + " is " + gi +", which is larger than than max level!");
        
        return (byte) (gi.intValue());
    }

    @Override
    public String getName() {
        return "NoiseIndex";
    }
}