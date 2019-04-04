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
