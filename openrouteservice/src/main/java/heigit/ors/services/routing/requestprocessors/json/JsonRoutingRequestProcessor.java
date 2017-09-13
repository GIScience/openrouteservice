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
package heigit.ors.services.routing.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.graphhopper.util.Helper;

import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;

public class JsonRoutingRequestProcessor extends AbstractHttpRequestProcessor {

	public JsonRoutingRequestProcessor(HttpServletRequest request) throws Exception 
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception {
		RoutingRequest rreq = JsonRoutingRequestParser.parseFromRequestParams(_request);
		
		RouteResult result = RoutingProfileManager.getInstance().computeRoute(rreq);
		
		JSONObject json = null;
		
		String respFormat = _request.getParameter("format");
		if (Helper.isEmpty(respFormat) || "json".equalsIgnoreCase(respFormat))
			json = JsonRoutingResponseWriter.toJson(rreq, new RouteResult[] { result });
		else if ("geojson".equalsIgnoreCase(respFormat))
			json = JsonRoutingResponseWriter.toGeoJson(rreq, new RouteResult[] { result });
		
		ServletUtility.write(response, json, "UTF-8");
	}
}
