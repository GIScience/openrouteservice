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
package heigit.ors.services.geocoding.requestprocessors;

import javax.servlet.http.HttpServletRequest;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.geocoding.geocoders.GeocodingErrorCodes;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.services.geocoding.GeocodingServiceSettings;
import heigit.ors.services.geocoding.requestprocessors.json.JsonGeocodingRequestProcessor;

import com.graphhopper.util.Helper;

public class GeocodingServiceRequestProcessorFactory {

	public static AbstractHttpRequestProcessor createProcessor(HttpServletRequest request) throws Exception  
	{
		if (!GeocodingServiceSettings.getEnabled())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, GeocodingErrorCodes.UNKNOWN, "Geocoding service is not enabled.");
		
		String formatParam = request.getParameter("format");

		if (Helper.isEmpty(formatParam))
			formatParam = "json";

		if (formatParam.equalsIgnoreCase("json") || formatParam.equalsIgnoreCase("gpx"))
			return new JsonGeocodingRequestProcessor(request);
	/*	else if (formatParam.equalsIgnoreCase("xml"))
			return new XmlGeocodingRequestProcessor(request);*/
		else 
			throw new UnknownParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "format", formatParam);
	}
}
