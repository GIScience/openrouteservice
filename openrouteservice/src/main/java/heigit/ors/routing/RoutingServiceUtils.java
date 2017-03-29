package heigit.ors.routing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import heigit.ors.localization.LocalizationManager;
import heigit.ors.routing.configuration.RouteProfileConfiguration;
import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.services.geocoding.GeocodingServiceSettings;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.AppInfo;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.OrderedJSONObjectFactory;
import heigit.ors.util.StringUtility;

import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.shapes.BBox;

import static javax.servlet.http.HttpServletResponse.*;

public class RoutingServiceUtils {

	public static void writeRouteInfo(HttpServletRequest req, HttpServletResponse res) throws Exception {
		RoutingProfileManager profileManager = RoutingProfileManager.getInstance();

		JSONObject props = OrderedJSONObjectFactory.create();

		if (profileManager.getProfiles().getUniqueProfiles().size() > 0) {
			
			List<String> list = new ArrayList<String>(4);
			if (RoutingServiceSettings.getEnabled())
				list.add("routing");
			if (GeocodingServiceSettings.getEnabled())
				list.add("geocoding");
			if (IsochronesServiceSettings.getEnabled())
				list.add("isochrones");
			props.put("services", list);
			props.put("languages", LocalizationManager.getInstance().getLanguages());

			BBox bb = profileManager.getProfiles().getUniqueProfiles().get(0).getBounds();
			list.clear();
			list.add(FormatUtility.formatValue(bb.minLon));
			list.add(FormatUtility.formatValue(bb.minLat));
			list.add(FormatUtility.formatValue(bb.maxLon));
			list.add(FormatUtility.formatValue(bb.maxLat));

			props.put("bbox", list);
			
			JSONObject appInfo = new JSONObject();
			
			appInfo.put("version", AppInfo.VERSION);
			appInfo.put("build_date", AppInfo.BUILD_DATE);
			
			props.put("app_info", appInfo);

			if (profileManager.updateEnabled()) {
				props.put("next_update", formatDateTime(profileManager.getNextUpdateTime()));
				String status = profileManager.getUpdatedStatus();
				if (status != null)
					props.put("update_status", status);
			}

			Map<String, Object> profiles = new LinkedHashMap<String, Object>();
			int i = 1;

			if (RoutingServiceSettings.getParameter("dynamic_weighting_max_distance") !=  null)
				profiles.put("dynamic_weighting_max_distance", RoutingServiceSettings.getParameter("dynamic_weighting_max_distance"));

			for (RoutingProfile rp : profileManager.getProfiles().getUniqueProfiles()) {
				RouteProfileConfiguration rpc = rp.getConfiguration();
				Map<String, Object> profileProps = new LinkedHashMap<String, Object>();

				profileProps.put("profiles", rpc.Profiles);

				if (rpc.MaximumDistance != null && rpc.MaximumDistance > 0) {
					profileProps.put("maxdistance", rpc.MaximumDistance.toString());
				}
				
				if (rpc.MinimumDistance != null && rpc.MinimumDistance > 0) {
					profileProps.put("mindistance", rpc.MinimumDistance.toString());
				}

				if (rpc.ExtStorages != null) {
					profileProps.put("storages", rpc.ExtStorages);
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

		writeJson(req, res, props);
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
