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
package heigit.ors.routing.graphhopper.extensions;

import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.*;
import heigit.ors.routing.RoutingProfile;
import heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMFeatureFilter;
import heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.WheelchairWayFilter;
import heigit.ors.routing.graphhopper.extensions.storages.builders.BordersGraphStorageBuilder;
import heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import heigit.ors.routing.graphhopper.extensions.storages.builders.RoadAccessRestrictionsGraphStorageBuilder;
import heigit.ors.routing.graphhopper.extensions.storages.builders.WheelchairGraphStorageBuilder;
import org.apache.log4j.Logger;

import java.io.InvalidObjectException;
import java.util.*;

import java.util.Map.Entry;

public class ORSOSMReader extends OSMReader {

	private static Logger LOGGER = Logger.getLogger(ORSOSMReader.class.getName());

	private GraphProcessContext _procCntx;
	private HashMap<Integer, Long> tmcEdges;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds;
	private RoutingProfile refProfile;
	// MARQ24: REMOVED SINCE code that handles 'enrichInstructions = true' is already inactive!
	//private boolean enrichInstructions;
	private boolean processNodeTags;
	private OSMDataReaderContext _readerCntx;

	private HashMap<Long, HashMap<String, String>> nodeTags = new HashMap<>();

	private boolean processGeom = false;
	private boolean processSimpleGeom = false;
	private boolean detachSidewalksFromRoad = false;

	private List<OSMFeatureFilter> filtersToApply = new ArrayList<>();

