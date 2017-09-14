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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.WaySurfaceDescription;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.graphhopper.extensions.SurfaceType;
import heigit.ors.routing.graphhopper.extensions.WayType;
import heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;

public class WaySurfaceTypeGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private WaySurfaceTypeGraphStorage _storage;
	private final WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
	protected final HashSet<String> ferries;
	
	public WaySurfaceTypeGraphStorageBuilder()
	{
		ferries = new HashSet<String>(5);
		ferries.add("shuttle_train");
		ferries.add("ferry");
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		_storage = new WaySurfaceTypeGraphStorage();
		return _storage;
	}

	public void processWay(ReaderWay way) {
		waySurfaceDesc.Reset();

		boolean hasHighway = way.hasTag("highway");
		boolean isFerryRoute = way.hasTag("route", ferries);

		java.util.Iterator<Entry<String, Object>> it = way.getProperties();

		while (it.hasNext()) {
			Map.Entry<String, Object> pairs = it.next();
			String key = pairs.getKey();
			String value = pairs.getValue().toString();

			if (hasHighway || isFerryRoute) {
				if (key.equals("highway")) {
					byte wayType = (isFerryRoute) ? WayType.Ferry : (byte)WayType.getFromString(value);

					if (waySurfaceDesc.SurfaceType == 0)
					{
						if (wayType == WayType.Road ||  wayType == WayType.StateRoad || wayType == WayType.Street)
							waySurfaceDesc.SurfaceType = (byte)SurfaceType.Asphalt;
						else if (wayType == WayType.Path)
							waySurfaceDesc.SurfaceType = (byte)SurfaceType.Unpaved;
					}

					waySurfaceDesc.WayType = wayType;
				}
				else if (key.equals("surface")) {
					waySurfaceDesc.SurfaceType = (byte)SurfaceType.getFromString(value);
				}
			}
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) 
	{
		_storage.setEdgeValue(edge.getEdge(), waySurfaceDesc);
	}

	@Override
	public String getName() {
		return "WaySurfaceType";
	}
}
