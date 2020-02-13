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
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.routing.graphhopper.extensions.storages.StreetCrossingGraphStorage;

import java.util.*;

public class StreetCrossingGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private int trafficLights;
	private int crossings;

	private StreetCrossingGraphStorage storage;
	private List<String> trafficLightValues = new ArrayList<>(4);
	private List<String> crossingValues = new ArrayList<>(3);

	public StreetCrossingGraphStorageBuilder() {
		//see https://wiki.openstreetmap.org/wiki/DE:Key:crossing
		trafficLightValues.addAll(Arrays.asList("traffic_signals", "traffic_lights", "toucan", "pegasus"));
		crossingValues.addAll(Arrays.asList("uncontrolled", "zebra", "island"));
	}

	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (storage != null) {
			throw new Exception("GraphStorageBuilder has been already initialized.");
		}
		storage = new StreetCrossingGraphStorage();
		return storage;
	}

	@Override
	public void processWay(ReaderWay way) {
		this.processWay(way, new Coordinate[0], new HashMap<>());
	}

	@Override
	public void processWay(ReaderWay way, Coordinate[] coords, HashMap<Integer, HashMap<String, String>> nodeTags){
		int trafficLights = 0;
		int crossings = 0;
		if(nodeTags != null && nodeTags.size()>0){
			// MARQ24: that was my initial/original code from the OSMReader to count traffic lights and crossings...
			// that was extracted in the 'processNode(...)' state...
			// This is just here as reference for the reviewer to check, if we now going to extract the same
			// information from the collected nodeTags...
			/*if (node.hasTag("highway", "traffic_signals") || node.hasTag("crossing", crossing_with_trafficLight)) {
				osmNodeIdToNodeExtraFlagsMap.put(node.getId(), NODE_EXTRADATA_HAS_TRAFFIC_LIGHT);
			} else if (node.hasTag("highway", "crossing") && node.hasTag("crossing", crossing_without)) {
				osmNodeIdToNodeExtraFlagsMap.put(node.getId(), NODE_EXTRADATA_HAS_CROSSING);
			}*/

			for(HashMap<String, String> aNodeTagList: nodeTags.values()){
				if(aNodeTagList.containsKey("highway")) {
					String highwayVal = aNodeTagList.get("highway");
					if (highwayVal.equalsIgnoreCase("traffic_signals")) {
						trafficLights++;
					} else if (highwayVal.equalsIgnoreCase("crossing")) {
						// check if there is additional info ?! what type of crossing?!
						if (aNodeTagList.containsKey("crossing")) {
							String crossingVal = aNodeTagList.get("crossing");
							if (trafficLightValues.contains(crossingVal)) {
								trafficLights++;
							} else if (crossingValues.contains(crossingVal)) {
								crossings++;
							}
						}
					}
				}
			}
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		storage.setEdgeValue(edge.getEdge(), trafficLights, crossings);
	}

	@Override
	public String getName() {
		return "StreetCrossing";
	}
}
