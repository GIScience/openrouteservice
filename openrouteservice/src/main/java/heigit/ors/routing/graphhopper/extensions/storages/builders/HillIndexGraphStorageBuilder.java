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
import com.graphhopper.util.ByteArrayBuffer;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;
import heigit.ors.routing.util.HillIndexCalculator;

public class HillIndexGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private HillIndexGraphStorage _storage;
	private HillIndexCalculator _hillIndexCalc;
	private ByteArrayBuffer _arrayBuffer;
	
	public HillIndexGraphStorageBuilder()
	{
		
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		_arrayBuffer = new ByteArrayBuffer();
		if (graphhopper.hasElevation())
		{
			_storage = new HillIndexGraphStorage(_parameters);
			_hillIndexCalc = new HillIndexCalculator();
			
			return _storage;
		}
		else 
			return null;
	}

	public void processWay(ReaderWay way) {
		
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		boolean revert = edge.getBaseNode() > edge.getAdjNode();

		PointList points = edge.fetchWayGeometry(3, _arrayBuffer);
	
		byte hillIndex = _hillIndexCalc.getHillIndex(points, false);
		byte reverseHillIndex = _hillIndexCalc.getHillIndex(points, true);

		if (revert)
			_storage.setEdgeValue(edge.getEdge(), reverseHillIndex, hillIndex);
		else
			_storage.setEdgeValue(edge.getEdge(), hillIndex, reverseHillIndex);
	}

	@Override
	public String getName() {
		return "HillIndex";
	}
}
