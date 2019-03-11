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
package heigit.ors.services.routing.requestprocessors;

import com.graphhopper.util.Helper;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.routing.RoutingErrorCodes;
import heigit.ors.routing.RoutingProfileManagerStatus;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;

import javax.servlet.http.HttpServletRequest;

@Deprecated
public class RoutingServiceRequestProcessorFactory {
	public static AbstractHttpRequestProcessor createProcessor(HttpServletRequest request) throws Exception
	{
		if (!RoutingServiceSettings.getEnabled())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, RoutingErrorCodes.UNKNOWN, "Routing service is not enabled.");

		if (!RoutingProfileManagerStatus.isReady())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, RoutingErrorCodes.UNKNOWN, "Routing service is not ready yet.");

		String requestParam = request.getParameter("request");
		// Example request: http://localhost:8082/openrouteservice-4.4.0/routes?profile=driving-car&coordinates=8.690614,49.38365|8.7007,49.411699|8.7107,49.4516&prettify_instructions=true&format=gpx
		if (Helper.isEmpty(requestParam))
			requestParam = "route";

		switch (requestParam.toLowerCase())
		{
			case "tmc":
				return new TmcInformationRequestProcessor(request);
			case "route":
				String formatParam = request.getParameter("format");
				if (Helper.isEmpty(formatParam))
					formatParam = "json";
				else
					formatParam = formatParam.toLowerCase();

				switch(formatParam)
				{
					case "json":
					case "geojson":
					case"gpx":
						return new RoutingRequestProcessor(request);
					default:
						throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "format", formatParam);
				}

			default:
				throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "request", requestParam);
		}
	}
}
