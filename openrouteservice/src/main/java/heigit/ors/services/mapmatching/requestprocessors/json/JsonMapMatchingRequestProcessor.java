/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
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
		
		if (MapMatchingServiceSettings.getMaximumLocations() > 0 && req.getCoordinates().length > MatrixServiceSettings.getMaximumLocations())
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
