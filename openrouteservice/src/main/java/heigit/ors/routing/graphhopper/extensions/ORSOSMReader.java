/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov

package heigit.ors.routing.graphhopper.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import heigit.ors.routing.RoutingProfile;

import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;

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

	public ORSOSMReader(GraphHopperStorage storage, GraphProcessContext procCntx, HashMap<Integer, Long> tmcEdges,  HashMap<Long, ArrayList<Integer>> osmId2EdgeIds, RoutingProfile refProfile) {
		super(storage);

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