	private String[] TMC_ROAD_TYPES = new String[] { "motorway", "motorway_link", "trunk", "trunk_link", "primary",
			"primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "residential" };

	private HashSet<String> extraTagKeys;

	public ORSOSMReader(GraphHopperStorage storage, GraphProcessContext procCntx, HashMap<Integer, Long> tmcEdges,  HashMap<Long, ArrayList<Integer>> osmId2EdgeIds, RoutingProfile refProfile) {
		super(storage);

		setCalcDistance3D(false);
		this._procCntx = procCntx;
		this._readerCntx = new OSMDataReaderContext(this);
		this.tmcEdges = tmcEdges;
		this.osmId2EdgeIds = osmId2EdgeIds;
		this.refProfile = refProfile;

		// MARQ24: REMOVED SINCE code that handles 'enrichInstructions = true' is already inactive!
		/*enrichInstructions = (refProfile != null) && (storage.getEncodingManager().supports("foot")
				|| storage.getEncodingManager().supports("bike")  
				|| storage.getEncodingManager().supports("MTB")
				|| storage.getEncodingManager().supports("RACINGBIKE")
				|| storage.getEncodingManager().supports("SAFETYBIKE"));
        */

		extraTagKeys = new HashSet<>();
		// Look if we should do border processing - if so then we have to process the geometry
		for(GraphStorageBuilder b : this._procCntx.getStorageBuilders()) {
			if ( b instanceof BordersGraphStorageBuilder) {
				this.processGeom = true;
			}

			if ( b instanceof WheelchairGraphStorageBuilder) {
				filtersToApply.add(new WheelchairWayFilter());
				this.processNodeTags = true;
				this.detachSidewalksFromRoad = true;
				this.processSimpleGeom = true;
				extraTagKeys.add("kerb");
				extraTagKeys.add("kerb:both");
                extraTagKeys.add("kerb:left");
                extraTagKeys.add("kerb:right");
				extraTagKeys.add("kerb:height");
                extraTagKeys.add("kerb:both:height");
                extraTagKeys.add("kerb:left:height");
                extraTagKeys.add("kerb:right:height");
			}

			if ( b instanceof RoadAccessRestrictionsGraphStorageBuilder) {
				this.processNodeTags = true;
				extraTagKeys.add("access");
				extraTagKeys.add("bicycle");
				extraTagKeys.add("foot");
				extraTagKeys.add("horse");
				extraTagKeys.add("motor_vehicle");
				extraTagKeys.add("motorcar");
				extraTagKeys.add("motorcycle");
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

	@Override
	public ReaderNode onProcessNode(ReaderNode node) {
		// On OSM, nodes are seperate entities which are used to make up ways. So basically, a node is read before a
		// way and if it has some properties that could affect routing, these properties need to be stored so that they
		// can be accessed when it comes to using ways
		if(processNodeTags && node.hasTags()) {
			// Check each node and store the tags that are required
			HashMap<String, String> tagValues = new HashMap<>();
			Set<String> nodeKeys = node.getTags().keySet();
			for(String key : nodeKeys) {
				if(extraTagKeys.contains(key)) {
					tagValues.put(key, node.getTag(key));
				}
			}

			// Now if we have tag data, we need to store it
			if(tagValues.size() > 0) {
				nodeTags.put(node.getId(), tagValues);
			}
		}
		return node;
	}

	@Override
	protected void processWay(ReaderWay way) {
		// As a first step we need to check to see if we should try to split the way
		if(this.detachSidewalksFromRoad) {
			// If we are requesting to split sidewalks, then we need to create multiple ways from a single road
			// For example, if a road way has been tagged as having sidewalks on both sides (sidewalk=both), then we
			// need to create two ways - one for the left sidewalk and one for the right. The Graph Builder would then
			// process these ways separately so that additional edges are created in the graph.

			for(OSMFeatureFilter filter : filtersToApply) {
				try {
					filter.assignFeatureForFiltering(way);
				} catch (InvalidObjectException ioe) {
					LOGGER.error("Invalid object for filtering - " + ioe.getMessage());
				}

				if(filter.accept()) {
					// We can only perform the processing of the ways here and so we cannot delegate it to another object.
					while (!filter.isWayProcessingComplete()) {
						filter.prepareForProcessing();
						super.processWay(way);
					}
				}
			}

			return;

		}

		// Normal processing
		super.processWay(way);
	}

	/**
	 * Method to be run against each way obtained from the data. If one of the storage builders needs geometry
	 * determined in the constructor then we need to get the geometry as well as the tags.
	 * Also we need to pass through any important tag values obtained from nodes through to the processing stage so
	 * that they can be evaluated.
	 *
	 * @param way		The way object read from the OSM data (not including geometry)
	 */
	@Override
	public void onProcessWay(ReaderWay way) {

		HashMap<Integer, HashMap<String,String>> tags = new HashMap<>();
		ArrayList<Coordinate> coords = new ArrayList<>();

		if(processNodeTags) {
			// If we are processing the node tags then we need to obtain the tags for nodes that are on the way. We
			// should store the internal node id though rather than the osm node as during the edge processing, we
			// do not know the osm node id
			LongArrayList osmNodeIds = way.getNodes();
			int size = osmNodeIds.size();

			for(int i=0; i<size; i++) {
				// find the node
				long id = osmNodeIds.get(i);
				// replace the osm id with the internal id
				int internalId = getInternalNodeIdOfOsmNode(id);
				HashMap<String, String> tagsForNode = nodeTags.get(id);

				if(tagsForNode != null) {
					tags.put(internalId, nodeTags.get(id));
				}
			}
		}

		if(processGeom || processSimpleGeom) {
			// We need to pass the geometry of the way aswell as the ReaderWay object
			// This is slower so should only be done when needed

			// First we need to generate the geometry
			LongArrayList osmNodeIds = new LongArrayList();
			LongArrayList allOsmNodes = way.getNodes();

			if(allOsmNodes.size() > 1) {
				if (processSimpleGeom) {
					// We only want the start and end nodes
					osmNodeIds.add(allOsmNodes.get(0));
					osmNodeIds.add(allOsmNodes.get(allOsmNodes.size()-1));
				} else {
					// Process all nodes
					osmNodeIds = allOsmNodes;
				}
			}

			if(osmNodeIds.size() > 1) {

				for (int i=0; i<osmNodeIds.size(); i++) {
					int id = getNodeMap().get(osmNodeIds.get(i));
					try {
						double lat = getLatitudeOfNode(id, true);
						double lon = getLongitudeOfNode(id, true);
						// Add the point to the line
						// Check that we have a tower node
						if(!(lat == 0 || lon == 0 || Double.isNaN(lat) || Double.isNaN(lon)))
						if (lat != 0 || lon != 0)
							coords.add(new Coordinate(lon, lat));
					} catch (Exception e) {
						LOGGER.error("Could not process node " + osmNodeIds.get(i) );
					}
				}
			}

		}

		if(tags.size() > 0 || coords.size() > 1) {
			// Use an overloaded method that allows the passing of parameters from this reader
			_procCntx.processWay(way, coords.toArray(new Coordinate[coords.size()]), tags);
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
	private double getLatitudeOfNode(int id, boolean onlyTower) {
		// for speed, we only want to handle the geometry of tower nodes (those at junctions)
		if (id == EMPTY_NODE)
			return Double.NaN;
		if (id < TOWER_NODE) {
			// tower node
			id = -id - 3;
			return nodeAccess.getLatitude(id);
		} else if (id > -TOWER_NODE) {
			// pillar node
			// Do we want to return it if it is not a tower node?
			if(onlyTower) {
				return Double.NaN;
			} else {
				return pillarInfo.getLatitude(id);
			}
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
	private double getLongitudeOfNode(int id, boolean onlyTower) {
		if (id == EMPTY_NODE)
			return Double.NaN;
		if (id < TOWER_NODE) {
			// tower node
			id = -id - 3;
			return nodeAccess.getLongitude(id);
		} else if (id > -TOWER_NODE) {
			// pillar node
			// Do we want to return it if it is not a tower node?
			if(onlyTower) {
				return Double.NaN;
			} else {
				return pillarInfo.getLatitude(id);
			}
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
		    // If it is a crossing then we need to apply any kerb tags to the way, but we need to make sure we keep the "worse" one
			for (int i = 1; i < size-1; i++) {
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
		// by MARQ24 	try {
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

		// by MARQ24 	}
		// by MARQ24 	catch (Exception ex) {
		// by MARQ24 	}
		// by MARQ24 }

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

			// Pass through the coordinates of the graph nodes
			Coordinate baseCoord = new Coordinate(
					getLongitudeOfNode(edge.getBaseNode(), false),
					getLatitudeOfNode(edge.getBaseNode(), false)
			);
			Coordinate adjCoordinate = new Coordinate(
					getLongitudeOfNode(edge.getAdjNode(), false),
					getLatitudeOfNode(edge.getAdjNode(), false)
			);

			_procCntx.processEdge(way, edge, new Coordinate[] {baseCoord, adjCoordinate});
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
