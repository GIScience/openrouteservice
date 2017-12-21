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

import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import heigit.ors.routing.RoutingProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Map.Entry;

public class ORSOSMReader extends OSMReader {

	private static Logger LOGGER = Logger.getLogger(ORSOSMReader.class.getName());

	private GraphProcessContext _procCntx;
	private HashMap<Integer, Long> tmcEdges;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds;
	private RoutingProfile refProfile;
	private boolean enrichInstructions;
	private OSMDataReaderContext _readerCntx;

	private String[] TMC_ROAD_TYPES = new String[] { "motorway", "motorway_link", "trunk", "trunk_link", "primary",
			"primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "residential" };

	public static final String[] HGV_VALUES = new String[] { "maxheight", "maxweight", "maxweight:hgv", "maxwidth", "maxlength", "maxlength:hgv", "maxaxleload" };
	public static final Set<String> hgv_tags = new HashSet<>(Arrays.asList(HGV_VALUES));

	public ORSOSMReader(GraphHopperStorage storage, GraphProcessContext procCntx, HashMap<Integer, Long> tmcEdges,  HashMap<Long, ArrayList<Integer>> osmId2EdgeIds, RoutingProfile refProfile) {
		super(storage);

		setCalcDistance3D(false);
		this._procCntx = procCntx;
		this._readerCntx = new OSMDataReaderContext(this);
		this.tmcEdges = tmcEdges;
		this.osmId2EdgeIds = osmId2EdgeIds;
		this.refProfile = refProfile;
		
		enrichInstructions = (refProfile != null) && (storage.getEncodingManager().supports("foot")
				|| storage.getEncodingManager().supports("bike")  
				|| storage.getEncodingManager().supports("MTB")
				|| storage.getEncodingManager().supports("RACINGBIKE")
				|| storage.getEncodingManager().supports("SAFETYBIKE"));
	}

	@Override
	protected boolean isInBounds(ReaderNode node) {
		if (_procCntx != null) {
			return _procCntx.isValidPoint(node.getLon(), node.getLat());
		}

		return super.isInBounds(node);
	}

	@Override
	public void onProcessWay(ReaderWay way) {
		_procCntx.processWay(way);
	}


	/**
	 * Applies tags of nodes that lie on a way onto the way itself so that they are
	 * regarded in the following storage building process. E.g. a maxheight tag on a node will
	 * be treated like a maxheight tag on the way the node belongs to.
	 *
	 * @param  map  a map that projects node ids onto a property map
	 * @param  way	the way to process
	 */


	@Override
	public void applyNodeTagsToWay(HashMap<Long, Map<String, Object>> map, ReaderWay way){
		LongArrayList osmNodeIds = way.getNodes();
		int size = osmNodeIds.size();
		if (size > 2) {
			for (int i = 1; i < size - 1; i++) {
				long nodeId = osmNodeIds.get(i);
				if (map.containsKey(nodeId)) {
					java.util.Iterator<Entry<String, Object>> it = map.get(nodeId).entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<String, Object> pairs = it.next();
						String key = pairs.getKey();
						String value = pairs.getValue().toString();
						//System.out.println(key);
						if(hgv_tags.contains(key)) way.setTag(key, value);
					}
				}
			}
		}
	}


	@Override
	protected void onProcessEdge(ReaderWay way, EdgeIteratorState edge) {

		if (enrichInstructions && Helper.isEmpty(way.getTag("name")) && Helper.isEmpty(way.getTag("ref"))) {
			try {
				/*	if (way.getId() != prevMatchedWayId)
				{
					prevMatchedWayId = way.getId();
					PointList pl = getWayPoints(way);
					matchedEdgeName = null;
					RouteSegmentInfo rsi = refProfile.getMatchedSegment(pl, 15.0);

					if (rsi != null) {
						String objName = rsi.getNearbyStreetName(pl, true);
						if (!Helper.isEmpty(objName)) {
							matchedEdgeName = objName;
							way.setTag("name", matchedEdgeName);
						}
					}
				}

				if (!Helper.isEmpty(matchedEdgeName)) {
					edge.setName(matchedEdgeName);
				}*/

			} 
			catch (Exception ex) {
			}
		}

		try {
			if ((tmcEdges != null) && (osmId2EdgeIds!=null)) {
				String highwayValue = way.getTag("highway");

				if (!Helper.isEmpty(highwayValue)) {

					for (int i = 0; i < TMC_ROAD_TYPES.length; i++) {
						if (TMC_ROAD_TYPES[i].equalsIgnoreCase(highwayValue)) {
							tmcEdges.put(edge.getEdge(), way.getId());

							if (osmId2EdgeIds.containsKey(way.getId())){
								osmId2EdgeIds.get(way.getId()).add(edge.getEdge());

							} else{								
								ArrayList<Integer> edgeIds = new ArrayList<Integer>();
								edgeIds.add(edge.getEdge()); 
								osmId2EdgeIds.put(way.getId(), edgeIds);		
							} 							

							break;
						}
					}
				}
			}
		
			_procCntx.processEdge(way, edge);
		} catch (Exception ex) {
			LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
		}
	}
	
	@Override 
    protected boolean onCreateEdges(ReaderWay way, LongArrayList osmNodeIds, long wayFlags, List<EdgeIteratorState> createdEdges)
    {
		try
		{
			return _procCntx.createEdges(_readerCntx, way, osmNodeIds, wayFlags, createdEdges);
		}
		catch (Exception ex) {
			LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
		}
		
		return false;
    }

	@Override 
	protected void finishedReading() {

		// System.out.println("----------  ORSOSMReader.finishedReading()");
		super.finishedReading();
		
		_procCntx.finish();
	}
}
