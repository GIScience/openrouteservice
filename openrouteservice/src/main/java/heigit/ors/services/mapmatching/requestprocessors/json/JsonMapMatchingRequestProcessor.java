/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
