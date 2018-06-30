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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import heigit.ors.routing.RoutingProfile;
import heigit.ors.routing.graphhopper.extensions.storages.builders.BordersGraphStorageBuilder;
import heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ORSOSMReader extends OSMReader {

	private static Logger LOGGER = Logger.getLogger(ORSOSMReader.class.getName());

	private GraphProcessContext _procCntx;
	private HashMap<Integer, Long> tmcEdges;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds;
	private RoutingProfile refProfile;

	// MARQ24: REMOVED SINCE code that handles 'enrichInstructions = true' is already inactive!
	//private boolean enrichInstructions;

	private OSMDataReaderContext _readerCntx;
	private GeometryFactory gf = new GeometryFactory();

	private boolean processGeom = false;

	private String[] TMC_ROAD_TYPES = new String[] { "motorway", "motorway_link", "trunk", "trunk_link", "primary",
			"primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "residential" };

	// MARQ24: REMOVED SINCE code that handles 'enrichInstructions = true' is already inactive!
	/*private boolean doEnrichInstructions(GraphHopperStorage storage){
		EncodingManager encMan = storage.getEncodingManager();
		return 	encMan.supports("foot")
				|| encMan.supports("hike")
				|| encMan.supports("bike")
				|| encMan.supports("bike-ors")
				|| encMan.supports("mtb")
				|| encMan.supports("mtb-ors")
				|| encMan.supports("RACINGBIKE")
				|| encMan.supports("roadbike-ors")
				|| encMan.supports("SAFETYBIKE");
	}*/

	public ORSOSMReader(GraphHopperStorage storage, GraphProcessContext procCntx, HashMap<Integer, Long> tmcEdges,  HashMap<Long, ArrayList<Integer>> osmId2EdgeIds, RoutingProfile refProfile) {
		super(storage);

		setCalcDistance3D(false);
		this._procCntx = procCntx;
		this._readerCntx = new OSMDataReaderContext(this);
		this.tmcEdges = tmcEdges;
		this.osmId2EdgeIds = osmId2EdgeIds;
		this.refProfile = refProfile;

		// MARQ24: REMOVED SINCE code that handles 'enrichInstructions = true' is already inactive!
		//enrichInstructions = (refProfile != null) && doEnrichInstructions(storage);

		// Look if we should do border processing - if so then we have to process the geometry
		for(GraphStorageBuilder b : this._procCntx.getStorageBuilders()) {
			if ( b instanceof BordersGraphStorageBuilder) {
				this.processGeom = true;
			}
		}

	}

	@Override
	protected boolean isInBounds(ReaderNode node) {
		if (_procCntx != null) {
			return _procCntx.isValidPoint(node.getLon(), node.getLat());
		}

		return super.isInBounds(node);
	}

	/**
	 * Method to be run against each way obtained from the data. If one of the storage builders needs geometry
	 * determined in the constructor then we need to get the geometry as well as the tags.
	 *
	 * @param way		The way object read from the OSM data (not including geometry)
	 */
	@Override
	public void onProcessWay(ReaderWay way) {
		if(processGeom) {
			// We need to pass the geometry of the way aswell as the ReaderWay object
			// This is slower so should only be done when needed

			// First we need to generate the geometry
			LongArrayList osmNodeIds = way.getNodes();

			ArrayList<Coordinate> coords = new ArrayList<>();

			if(osmNodeIds.size() > 1) {
				for (int i=0; i<osmNodeIds.size(); i++) {
					int id = getNodeMap().get(osmNodeIds.get(i));
					try {
						double lat = getLatitudeOfNode(id);
						double lon = getLongitudeOfNode(id);
						// Add the point to the line
						// Check that we have a tower node
						if(!(lat == 0 || lon == 0 || Double.isNaN(lat) || Double.isNaN(lon)))
						if (lat != 0 || lon != 0)
							coords.add(new Coordinate(lon, lat));
					} catch (Exception e) {
						LOGGER.error("Could not process node " + osmNodeIds.get(i) );
					}
				}

				// Only process valid ways (with more than 1 valid node)
                if(coords.size() > 1) {
                    //LineString ls = gf.createLineString(coords.toArray(new Coordinate[coords.size()]));

                    _procCntx.processWay(way, coords.toArray(new Coordinate[coords.size()]));
                }
			}

		} else {
			_procCntx.processWay(way);
		}
	}

	/* The following two methods are not ideal, but due to a preprocessing stage of GH they are required if you want
	 * the geometry of the whole way. */

	/**
	 * Find the latitude of the node with the given ID. It checks to see what type of node it is and then finds the
	 * latitude from the correct storage location.
	 *
	 * @param id		Internal ID of the OSM node
	 * @return
	 */
	private double getLatitudeOfNode(int id) {
		// for speed, we only want to handle the geometry of tower nodes (those at junctions)
		if (id == EMPTY_NODE)
			return Double.NaN;
		if (id < TOWER_NODE) {
			// tower node
			id = -id - 3;
			return nodeAccess.getLatitude(id);
		} else if (id > -TOWER_NODE) {
			// pillar node
			return Double.NaN;
		} else
			// e.g. if id is not handled from preparse (e.g. was ignored via isInBounds)
			return Double.NaN;
	}

	/**
	 * Find the longitude of the node with the given ID. It checks to see what type of node it is and then finds the
	 * longitude from the correct storage location.
	 *
	 * @param id		Internal ID of the OSM node
	 * @return
	 */
	private double getLongitudeOfNode(int id) {
		if (id == EMPTY_NODE)
			return Double.NaN;
		if (id < TOWER_NODE) {
			// tower node
			id = -id - 3;
			return nodeAccess.getLongitude(id);
		} else if (id > -TOWER_NODE) {
			// pillar node
			return Double.NaN;
		} else
			// e.g. if id is not handled from preparse (e.g. was ignored via isInBounds)
			return Double.NaN;
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
						way.setTag(key, value);
					}
				}
			}
		}
	}


	@Override
	protected void onProcessEdge(ReaderWay way, EdgeIteratorState edge) {

		// MARQ24: REMOVED SINCE code that handles 'enrichInstructions = true' is already inactive!
		// by MARQ24 if (enrichInstructions && Helper.isEmpty(way.getTag("name")) && Helper.isEmpty(way.getTag("ref"))) {
		// by MARQ24	try {
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

		// by MARQ24	}
		// by MARQ24	catch (Exception ex) {
		// by MARQ24	}
		// by MARQ24}

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
			LOGGER.warn(ex.getMessage() + ". Way id = " + way.getId());
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
			LOGGER.warn(ex.getMessage() + ". Way id = " + way.getId());
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
