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

import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;

/**
 * Created by adam on 26/10/2017.
 * Copied from the GreenIndex portion of the routing
 */
public class BordersGraphStorageBuilder extends AbstractGraphStorageBuilder {
	private BordersGraphStorage _storage;
	private Map<Long, Byte> _borders = new HashMap<>();
	private byte DEFAULT_BORDER_TYPE = 0;

	public BordersGraphStorageBuilder() {

	}

	@Override
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");

		// TODO Check if the _greenIndexFile exists
		String csvFile = _parameters.get("filepath");
		readBorderWaysFromCSV(csvFile);
		_storage = new BordersGraphStorage();

		return _storage;
	}

	private void readBorderWaysFromCSV(String csvFile) throws IOException {
		BufferedReader csvBuffer = null;

		// The csv data should be as follows:
		// osm id (long), border type (hard, soft)

		try {
			String row;
			csvBuffer = new BufferedReader(new FileReader(csvFile));
			// Jump the header line
			row = csvBuffer.readLine();
			char separator = row.contains(";") ? ';': ',';
			String[] rowValues = new String[4];

			while ((row = csvBuffer.readLine()) != null)
			{
				if (!parseCSVrow(row, separator, rowValues))
					continue;

				_borders.put(Long.parseLong(rowValues[0]),  Byte.parseByte(rowValues[3]));
			}

		} catch (IOException openFileEx) {
			openFileEx.printStackTrace();
			throw openFileEx;
		} finally {
			if (csvBuffer != null)
				csvBuffer.close();
		}
	}

	private boolean parseCSVrow(String row, char separator,  String[] rowValues) {
		if (Helper.isEmpty(row))
			return false;

		String[] split = row.split(Character.toString(separator));
		// check to see how many columns - its should be the same length as the rowValues array
		if(split.length != rowValues.length) {
			return false;
		}

		// We have the same length of columns as expected, so now match to the rowValues
		for(int i=0; i<rowValues.length; i++) {
			rowValues[i] = split[i].trim().replace("\"", "");
			if(Helper.isEmpty(rowValues[i]))
				return false;
		}

		// If we got this far, then it is ok
		return true;
	}

	@Override
	public void processWay(ReaderWay way) {

	}

	@Override
	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		_storage.setEdgeValue(edge.getEdge(), calcBorderCrossing(way.getId()));
	}

	private byte calcBorderCrossing(long id) {
		// return a byte value based on the border type (none, hard or soft)

		Byte borderType = _borders.get(id);

		if (borderType == null)
			return DEFAULT_BORDER_TYPE;
		else
			return borderType;

	}

	@Override
	public String getName() {
		return "Borders";
	}
}
