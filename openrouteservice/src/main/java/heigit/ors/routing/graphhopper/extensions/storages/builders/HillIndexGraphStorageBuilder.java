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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;
import heigit.ors.routing.util.HillIndexCalculator;

public class HillIndexGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private HillIndexGraphStorage _storage;
	private HillIndexCalculator _hillIndexCalc;
	
	public HillIndexGraphStorageBuilder()
	{
		
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		if (graphhopper.hasElevation())
		{
			_storage = new HillIndexGraphStorage();
			_hillIndexCalc = new HillIndexCalculator();
			
			return _storage;
		}
		else 
			return null;
	}

	public void processWay(OSMWay way) {
		// TODO Auto-generated method stub
		
	}

	public void processEdge(OSMWay way, EdgeIteratorState edge) {
		boolean revert = edge.getBaseNode() > edge.getAdjNode();

		PointList points = edge.fetchWayGeometry(3);
	
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
