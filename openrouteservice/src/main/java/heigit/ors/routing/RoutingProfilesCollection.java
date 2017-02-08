package heigit.ors.routing;

import java.util.ArrayList;
import java.util.HashMap;

public class RoutingProfilesCollection {
	private HashMap<Integer, RoutingProfile> m_routeProfiles;
	private ArrayList<RoutingProfile> m_uniqueProfiles;

	public RoutingProfilesCollection() {
		m_routeProfiles = new HashMap<Integer, RoutingProfile>();
		m_uniqueProfiles = new ArrayList<RoutingProfile>();
	}

	public void destroy() {
		for (RoutingProfile rp : m_uniqueProfiles) {
			rp.close();
		}
		
		m_routeProfiles.clear();	
	}
	
	public ArrayList<RoutingProfile> getUniqueProfiles()
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
	
	public boolean add(RoutingProfile rp )
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

	public boolean add(int routePref, RoutingProfile rp, boolean isUnique) {
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

	public ArrayList<RoutingProfile> getCarProfiles() {
		ArrayList<RoutingProfile> result = new ArrayList<RoutingProfile>();
		for (RoutingProfile rp : m_routeProfiles.values()) {
			if (rp.hasCarPreferences())
				result.add(rp);
		}

		return result;
	}

	public RoutingProfile getRouteProfile(int routePref, boolean chEnabled, boolean dynamicWeights, double lat0,
			double lon0, double lat1, double lon1, boolean checkDistance) throws Exception {

		int routePrefKey = getRoutePreferenceKey(routePref, chEnabled, dynamicWeights);
		RoutingProfile rp = getRouteProfileByKey(routePrefKey);

		if (rp != null)
		{
			if (!checkDistance || rp.canProcessRequest(lat0, lon0, lat1, lon1))
				return rp;
		}
		
		return null;
	}

	public RoutingProfile getRouteProfile(int routePref, boolean chEnabled, boolean dynamicWeights) throws Exception {
		int routePrefKey = getRoutePreferenceKey(routePref, chEnabled, dynamicWeights);
		if (!m_routeProfiles.containsKey(routePrefKey))
			return null;
		else {
			RoutingProfile rp = m_routeProfiles.get(routePrefKey);
			return rp;
		}
	}
	
	public RoutingProfile getRouteProfile(int routePref) throws Exception
	{
		RoutingProfile rp = getRouteProfileByKey(getRoutePreferenceKey(routePref, false, true));
		if (rp == null)
			rp = getRouteProfileByKey(getRoutePreferenceKey(routePref, false, false));
		
		return rp;
	}

	private RoutingProfile getRouteProfileByKey(int routePrefKey) throws Exception {
		if (!m_routeProfiles.containsKey(routePrefKey))
			return null;
		else {
			RoutingProfile rp = m_routeProfiles.get(routePrefKey);
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
