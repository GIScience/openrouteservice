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
package heigit.ors.services;

import com.graphhopper.storage.StorableProperties;
import heigit.ors.localization.LocalizationManager;
import heigit.ors.routing.RoutingProfile;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingProfileManagerStatus;
import heigit.ors.routing.configuration.RouteProfileConfiguration;
import heigit.ors.routing.traffic.RealTrafficDataProvider;
//import heigit.ors.services.accessibility.AccessibilityServiceSettings;
import heigit.ors.services.geocoding.GeocodingServiceSettings;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.services.mapmatching.MapMatchingServiceSettings;
import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.AppInfo;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class ORSServicesUtils {

	public static void writeStatusInfo(HttpServletRequest req, HttpServletResponse res) throws Exception {

		JSONObject jInfo = new JSONObject(true);

		jInfo.put("engine", AppInfo.getEngineInfo());

		if (RoutingProfileManagerStatus.isReady())
		{
			RoutingProfileManager profileManager = RoutingProfileManager.getInstance();

			if (profileManager.getProfiles().getUniqueProfiles().size() > 0) {

				List<String> list = new ArrayList<String>(4);
				if (RoutingServiceSettings.getEnabled())
					list.add("routing");
				if (GeocodingServiceSettings.getEnabled())
					list.add("geocoding");
				if (IsochronesServiceSettings.getEnabled())
					list.add("isochrones");
				/*if (AccessibilityServiceSettings.getEnabled())
					list.add("accessibility");*/
				if (MatrixServiceSettings.getEnabled())
					list.add("matrix");
				if (MapMatchingServiceSettings.getEnabled())
					list.add("mapmatching");
				jInfo.put("services", list);
				jInfo.put("languages", LocalizationManager.getInstance().getLanguages());

				if (profileManager.updateEnabled()) {
					jInfo.put("next_update", formatDateTime(profileManager.getNextUpdateTime()));
					String status = profileManager.getUpdatedStatus();
					if (status != null)
						jInfo.put("update_status", status);
				}

				JSONObject jProfiles = new JSONObject(true);
				int i = 1;

				for (RoutingProfile rp : profileManager.getProfiles().getUniqueProfiles()) {
					RouteProfileConfiguration rpc = rp.getConfiguration();
					JSONObject jProfileProps = new JSONObject(true);

					jProfileProps.put("profiles", rpc.getProfiles());
					StorableProperties storageProps = rp.getGraphProperties();
					jProfileProps.put("creation_date", storageProps.get("osmreader.import.date"));

					if (rpc.getExtStorages() != null && rpc.getExtStorages().size() > 0) 
						jProfileProps.put("storages", rpc.getExtStorages());

					JSONObject jProfileLimits = new JSONObject(true);
					if (rpc.getMaximumDistance() > 0) 
						jProfileLimits.put("maximum_distance", rpc.getMaximumDistance());

					if (rpc.getMaximumDistanceDynamicWeights() > 0)
						jProfileLimits.put("maximum_distance_dynamic_weights", rpc.getMaximumDistanceDynamicWeights());

					if (rpc.getMaximumDistanceAvoidAreas() > 0)
						jProfileLimits.put("maximum_distance_avoid_areas", rpc.getMaximumDistanceAvoidAreas());

					if (rpc.getMaximumWayPoints() > 0) 
						jProfileLimits.put("maximum_waypoints", rpc.getMaximumWayPoints());

					if (jProfileLimits.length() > 0)
						jProfileProps.put("limits", jProfileLimits);

					jProfiles.put("profile " + Integer.toString(i), jProfileProps);

					i++;
				}

				jInfo.put("profiles", jProfiles);
			}

			if (RealTrafficDataProvider.getInstance().isInitialized())
			{
				JSONObject jTrafficInfo = new JSONObject(true);
				jTrafficInfo.put("update_date", RealTrafficDataProvider.getInstance().getTimeStamp());
				jInfo.put("tmc", jTrafficInfo);
			}
		}
		else
		{
			// TODO
		}

		writeJson(req, res, jInfo);
	}

	private static void writeJson(HttpServletRequest req, HttpServletResponse res, JSONObject json)
			throws JSONException, IOException {
		String type = getParam(req, "type", "json");
		res.setCharacterEncoding("UTF-8");
		boolean debug = getBooleanParam(req, "debug", false) || getBooleanParam(req, "pretty", false);
		if ("jsonp".equals(type)) {
			res.setContentType("application/javascript");

			String callbackName = getParam(req, "callback", null);
			if (callbackName == null) {
				res.sendError(SC_BAD_REQUEST, "No callback provided, necessary if type=jsonp");
				return;
			}

			if (debug) {
				writeResponse(res, callbackName + "(" + json.toString(2) + ")");
			} else {
				writeResponse(res, callbackName + "(" + json.toString() + ")");
			}
		} else {
			res.setContentType("application/json");
			if (debug) {
				writeResponse(res, json.toString(2));
			} else {
				writeResponse(res, json.toString());
			}
		}
	}

	private static String formatDateTime(Date date )
	{
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
	}

	protected static boolean getBooleanParam(HttpServletRequest req, String string, boolean _default) {
		try {
			return Boolean.parseBoolean(getParam(req, string, "" + _default));
		} catch (Exception ex) {
			return _default;
		}
	}

	protected static String getParam(HttpServletRequest req, String string, String _default) {
		String[] l = req.getParameterMap().get(string);
		if (l != null && l.length > 0)
			return l[0];

		return _default;
	}

	protected static String[] getParams(HttpServletRequest req, String string) {
		String[] l = req.getParameterMap().get(string);
		if (l != null && l.length > 0) {
			return l;
		}
		return new String[0];
	}

	private static void writeResponse(HttpServletResponse res, String str) {
		try {
			res.setStatus(SC_OK);
			res.getWriter().append(str);
		} catch (IOException ex) {
			// logger.error("Cannot write message:" + str, ex);
		}
	}
}
