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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.OSMWay;
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

	public void processWay(OSMWay way) {
		waySurfaceDesc.Reset();

		boolean hasHighway = way.containsTag("highway");
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

	public void processEdge(OSMWay way, EdgeIteratorState edge) 
	{
		_storage.setEdgeValue(edge.getEdge(), waySurfaceDesc);
	}

	@Override
	public String getName() {
		return "WaySurfaceType";
	}
}
