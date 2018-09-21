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
package heigit.ors.services.optimization.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.BBox;

import heigit.ors.common.StatusCode;
import heigit.ors.config.AppConfig;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.optimization.OptimizationErrorCodes;
import heigit.ors.optimization.RouteOptimizationRequest;
import heigit.ors.optimization.RouteOptimizationResult;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.optimization.OptimizationServiceSettings;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.AppInfo;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FileUtility;


public class JsonOptimizationRequestProcessor extends AbstractHttpRequestProcessor 
{
	public JsonOptimizationRequestProcessor(HttpServletRequest request) throws Exception
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception 
	{
		String reqMethod = _request.getMethod();

		RouteOptimizationRequest req = null;
		
		switch (reqMethod)
		{
		case "GET":
			req = JsonOptimizationRequestParser.parseFromRequestParams(_request);
			break;
		case "POST": 
			req = JsonOptimizationRequestParser.parseFromStream(_request.getInputStream());  
			break;
		default:
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED);
		}

		if (req == null)
			throw new StatusCodeException(StatusCode.BAD_REQUEST, OptimizationErrorCodes.UNKNOWN, "RouteOptimizationRequest object is null.");
		
		if (OptimizationServiceSettings.getMaximumLocations() > 0 && req.getLocationsCount() > OptimizationServiceSettings.getMaximumLocations())
			throw new ParameterOutOfRangeException(OptimizationErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "locations", Integer.toString(req.getLocationsCount()), Integer.toString(OptimizationServiceSettings.getMaximumLocations()));
		
		RouteOptimizationResult optResult = RoutingProfileManager.getInstance().computeOptimizedRoutes(req);
		
		writeResponse(response, req, optResult);
	}
	
	private void writeResponse(HttpServletResponse response, RouteOptimizationRequest request, RouteOptimizationResult optResult) throws Exception
	{
		JSONObject jResp = new JSONObject(true);
		
		RoutingRequest reqRoute = request.createRoutingRequest(optResult.getWayPoints());

		BBox bbox = new BBox(0, 0, 0, 0);
		JSONArray jRoutes = JsonRoutingResponseWriter.toJsonArray(reqRoute, new RouteResult[] { optResult.getRouteResult() }, bbox);
		jResp.put("routes", jRoutes);
		
		JSONArray jWayPoints = new JSONArray();
		jWayPoints.put(0, new JSONArray(optResult.getWayPoints()));
        jResp.put("way_points", jWayPoints);
        
		if (bbox != null)
			jResp.put("bbox", GeometryJSON.toJSON(bbox.minLon, bbox.minLat, bbox.maxLon, bbox.maxLat));
		
		JSONObject jQuery = new JSONObject();

		jQuery.put("profile", RoutingProfileType.getName(request.getProfileType()));

		if (request.getUnits() != null)
			jQuery.put("units", DistanceUnitUtil.toString(request.getUnits()));
		
		/*if (request.getWeightingMethod() != null)
			jQuery.put("preference", request.getWeightingMethod());*/

		if (request.getId() != null)
			jQuery.put("id", request.getId());

		JSONObject jInfo = new JSONObject(true);
		jInfo.put("service", "optimization");
		jInfo.put("engine", AppInfo.getEngineInfo());
		if (!Helper.isEmpty(OptimizationServiceSettings.getAttribution()))
			jInfo.put("attribution", OptimizationServiceSettings.getAttribution());
		jInfo.put("timestamp", System.currentTimeMillis());

		if (AppConfig.hasValidMD5Hash())
			jInfo.put("osm_file_md5_hash", AppConfig.getMD5Hash());

		jInfo.put("query", jQuery);
		jResp.put("info", jInfo);
		
		ServletUtility.write(response, jResp);
	}
}
