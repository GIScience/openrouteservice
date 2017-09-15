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
package heigit.ors.routing.graphhopper.extensions;

import java.util.Collection;

import com.carrotsearch.hppc.LongIndexedContainer;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.util.EdgeIteratorState;

public class OSMDataReaderContext implements DataReaderContext {

	private OSMReader osmReader;
	
	public OSMDataReaderContext(OSMReader osmReader) {
		this.osmReader = osmReader;
	}
	
	@Override
	public LongIntMap getNodeMap() {
		return osmReader.getNodeMap();
	}

	@Override
	public double getNodeLongitude(int nodeId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNodeLatitude(int nodeId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<EdgeIteratorState> addWay(LongIndexedContainer subgraphNodes, long wayFlags, long wayId) {
		// TODO Auto-generated method stub
		return null;
	}
   
}
