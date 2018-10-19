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

		if (formatParam.equalsIgnoreCase("json"))
			return new JsonGeocodingRequestProcessor(request);
	/*	else if (formatParam.equalsIgnoreCase("xml"))
			return new XmlGeocodingRequestProcessor(request);*/
		else 
			throw new UnknownParameterValueException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "format", formatParam);
	}
}
