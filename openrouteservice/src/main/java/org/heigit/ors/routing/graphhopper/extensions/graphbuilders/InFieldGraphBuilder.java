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
package org.heigit.ors.routing.graphhopper.extensions.graphbuilders;

import com.carrotsearch.hppc.IntIndexedContainer;
import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.GraphHopper;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import org.locationtech.jts.geom.*;
import org.heigit.ors.routing.graphhopper.extensions.DataReaderContext;

import java.util.*;

public class InFieldGraphBuilder extends AbstractGraphBuilder {

	private final GeometryFactory geometryFactory = new GeometryFactory();
	private final Map<Integer, Integer> intId2idx = new HashMap<>();
	private final Map<Integer, Integer> idx2intId =  new HashMap<>();
	private final Map<Integer, Long> intId2osmId = new HashMap<>();
	private final ArrayList<Integer> internalTowerNodeIds = new ArrayList<>();
	private Coordinate[] coordinates;
	private final Set<ArrayList<Integer>> edges = new HashSet<>();
	private final ArrayList<Integer> tmpEdge = new ArrayList<>();
	private List<Weighting> weightings;
	private EncodingManager encodingManager;

	@Override
	public void init(GraphHopper graphhopper) throws Exception {
		// create local network taken from        
		// https://github.com/graphhopper/graphhopper/blob/0.5/core/src/test/java/com/graphhopper/GraphHopperTest.java#L746
		FootFlagEncoder footEncoder = new FootFlagEncoder();
		encodingManager = EncodingManager.create(footEncoder);
		weightings = new ArrayList<>(1);
		weightings.add(new FastestWeighting(footEncoder));
	}

	@Override
	public boolean createEdges(DataReaderContext readerCntx, ReaderWay way, LongArrayList osmNodeIds, IntsRef wayFlags, List<EdgeIteratorState> createdEdges) {
		if (!hasOpenSpace(way, osmNodeIds))
			return false;

		LongIntMap nodeMap = readerCntx.getNodeMap();
		Polygon openSpace = osmPolygon2JTS(readerCntx, osmNodeIds);

		internalTowerNodeIds.clear();
		intId2osmId.clear();
		idx2intId.clear();
		intId2idx.clear();

		// fill list with tower nodes        
		// fill map "internal ID 2 OSM ID"     
		for (int j = 0; j < osmNodeIds.size() - 1; j++) {       
			long osmNodeId = osmNodeIds.get(j);           
			int internalOSMId = nodeMap.get(osmNodeId);   
			intId2osmId.put(internalOSMId, osmNodeId);          
			if (internalOSMId < -2) //towernode
			{        
				internalTowerNodeIds.add(internalOSMId);    
			}      
		}

		DistanceCalc distCalc = DistanceCalcEarth.DIST_EARTH;
		try (GraphHopperStorage graphStorage = new GraphHopperStorage(new RAMDirectory(), encodingManager, false).create(20)) {
			for (int idxMain = 0; idxMain < osmNodeIds.size() - 1; idxMain++) {
				long mainOsmId = osmNodeIds.get(idxMain);
				int internalMainId = nodeMap.get(mainOsmId);
				// coordinates of the first nodes
				double latMain = readerCntx.getNodeLatitude(internalMainId);
				double lonMain = readerCntx.getNodeLongitude(internalMainId);
				// connect the boundary of the open space
				int idxNeighbor = idxMain + 1;
				long neighborOsmId = osmNodeIds.get(idxNeighbor);
				int internalNeighborId = nodeMap.get(neighborOsmId);
				double latNeighbor = readerCntx.getNodeLatitude(internalNeighborId);
				double lonNeighbor = readerCntx.getNodeLongitude(internalNeighborId);
				double distance = distCalc.calcDist(latMain, lonMain, latNeighbor, lonNeighbor);
				graphStorage.edge(idxMain, idxNeighbor).setDistance(distance);
				// iterate through remaining nodes,
				// but not through the direct neighbors
				for (int idxPartner = idxMain + 2; idxPartner < osmNodeIds.size() - 1; idxPartner++) {
					long partnerOsmId = osmNodeIds.get(idxPartner);
					int internalPartnerId = nodeMap.get(partnerOsmId);
					// coordinates of second nodes
					double latPartner = readerCntx.getNodeLatitude(internalPartnerId);
					double lonPartner = readerCntx.getNodeLongitude(internalPartnerId);
					// connect nodes
					LineString ls = geometryFactory.createLineString(new Coordinate[]{new Coordinate(lonMain, latMain), new Coordinate(lonPartner, latPartner)});
					// check if new edge is within open space
					if (ls.within(openSpace)) {
						// compute distance between nodes
						distance = distCalc.calcDist(latMain, lonMain, latPartner, lonPartner);
						// the index number of the nodes in the local network
						// necessary, because it does not accept big values
						// fill
						intId2idx.put(internalMainId, idxMain);
						intId2idx.put(internalPartnerId, idxPartner);
						// fill
						idx2intId.put(idxMain, internalMainId);
						idx2intId.put(idxPartner, internalPartnerId);
						// add edge to local graph
						graphStorage.edge(idxMain, idxPartner).setDistance(distance);
					}
				}
			}

			// a set with all created edges.
			// the nodes which create the edge are stored in a ArrayList.
			// it is important that the first node is smaller than the second node.
			// TODO maybe a treeset would make the code more elegant
			edges.clear();

			// compute routes between all tower nodes using the local graph
			for (int i = 0; i < internalTowerNodeIds.size(); i++) {
				int internalIdTowerStart = internalTowerNodeIds.get(i);
				// check if tower node is in map
				// it can miss if no edge is starting from here
				if (!intId2idx.containsKey(internalIdTowerStart)) {
					continue;
				}
				int idxTowerStart = intId2idx.get(internalIdTowerStart);
				for (int j = i + 1; j < internalTowerNodeIds.size(); j++) {
					int internalIdTowerDestination = internalTowerNodeIds.get(j);
					// check if tower node is in map
					// it can miss if no edge is starting from here
					if (!intId2idx.containsKey(internalIdTowerDestination)) {
						continue;
					}
					int idxTowerDest = intId2idx.get(internalIdTowerDestination);
					// compute route between tower nodes
					try {
						Dijkstra dijkstra = new Dijkstra(graphStorage, weightings.get(0), TraversalMode.EDGE_BASED);
						Path path = dijkstra.calcPath(idxTowerStart, idxTowerDest);
						IntIndexedContainer pathNodes = path.calcNodes();
						// iterate through nodes of routing result
						for (int k = 0; k < pathNodes.size() - 1; k++) {
							// local index
							int idxNodeA = pathNodes.get(k);
							int idxNodeB = pathNodes.get(k + 1);
							// internal Node IDs
							int nodeA = idx2intId.get(idxNodeA);
							int nodeB = idx2intId.get(idxNodeB);
							// add to nodes to array sorted
							int minNode = Integer.min(nodeA, nodeB);
							int maxNode = Integer.max(nodeA, nodeB);
							tmpEdge.clear();
							tmpEdge.add(minNode);
							tmpEdge.add(maxNode);
							boolean edgeIsNew = edges.add(tmpEdge);
							if (edgeIsNew) {
								// it is necessary to get the long node OSM IDs...
								long osmNodeA = intId2osmId.get(minNode);
								long osmNodeB = intId2osmId.get(maxNode);
								addNodePairAsEdgeToGraph(readerCntx, way.getId(), wayFlags, createdEdges, osmNodeA, osmNodeB);
							}
						}
					} catch (Exception ex) {
						// do nothing
					}
				}
			}

			// TODO this loop can maybe be integrated at the part where the boundary edges are handled alread<
			// add boundary of open space
			for (int i = 0; i < osmNodeIds.size() - 1; i++) {
				long osmIdA = osmNodeIds.get(i);
				long osmIdB = osmNodeIds.get(i + 1);
				int internalIdA = nodeMap.get(osmIdA);
				int internalIdB = nodeMap.get(osmIdB);
				// add to nodes to array sorted
				int minIntId = Integer.min(internalIdA, internalIdB);
				int maxIndId = Integer.max(internalIdA, internalIdB);
				// create a boundary edge
				tmpEdge.clear();
				tmpEdge.add(minIntId);
				tmpEdge.add(maxIndId);
				// test if already exists
				boolean edgeIsNew = edges.add(tmpEdge);
				if (edgeIsNew) {
					// edge is added to global GraphHopper graph
					addNodePairAsEdgeToGraph(readerCntx, way.getId(), wayFlags, createdEdges, osmIdA, osmIdB);
				}
			}
		}
		return true;
	}

