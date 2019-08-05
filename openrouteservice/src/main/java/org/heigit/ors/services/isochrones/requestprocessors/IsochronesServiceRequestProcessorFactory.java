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
package heigit.ors.services.isochrones.requestprocessors;

import javax.servlet.http.HttpServletRequest;

import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.routing.RoutingProfileManagerStatus;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.services.isochrones.requestprocessors.json.JsonIsochronesRequestProcessor;

import com.graphhopper.util.Helper;

public class IsochronesServiceRequestProcessorFactory {

	public static AbstractHttpRequestProcessor createProcessor(HttpServletRequest request) throws Exception  
	{
		if (!IsochronesServiceSettings.getEnabled())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, IsochronesErrorCodes.UNKNOWN, "Isochrones service is not enabled.");
		
		if (!RoutingProfileManagerStatus.isReady())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, IsochronesErrorCodes.UNKNOWN, "Isochrones service is not ready yet.");

		String formatParam = request.getParameter("format");

		if (Helper.isEmpty(formatParam))
			formatParam = "json";

		if (formatParam.equalsIgnoreCase("json"))
			return new JsonIsochronesRequestProcessor(request);
		/*else if (formatParam.equalsIgnoreCase("xml"))
			return new XmlAccessibilityRequestProcessor(request);*/
		else 
			throw new UnknownParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "format", formatParam);
	}
}
