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

import heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import heigit.ors.routing.RoutingProfile;

import com.graphhopper.reader.OSMNode;
import com.graphhopper.reader.OSMReader;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.reader.dem.HeightTile;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;

public class ORSOSMReader extends OSMReader {

	private static Logger LOGGER = Logger.getLogger(ORSOSMReader.class.getName());

	private Envelope bbox;
	private HashMap<Integer, Long> tmcEdges;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds;
	private RoutingProfile refProfile;
	private boolean enrichInstructions;

	private List<GraphStorageBuilder> _storageBuilders;

	private String[] TMC_ROAD_TYPES = new String[] { "motorway", "motorway_link", "trunk", "trunk_link", "primary",
			"primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "residential" };

	public ORSOSMReader(GraphHopperStorage storage, Envelope bbox,  List<GraphStorageBuilder> storageBuilders, HashMap<Integer, Long> tmcEdges,  HashMap<Long, ArrayList<Integer>> osmId2EdgeIds, RoutingProfile refProfile) {
		super(storage);

		this._storageBuilders = storageBuilders;
		this.bbox = bbox;
		this.tmcEdges = tmcEdges;
		this.osmId2EdgeIds = osmId2EdgeIds;
		this.refProfile = refProfile;

		enrichInstructions = (refProfile != null) && (storage.getEncodingManager().supports("foot")
				|| storage.getEncodingManager().supports("bike")  
				|| storage.getEncodingManager().supports("MTB")
				|| storage.getEncodingManager().supports("RACINGBIKE")
				|| storage.getEncodingManager().supports("SAFETYBIKE"));


		HeightTile.CUSTOM_ROUND = true;
	}

	public OSMReader setEncodingManager( EncodingManager em )
	{
		super.setEncodingManager(em);

		return this;
	}

	@Override
	protected boolean isInBounds(OSMNode node) {
		if (bbox != null) {
			double x = node.getLon();
			double y = node.getLat();

			return bbox.contains(x, y);
		}

		return super.isInBounds(node);
	}

	@Override
	public void onProcessWay(OSMWay way) {
		try
		{
			if (_storageBuilders != null)
			{
				int nStorages = _storageBuilders.size();
				if (nStorages > 0)
				{
					for (int i = 0; i < nStorages; ++i)
					{
						GraphStorageBuilder gsBuilder = _storageBuilders.get(i);
						gsBuilder.processWay(way);
					}
				}
			}
		}
		catch(Exception ex)
		{
			LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
		}
	}

	protected void onProcessEdge(OSMWay way, EdgeIteratorState edge) {

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

			if (_storageBuilders != null)
			{
				int nStorages = _storageBuilders.size();
				if (nStorages > 0)
				{
					for (int i = 0; i < nStorages; ++i)
					{
						GraphStorageBuilder gsBuilder = _storageBuilders.get(i);
						gsBuilder.processEdge(way, edge);
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
		}
	}
	
	@Override 
	protected void finishedReading() {
		
		// System.out.println("----------  ORSOSMReader.finishedReading()");
		super.finishedReading();
		
		if (_storageBuilders != null)
		{
			int nStorages = _storageBuilders.size();
			if (nStorages > 0)
			{
				for (int i = 0; i < nStorages; ++i)
				{
					GraphStorageBuilder gsBuilder = _storageBuilders.get(i);
					gsBuilder.finish();
				}
			}
		}
	}
}
