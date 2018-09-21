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
package heigit.ors.services.mapmatching.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.graphhopper.util.Helper;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.mapmatching.MapMatchingErrorCodes;
import heigit.ors.mapmatching.MapMatchingRequest;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.services.mapmatching.MapMatchingServiceSettings;
import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;

public class JsonMapMatchingRequestProcessor extends AbstractHttpRequestProcessor {

	public JsonMapMatchingRequestProcessor(HttpServletRequest request) throws Exception 
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception {
		MapMatchingRequest req = JsonMapMatchingRequestParser.parseFromRequestParams(_request);
		
		if (req == null)
			throw new StatusCodeException(StatusCode.BAD_REQUEST, MapMatchingErrorCodes.UNKNOWN, "MapMatchingRequest object is null.");

		if (MapMatchingServiceSettings.getMaximumLocations() > 0 && req.getCoordinates().length > MatrixServiceSettings.getMaximumLocations(false))
			throw new ParameterOutOfRangeException(MapMatchingErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "sources/destinations", Integer.toString(req.getCoordinates().length), Integer.toString(MapMatchingServiceSettings.getMaximumLocations()));

		
		RouteResult result = RoutingProfileManager.getInstance().matchTrack(req);
		
		JSONObject json = null;
		
		String respFormat = _request.getParameter("format");
		if (Helper.isEmpty(respFormat) || "json".equalsIgnoreCase(respFormat))
			json = JsonMapMatchingResponseWriter.toJson(req, new RouteResult[] { result });
		else if ("geojson".equalsIgnoreCase(respFormat))
			json = JsonMapMatchingResponseWriter.toGeoJson(req, new RouteResult[] { result });
		
		ServletUtility.write(response, json, "UTF-8");
	}
}
