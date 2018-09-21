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
package heigit.ors.services.matrix.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import heigit.ors.routing.RoutingProfilesCollection;
import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.StatusCode;
import heigit.ors.config.AppConfig;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.ResolvedLocation;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.AppInfo;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FormatUtility;

public class JsonMatrixRequestProcessor extends AbstractHttpRequestProcessor 
{
	public JsonMatrixRequestProcessor(HttpServletRequest request) throws Exception {
		super(request);
	}

    /*
    @Override
    public int getMethodNotSupportedErrorCode() {
        return -1;
    }

    @Override
    public int getInvalidJsonFormatErrorCode() {
        return MatrixErrorCodes.INVALID_JSON_FORMAT;
    }

    @Override
    public int getInvalidParameterFormatErrorCode() {
        return MatrixErrorCodes.INVALID_PARAMETER_FORMAT;
    }

    @Override
    public int getMissingParameterErrorCode() {
        return MatrixErrorCodes.MISSING_PARAMETER;
    }

    @Override
    public void process(AbstractHttpRequestProcessor.MixedRequestParameters map, HttpServletResponse response) throws Exception {
        MatrixRequest req = JsonMatrixRequestParser.parseFromRequestParams(map);

        if (req == null) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, MatrixErrorCodes.UNKNOWN, "MatrixRequest object is null.");
        }

        boolean flexibleMode = req.getFlexibleMode() ? true : !RoutingProfileManager.getInstance().getProfiles().isCHProfileAvailable(req.getProfileType());
        if (MatrixServiceSettings.getMaximumLocations(flexibleMode) > 0 && req.getTotalNumberOfLocations() > MatrixServiceSettings.getMaximumLocations(flexibleMode)) {
            throw new ParameterOutOfRangeException(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "sources/destinations", Integer.toString(req.getTotalNumberOfLocations()), Integer.toString(MatrixServiceSettings.getMaximumLocations(flexibleMode)));
        }
        MatrixResult mtxResult = RoutingProfileManager.getInstance().computeMatrix(req);

        writeResponse(response, req, mtxResult);
    }
    */

	@Override
	public void process(HttpServletResponse response) throws Exception 
	{
		String reqMethod = _request.getMethod();

		MatrixRequest req = null;
		switch (reqMethod)
		{
		case "GET":
			req = JsonMatrixRequestParser.parseFromRequestParams(_request);
			break;
		case "POST": 
			req = JsonMatrixRequestParser.parseFromStream(_request.getInputStream());  
			break;
		default:
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED);
		}

		if (req == null)
			throw new StatusCodeException(StatusCode.BAD_REQUEST, MatrixErrorCodes.UNKNOWN, "MatrixRequest object is null.");
		boolean flexibleMode = req.getFlexibleMode() ? true : !RoutingProfileManager.getInstance().getProfiles().isCHProfileAvailable(req.getProfileType());
		if (MatrixServiceSettings.getMaximumLocations(flexibleMode) > 0 && req.getTotalNumberOfLocations() > MatrixServiceSettings.getMaximumLocations(flexibleMode))
			throw new ParameterOutOfRangeException(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "sources/destinations", Integer.toString(req.getTotalNumberOfLocations()), Integer.toString(MatrixServiceSettings.getMaximumLocations(flexibleMode)));
		
		MatrixResult mtxResult = RoutingProfileManager.getInstance().computeMatrix(req);
		
		writeResponse(response, req, mtxResult);
	}
	
	private void writeResponse(HttpServletResponse response, MatrixRequest request, MatrixResult mtxResult) throws Exception
	{
		JSONObject jResp = new JSONObject(true);
		
		if (MatrixMetricsType.isSet(request.getMetrics(), MatrixMetricsType.Distance))
			jResp.put("distances", createTable(mtxResult.getTable(MatrixMetricsType.Distance), request.getSources().length, request.getDestinations().length));
		if (MatrixMetricsType.isSet(request.getMetrics(), MatrixMetricsType.Duration))
			jResp.put("durations", createTable(mtxResult.getTable(MatrixMetricsType.Duration), request.getSources().length, request.getDestinations().length));
		if (MatrixMetricsType.isSet(request.getMetrics(), MatrixMetricsType.Weight))
			jResp.put("weights", createTable(mtxResult.getTable(MatrixMetricsType.Weight), request.getSources().length, request.getDestinations().length));		

		jResp.put("destinations", createLocations(mtxResult.getDestinations(), request.getResolveLocations()));
		jResp.put("sources", createLocations(mtxResult.getSources(), request.getResolveLocations()));
		
		JSONObject jInfo = new JSONObject(true);
		jInfo.put("service", "matrix");
		jInfo.put("engine", AppInfo.getEngineInfo());
		if (!Helper.isEmpty(MatrixServiceSettings.getAttribution()))
			jInfo.put("attribution", MatrixServiceSettings.getAttribution());
		jInfo.put("timestamp", System.currentTimeMillis());

		if (AppConfig.hasValidMD5Hash())
			jInfo.put("osm_file_md5_hash", AppConfig.getMD5Hash());

		JSONObject jQuery = new JSONObject();

		jQuery.put("profile", RoutingProfileType.getName(request.getProfileType()));

		if (request.getUnits() != null)
			jQuery.put("units", DistanceUnitUtil.toString(request.getUnits()));
		
		if (request.getWeightingMethod() != null)
			jQuery.put("preference", request.getWeightingMethod());

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
