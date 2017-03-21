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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.OSMNode;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.graphhopper.extensions.WheelchairRestrictionCodes;
import heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;

public class WheelchairGraphStorageBuilder extends AbstractGraphStorageBuilder 
{
	private WheelchairAttributesGraphStorage _storage;
	private int _wayType = 0;
	private boolean hasWheelchairAttributes;
	private boolean hasWheelchairLeftSidewalkAttributes;
	private boolean hasWheelchairRightSidewalkAttributes;
	private double[] passabilityValues = new double[4];
	private double[] wheelchairAttributes = new double[5];
	private double[] wheelchairLeftSidewalkAttributes = new double[5];
	private double[] wheelchairRightSidewalkAttributes = new double[5];
	private List<EdgeIteratorState> wheelchairLeftSidewalkEdges = null;
	private List<EdgeIteratorState> wheelchairRightSidewalkEdges = null;
	private Boolean isWheelchair = false;
	//private HashMap<Long, List<WayWithSidewalk>> sidewalkJunctions;
	
	public WheelchairGraphStorageBuilder()
	{
		//sidewalkJunctions = new HashMap<Long, List<WayWithSidewalk>>();
		// back up from 03.03.2017
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception 
	{
		if (!graphhopper.getEncodingManager().supports("wheelchair"))
		{
			
			return null;
		}
		
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		_storage = new WheelchairAttributesGraphStorage();
		return _storage;
	}

	public void processWay(OSMWay way) 
	{
	}

	public void processEdge(OSMWay way, EdgeIteratorState edge) {
		
			
	}

	@Override
	public String getName() {
		return "Wheelchair";
	}

	@Override
	public void finish() {
		//handleSidewalkJunctions();
	}
}