	private void addNodePairAsEdgeToGraph(DataReaderContext readerCntx, long wayOsmId, IntsRef wayFlags,  List<EdgeIteratorState> createdEdges, long node1, long node2) {
		// list which contains the Nodes of the new Edge     
		LongArrayList subgraphNodes = new LongArrayList(5);  
		subgraphNodes.add(node1);
		subgraphNodes.add(node2);
		createdEdges.addAll(readerCntx.addWay(subgraphNodes, wayFlags, wayOsmId));   
	}

	private Polygon osmPolygon2JTS(DataReaderContext readerCntx, LongArrayList osmNodeIds) {     
		// collect all coordinates in ArrayList       
		if (coordinates == null || coordinates.length < osmNodeIds.size())
			coordinates = new Coordinate[osmNodeIds.size()];

		for (int i = 0; i < osmNodeIds.size(); i++) 
		{      
			long osmNodeId = osmNodeIds.get(i);       
			int internalID = readerCntx.getNodeMap().get(osmNodeId);   
			coordinates[i] = new Coordinate(readerCntx.getNodeLongitude(internalID),  readerCntx.getNodeLatitude(internalID));
		}  

		Coordinate[] coords  = Arrays.copyOf(coordinates, osmNodeIds.size());
		LinearRing ring = geometryFactory.createLinearRing(coords);     
		// a JTS polygon consists of a ring and holes
		return geometryFactory.createPolygon(ring, null);
	}

	@Override
	public void finish() {
		// do nothing
	}

	/* * checks if the OSM way is an open space      *      
	 * 
	 * @param way      
	 * @param osmNodeIds      
	 * @return      */ 
	private boolean hasOpenSpace(ReaderWay way, LongArrayList osmNodeIds) 
	{    
		long firstNodeId = osmNodeIds.get(0);        
		long lastNodeId = osmNodeIds.get(osmNodeIds.size() - 1);       
		return (firstNodeId == lastNodeId) && way.hasTag("area", "yes");    
	} 

	@Override
	public String getName() {
		return "InField";
	}
}
