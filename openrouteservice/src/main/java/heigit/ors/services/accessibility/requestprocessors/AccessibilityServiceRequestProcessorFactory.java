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
 *//*

package heigit.ors.services.accessibility.requestprocessors;

import javax.servlet.http.HttpServletRequest;

import heigit.ors.accessibility.AccessibilityErrorCodes;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.routing.RoutingProfileManagerStatus;
import heigit.ors.services.accessibility.AccessibilityServiceSettings;
import heigit.ors.services.accessibility.requestprocessors.json.JsonAccessibilityRequestProcessor;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;

import com.graphhopper.util.Helper;

public class AccessibilityServiceRequestProcessorFactory {

	public static AbstractHttpRequestProcessor createProcessor(HttpServletRequest request) throws Exception  
	{
		if (!AccessibilityServiceSettings.getEnabled())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, AccessibilityErrorCodes.UNKNOWN,  "Accessibility service is not enabled.");

		if (!RoutingProfileManagerStatus.isReady())
			throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, AccessibilityErrorCodes.UNKNOWN, "Accessibility service is not ready yet.");

		String formatParam = request.getParameter("format");

		if (Helper.isEmpty(formatParam))
			formatParam = "json";

		if (formatParam.equalsIgnoreCase("json"))
			return new JsonAccessibilityRequestProcessor(request);
		else 
			throw new UnknownParameterValueException(AccessibilityErrorCodes.INVALID_PARAMETER_VALUE, "format", formatParam);
	}
}
*/
