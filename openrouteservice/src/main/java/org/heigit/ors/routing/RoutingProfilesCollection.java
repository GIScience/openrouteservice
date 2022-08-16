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
package org.heigit.ors.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import org.heigit.ors.util.RuntimeUtility;

public class RoutingProfilesCollection {
	private final HashMap<Integer, RoutingProfile> routeProfiles;
	private final ArrayList<RoutingProfile> uniqueProfiles;

	public RoutingProfilesCollection() {
		routeProfiles = new HashMap<>();
		uniqueProfiles = new ArrayList<>();
	}

	public void destroy() {
		for (RoutingProfile rp : uniqueProfiles) {
			rp.close();
		}
		routeProfiles.clear();
	}
	
	public List<RoutingProfile> getUniqueProfiles() {
		return uniqueProfiles;
	}
	
	public int size()
	{
		return uniqueProfiles.size();
	}
	
	public void clear() {
		routeProfiles.clear();
		uniqueProfiles.clear();
	}
	
	public boolean add(RoutingProfile rp ) {
		boolean result = true;
		synchronized (uniqueProfiles) {
			uniqueProfiles.add(rp);
			Integer[] routePrefs = rp.getPreferences();
			if (routePrefs != null) {
				for (int j = 0; j < routePrefs.length; j++) {
					if (!add(routePrefs[j], rp, false))
						result = false;
				}
			}
		}
		return result;
	}

	public boolean add(int routePref, RoutingProfile rp, boolean isUnique) {
		boolean res = false;

		synchronized (routeProfiles) {
			int key = getRoutePreferenceKey(routePref, rp.isCHEnabled());

			if (!routeProfiles.containsKey(key)) {
				routeProfiles.put(key, rp);
				if (isUnique)
					uniqueProfiles.add(rp);
				
				res = true;
			}
			
			if (rp.isCHEnabled()) {
				//  add the same profile but with dynamic weights
				key = getRoutePreferenceKey(routePref, false);

				if (!routeProfiles.containsKey(key)) {
					routeProfiles.put(key, rp);
					if (isUnique)
						uniqueProfiles.add(rp);
					
					res = true;
				}
			}
		}

		return res;
	}

	public List<RoutingProfile> getCarProfiles() {
		ArrayList<RoutingProfile> result = new ArrayList<>();
		for (RoutingProfile rp : routeProfiles.values()) {
			if (rp.hasCarPreferences())
				result.add(rp);
		}
		return result;
	}

	public RoutingProfile getRouteProfile(int routePref, boolean chEnabled) throws Exception {
		int routePrefKey = getRoutePreferenceKey(routePref, chEnabled);
		//Fall back to non-CH version if CH routing profile does not exist
		if (!routeProfiles.containsKey(routePrefKey)){
			routePrefKey = getRoutePreferenceKey(routePref, false);
			if (!routeProfiles.containsKey(routePrefKey))
				return null;
		}
		return routeProfiles.get(routePrefKey);
	}

	/**
	 	 * Check if the CH graph of the specified profile has been built.
	 	 *
	 	 * @param routePref				The chosen routing profile
	 	 */

	public boolean isCHProfileAvailable(int routePref) throws Exception {
		int routePrefKey = getRoutePreferenceKey(routePref, true);
		return routeProfiles.containsKey(routePrefKey);
	}
	
	public RoutingProfile getRouteProfile(int routePref) throws Exception {
		return getRouteProfileByKey(getRoutePreferenceKey(routePref, false));
	}

	private RoutingProfile getRouteProfileByKey(int routePrefKey) throws Exception {
		return routeProfiles.getOrDefault(routePrefKey, null);
	}

	private int getRoutePreferenceKey(int routePref, boolean chEnabled) {
		int key = routePref;
		if (chEnabled)
			key += 100;
		return key;
	}
	
	public void printStatistics(Logger logger) {
		logger.info("====> Memory usage by profiles:");
		long totalUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long totalProfilesMemory = 0;
		
		int i = 0;
		for(RoutingProfile profile : getUniqueProfiles()) {
			i++;
			long profileMemory = profile.getMemoryUsage();
			totalProfilesMemory += profileMemory;
			logger.info(String.format("[%d] %s (%.1f%%)", i, RuntimeUtility.getMemorySize(profileMemory), ((double)profileMemory/totalUsedMemory)*100));
		}
		logger.info(String.format("Total: %s (%.1f%%)", RuntimeUtility.getMemorySize(totalProfilesMemory), ((double)totalProfilesMemory/totalUsedMemory)*100));
		logger.info("========================================================================");
	}
}
