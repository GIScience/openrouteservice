package org.freeopenls.routeservice.routing;

import java.util.ArrayList;
import java.util.HashMap;

public class RouteProfilesCollection {
	private HashMap<Integer, RouteProfile> m_routeProfiles;
	private ArrayList<RouteProfile> m_uniqueProfiles;

	public RouteProfilesCollection() {
		m_routeProfiles = new HashMap<Integer, RouteProfile>();
		m_uniqueProfiles = new ArrayList<RouteProfile>();
	}

	public void destroy() {
		for (RouteProfile rp : m_uniqueProfiles) {
			rp.close();
		}
		
		m_routeProfiles.clear();	
	}
	
	public ArrayList<RouteProfile> getUniqueProfiles()
	{
		return m_uniqueProfiles;
	}
	
	public int size()
	{
		return m_uniqueProfiles.size();
	}
	
	public void clear() {
		m_routeProfiles.clear();
		m_uniqueProfiles.clear();
	}
	
	public boolean add(RouteProfile rp )
	{
		boolean result = true;
		
		synchronized (m_uniqueProfiles) {
			m_uniqueProfiles.add(rp);

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

	public boolean add(int routePref, RouteProfile rp, boolean isUnique) {
		synchronized (m_routeProfiles) {
			
			int key = getRoutePreferenceKey(routePref, rp.isCHEnabled(), rp.hasDynamicWeights());

			if (!m_routeProfiles.containsKey(key))
			{
				m_routeProfiles.put(key, rp);
				if (isUnique)
					m_uniqueProfiles.add(rp);
			}
			else
				return false;
		}

		return true;
	}

	public ArrayList<RouteProfile> getCarProfiles() {
		ArrayList<RouteProfile> result = new ArrayList<RouteProfile>();
		for (RouteProfile rp : m_routeProfiles.values()) {
			if (rp.hasCarPreferences())
				result.add(rp);
		}

		return result;
	}

	public RouteProfile getRouteProfile(int routePref, boolean chEnabled, boolean dynamicWeights, double lat0,
			double lon0, double lat1, double lon1, boolean checkDistance) throws Exception {

		int routePrefKey = getRoutePreferenceKey(routePref, chEnabled, dynamicWeights);
		RouteProfile rp = getRouteProfileByKey(routePrefKey);

		if (rp != null)
		{
			if (!checkDistance || rp.canProcessRequest(lat0, lon0, lat1, lon1))
				return rp;
		}
		
		return null;
	}

	public RouteProfile getRouteProfile(int routePref, boolean chEnabled, boolean dynamicWeights) throws Exception {
		int routePrefKey = getRoutePreferenceKey(routePref, chEnabled, dynamicWeights);
		if (!m_routeProfiles.containsKey(routePrefKey))
			return null;
		else {
			RouteProfile rp = m_routeProfiles.get(routePrefKey);
			return rp;
		}
	}
	
	public RouteProfile getRouteProfile(int routePref) throws Exception
	{
		RouteProfile rp = getRouteProfileByKey(getRoutePreferenceKey(routePref, false, true));
		if (rp == null)
			rp = getRouteProfileByKey(getRoutePreferenceKey(routePref, false, false));
		
		return rp;
	}

	private RouteProfile getRouteProfileByKey(int routePrefKey) throws Exception {
		if (!m_routeProfiles.containsKey(routePrefKey))
			return null;
		else {
			RouteProfile rp = m_routeProfiles.get(routePrefKey);
			return rp;
		}
	}

	private int getRoutePreferenceKey(int routePref, boolean chEnabled, boolean dynamicWeights) {
		int key = routePref;
		if (chEnabled)
			key += 100;
		else if (dynamicWeights)
			key += 1000;

		return key;
	}
}
