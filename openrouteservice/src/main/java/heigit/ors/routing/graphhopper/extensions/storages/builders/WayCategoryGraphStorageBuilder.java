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
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.AvoidFeatureFlags;
import heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;

public class WayCategoryGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private WayCategoryGraphStorage _storage;
	protected final HashSet<String> ferries;
	private int _wayType = 0;
	
	public WayCategoryGraphStorageBuilder()
	{
		ferries = new HashSet<String>(5);
		ferries.add("shuttle_train");
		ferries.add("ferry");
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		_storage = new WayCategoryGraphStorage();
		
		return _storage;
	}

	public void processWay(OSMWay way) {
		_wayType = 0;
		
		boolean hasHighway = way.containsTag("highway");
		boolean isFerryRoute = isFerryRoute(way);
		
		java.util.Iterator<Entry<String, Object>> it = way.getProperties();

		while (it.hasNext()) {
			Map.Entry<String, Object> pairs = it.next();
			String key = pairs.getKey();
			String value = pairs.getValue().toString();

			if (hasHighway || isFerryRoute) {
				if (key.equals("highway")) {
					if (value.equals("motorway") || value.equals("motorway_link"))
					{
						_wayType |= AvoidFeatureFlags.Highways;
					}
					else if (value.equals("steps"))
					{
						_wayType |= AvoidFeatureFlags.Steps;
					}
					else if ("track".equals(value)) {
						String tracktype = way.getTag("tracktype");
						if (tracktype != null
								&& (tracktype.equals("grade1") || tracktype.equals("grade2")
										|| tracktype.equals("grade3") || tracktype.equals("grade4") || tracktype
											.equals("grade5"))) {
						
							_wayType |= AvoidFeatureFlags.Tracks;
						}
					}

				} else if (key.equals("toll") && value.equals("yes")) 
					_wayType |= AvoidFeatureFlags.Tollways;
				/*} else if (gsHeavyVehicleAttrs != null && key.equals("toll:hgv") && value.equals("yes")) {
					wayFlags[1]  |= AvoidFeatureFlags.Tollways;
				*/ else if (key.equals("route") && isFerryRoute) 
					_wayType |= AvoidFeatureFlags.Ferries;
				else if (key.equals("tunnel") && value.equals("yes")) 
					_wayType |= AvoidFeatureFlags.Tunnels;
				else if (key.equals("bridge") && value.equals("yes")) 
					_wayType |= AvoidFeatureFlags.Bridges;
				else if (("ford".equals(key) && value.equals("yes")))
					_wayType |= AvoidFeatureFlags.Fords;
				else if (key.equals("surface")) {
						if (value.equals("paved") || value.equals("asphalt") || value.equals("cobblestone")
								|| value.equals("cobblestone") || value.equals("cobblestone:flattened")
								|| value.equals("sett") || value.equals("concrete")
								|| value.equals("concrete:lanes") || value.equals("concrete:plates")
								|| value.equals("paving_stones") || value.equals("metal") || value.equals("wood"))
						{
							_wayType |= AvoidFeatureFlags.PavedRoads;
						}

						if (value.equals("unpaved") || value.equals("compacted") || value.equals("dirt")
								|| value.equals("earth") || value.equals("fine_gravel") || value.equals("grass")
								|| value.equals("grass_paver") || value.equals("gravel") || value.equals("ground")
								|| value.equals("ice") || value.equals("metal") || value.equals("mud")
								|| value.equals("pebblestone") || value.equals("salt") || value.equals("sand")
								|| value.equals("snow") || value.equals("wood") || value.equals("woodchips"))
						{
							_wayType |= AvoidFeatureFlags.UnpavedRoads;
						}
					}
			}
		}
	}

	public void processEdge(OSMWay way, EdgeIteratorState edge)
	{
		if (_wayType > 0) 
			_storage.setEdgeValue(edge.getEdge(), _wayType);
	}

	private boolean isFerryRoute(OSMWay way) {
		if (way.hasTag("route", ferries)) {

				String motorcarTag = way.getTag("motorcar");
				if (motorcarTag == null)
					motorcarTag = way.getTag("motor_vehicle");

				if (motorcarTag == null && !way.hasTag("foot") && !way.hasTag("bicycle") || "yes".equals(motorcarTag))
					return true;
			
				//return way.hasTag("bicycle", "yes");
				String bikeTag = way.getTag("bicycle");
				if (bikeTag == null && !way.hasTag("foot") || "yes".equals(bikeTag))
					return true;
		}

		return false;
	}
	
	@Override
	public String getName() {
		return "WayCategory";
	}
}
