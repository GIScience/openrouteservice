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
package org.heigit.ors.services.matrix.requestprocessors.json;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.api.util.SystemMessage;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.exceptions.ParameterOutOfRangeException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matrix.*;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.services.matrix.MatrixServiceSettings;
import org.heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import org.heigit.ors.servlet.util.ServletUtility;
import org.heigit.ors.util.AppInfo;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.FormatUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JsonMatrixRequestProcessor extends AbstractHttpRequestProcessor {
	public JsonMatrixRequestProcessor(HttpServletRequest request) throws Exception {
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception {
		String reqMethod = request.getMethod();

		MatrixRequest req;
		switch (reqMethod) {
			case "GET":
				req = JsonMatrixRequestParser.parseFromRequestParams(request);
				break;
			case "POST":
				req = JsonMatrixRequestParser.parseFromStream(request.getInputStream());
				break;
			default:
				throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED);
		}

		boolean flexibleMode = req.getFlexibleMode() || !RoutingProfileManager.getInstance().getProfiles().isCHProfileAvailable(req.getProfileType());
		if (MatrixServiceSettings.getMaximumRoutes(flexibleMode) > 0 && req.getTotalNumberOfLocations() > MatrixServiceSettings.getMaximumRoutes(flexibleMode))
			throw new ParameterOutOfRangeException(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "sources/destinations", Integer.toString(req.getTotalNumberOfLocations()), Integer.toString(MatrixServiceSettings.getMaximumRoutes(flexibleMode)));
		
		MatrixResult mtxResult = RoutingProfileManager.getInstance().computeMatrix(req);
		
		writeResponse(response, req, mtxResult);
	}
	
	private void writeResponse(HttpServletResponse response, MatrixRequest request, MatrixResult mtxResult) throws Exception {
		JSONObject jResp = new JSONObject(true);
		
		if (MatrixMetricsType.isSet(request.getMetrics(), MatrixMetricsType.DISTANCE))
			jResp.put("distances", createTable(mtxResult.getTable(MatrixMetricsType.DISTANCE), request.getSources().length, request.getDestinations().length));
		if (MatrixMetricsType.isSet(request.getMetrics(), MatrixMetricsType.DURATION))
			jResp.put("durations", createTable(mtxResult.getTable(MatrixMetricsType.DURATION), request.getSources().length, request.getDestinations().length));
		if (MatrixMetricsType.isSet(request.getMetrics(), MatrixMetricsType.WEIGHT))
			jResp.put("weights", createTable(mtxResult.getTable(MatrixMetricsType.WEIGHT), request.getSources().length, request.getDestinations().length));

		jResp.put("destinations", createLocations(mtxResult.getDestinations(), request.getResolveLocations()));
		jResp.put("sources", createLocations(mtxResult.getSources(), request.getResolveLocations()));
		
		JSONObject jInfo = new JSONObject(true);
		jInfo.put("service", "matrix");
		jInfo.put("engine", AppInfo.getEngineInfo());
		if (!Helper.isEmpty(MatrixServiceSettings.getAttribution()))
			jInfo.put("attribution", MatrixServiceSettings.getAttribution());
		jInfo.put("timestamp", System.currentTimeMillis());

		jInfo.put("system_message", SystemMessage.getSystemMessage(request));

		if (AppConfig.hasValidMD5Hash())
			jInfo.put("osm_file_md5_hash", AppConfig.getMD5Hash());

		JSONObject jQuery = new JSONObject();

		jQuery.put("profile", RoutingProfileType.getName(request.getProfileType()));

		if (request.getUnits() != null)
			jQuery.put("units", DistanceUnitUtil.toString(request.getUnits()));
		
		if (request.getWeightingMethod() != WeightingMethod.UNKNOWN)
			jQuery.put("preference", WeightingMethod.getName(request.getWeightingMethod()));

		if (request.getId() != null)
			jQuery.put("id", request.getId());

		jInfo.put("query", jQuery);
		jResp.put("info", jInfo);
		
		ServletUtility.write(response, jResp);
	}
	
	private JSONArray createLocations(ResolvedLocation[] locations, boolean includeLocationNames)
	{
		JSONArray jLocations = new JSONArray(locations.length);

		for (int i = 0; i < locations.length; i++)
		{
			JSONObject jLoc = new JSONObject(true);

			ResolvedLocation loc = locations[i];
			if (loc != null)
			{
				Coordinate c = locations[i].getCoordinate();
				JSONArray jCoord = new JSONArray(2);
				jCoord.put(FormatUtility.roundToDecimals(c.x, 6));
				jCoord.put(FormatUtility.roundToDecimals(c.y, 6));
				jLoc.put("location", jCoord);

				if (includeLocationNames && loc.getName() != null)
					jLoc.put("name", loc.getName());

				jLoc.put("snapped_distance",FormatUtility.roundToDecimals( loc.getSnappedDistance(), 2));
			}
			else
				jLoc.put("location", JSONObject.NULL);
			
			jLocations.put(jLoc);
		}
		
		return jLocations;
	}
	
	private JSONArray createTable(float[] values, int rows, int clms)
	{
		JSONArray jMatrix = new JSONArray(rows);
		
		int rowOffset = 0;
		float value = 0;
		
		for (int i = 0; i < rows; ++i)
		{
			JSONArray jRow = new JSONArray(clms);
			rowOffset = i*clms;
			
			for (int j = 0; j < clms; ++j)
			{
				value = values[rowOffset + j];
				if (value == -1)
					jRow.put(JSONObject.NULL);
				else
					jRow.put(FormatUtility.roundToDecimals(value, 2));
			}

			jMatrix.put(jRow);
		}

		return jMatrix;
	}
}
