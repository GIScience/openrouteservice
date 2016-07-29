package org.freeopenls.routeservice.routing;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.freeopenls.routeservice.routing.configuration.RouteProfileConfiguration;
import org.freeopenls.routeservice.traffic.RealTrafficDataProvider;
import org.freeopenls.tools.FormatUtility;
import org.freeopenls.tools.StringUtility;
import org.json.JSONException;
import org.json.JSONObject;

import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.Constants;
import com.graphhopper.util.shapes.BBox;

import static javax.servlet.http.HttpServletResponse.*;

public class RouteInfoUtils {

	public static void writeRouteInfo(HttpServletRequest req, HttpServletResponse res) throws JSONException,
			IOException {
		RouteProfileManager profileManager = RouteProfileManager.getInstance();

		Map<String, Object> props = new LinkedHashMap<String, Object>();
		// JSONObject json = (JSONObject)obj; // unordered sequence of elements

		if (profileManager.getProfiles().getUniqueProfiles().size() > 0) {
			BBox bb = profileManager.getProfiles().getUniqueProfiles().get(0).getBounds();
			List<String> list = new ArrayList<String>(4);
			list.add(FormatUtility.formatValue(bb.minLon));
			list.add(FormatUtility.formatValue(bb.minLat));
			list.add(FormatUtility.formatValue(bb.maxLon));
			list.add(FormatUtility.formatValue(bb.maxLat));
 
			props.put("bbox", list);
			props.put("gh_version", Constants.VERSION);
			props.put("gh_build_date", Constants.BUILD_DATE);

			if (profileManager.updateEnabled()) {
				props.put("next_update", formatDateTime(profileManager.getNextUpdateTime()));
				String status = profileManager.getUpdatedStatus();
				if (status != null)
					props.put("update_status", status);
			}

			Map<String, Object> profiles = new LinkedHashMap<String, Object>();
			int i = 1;

			for (RouteProfile rp : profileManager.getProfiles().getUniqueProfiles()) {
				RouteProfileConfiguration rpc = rp.getConfiguration();
				Map<String, Object> profileProps = new LinkedHashMap<String, Object>();

				profileProps.put("preferences", rpc.Preferences);

				if (rpc.MaximumDistance != null && rpc.MaximumDistance > 0) {
					profileProps.put("maxdistance", rpc.MaximumDistance.toString());
				}
				
				if (rpc.MinimumDistance != null && rpc.MinimumDistance > 0) {
					profileProps.put("mindistance", rpc.MinimumDistance.toString());
				}

				if (rpc.DynamicWeighting != null && rpc.DynamicWeighting == true) {
					profileProps.put("custom_attributes", "true");
				}

				StorableProperties storageProps = rp.getGraphProperties();
				profileProps.put("import_date", storageProps.get("osmreader.import.date"));

				if (!StringUtility.isEmpty(storageProps.get("prepare.date"))) {
					profileProps.put("prepare_date", storageProps.get("prepare.date"));
				}

				profiles.put("profile " + Integer.toString(i), new JSONObject(profileProps));
				i++;
			}

			props.put("profiles", new JSONObject(profiles));
		}
		
		if (RealTrafficDataProvider.getInstance().isInitialized())
		{
			Map<String, Object> tmcProps = new LinkedHashMap<String, Object>();
			
			tmcProps.put("update_date", RealTrafficDataProvider.getInstance().getTimeStamp());
			props.put("tmc", new JSONObject(tmcProps));
		}

		writeJson(req, res, new JSONObject(props));
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

	public static void writeResponse(HttpServletResponse res, String str) {
		try {
			res.setStatus(SC_OK);
			res.getWriter().append(str);
		} catch (IOException ex) {
			// logger.error("Cannot write message:" + str, ex);
		}
	}
}
