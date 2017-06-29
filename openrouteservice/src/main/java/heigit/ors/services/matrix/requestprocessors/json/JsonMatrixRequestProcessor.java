/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.services.matrix.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.AppInfo;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FormatUtility;

public class JsonMatrixRequestProcessor extends AbstractHttpRequestProcessor 
{
	public JsonMatrixRequestProcessor(HttpServletRequest request) throws Exception
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws Exception 
	{
		String reqMethod = _request.getMethod();

		MatrixRequest req = null;
		switch (reqMethod)
		{
		case "GET":
			req = JsonMatrixRequestParser.parseFromRequestParams(_request);
			break;
		///case "POST":  needs to be implemented
		//	req = JsonMatrixRequestParser.parseFromStream(_request.getInputStream());  
		//	break;
		default:
			throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED);
		}

		if (req == null)
			throw new StatusCodeException(StatusCode.BAD_REQUEST, MatrixErrorCodes.UNKNOWN, "MatrixRequest object is null.");
		
		if (MatrixServiceSettings.getMaximumLocations() > 0 && req.getTotalNumberOfLocations() > MatrixServiceSettings.getMaximumLocations())
			throw new ParameterOutOfRangeException(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "sources/destinations", Integer.toString(req.getTotalNumberOfLocations()), Integer.toString(MatrixServiceSettings.getMaximumLocations()));
		
		MatrixResult mtxResult = RoutingProfileManager.getInstance().computeMatrix(req);
		
		writeResponse(response, req, mtxResult);
	}
	
	private void writeResponse(HttpServletResponse response, MatrixRequest request, MatrixResult mtxResult) throws Exception
	{
		JSONObject jResp = new JSONObject(true);
		
		if (MatrixMetricsType.isSet(request.getMetrics(), MatrixMetricsType.Distance))
			jResp.put("distances", createTable(mtxResult.getTable(MatrixMetricsType.Distance), request.getSources().length, request.getDestinations().length));
		if (MatrixMetricsType.isSet(request.getMetrics(), MatrixMetricsType.Duration))
			jResp.put("durations", createTable(mtxResult.getTable(MatrixMetricsType.Duration), request.getSources().length, request.getDestinations().length));
		if (MatrixMetricsType.isSet(request.getMetrics(), MatrixMetricsType.Weight))
			jResp.put("weights", createTable(mtxResult.getTable(MatrixMetricsType.Weight), request.getSources().length, request.getDestinations().length));		

		jResp.put("destinations", createLocations(mtxResult.getDestinations(), request.getResolveLocations() ? mtxResult.getDestinationNames() : null));
		jResp.put("sources", createLocations(mtxResult.getSources(), request.getResolveLocations() ? mtxResult.getSourceNames() : null));
		
		JSONObject jInfo = new JSONObject(true);
		jInfo.put("service", "matrix");
		jInfo.put("version", AppInfo.VERSION);
		if (!Helper.isEmpty(MatrixServiceSettings.getAttribution()))
			jInfo.put("attribution", MatrixServiceSettings.getAttribution());
		jInfo.put("timestamp", System.currentTimeMillis());
		
		JSONObject jQuery = new JSONObject();

		jQuery.put("profile", RoutingProfileType.getName(request.getProfileType()));

		if (request.getUnits() != null)
			jQuery.put("units", DistanceUnitUtil.toString(request.getUnits()));

		if (request.getId() != null)
			jQuery.put("id", request.getId());

		jInfo.put("query", jQuery);
		jResp.put("info", jInfo);
		
		ServletUtility.write(response, jResp);
	}
	
	private JSONArray createLocations(Coordinate[] locations, String[] names)
	{
		JSONArray jLocations = new JSONArray(locations.length);
		
		for (int i = 0; i < locations.length; i++)
		{
			Coordinate c = locations[i];
			JSONObject jLoc = new JSONObject(true);
			JSONArray jCoord = new JSONArray(2);
			jCoord.put(FormatUtility.roundToDecimals(c.x, 6));
			jCoord.put(FormatUtility.roundToDecimals(c.y, 6));
			jLoc.put("location", jCoord);
			
			if (names != null)
				jLoc.put("name", names[i]);
			
			jLocations.put(jLoc);
		}
		
		return jLocations;
	}
	
	private JSONArray createTable(float[] values, int rows, int clms)
	{
		JSONArray jMatrix = new JSONArray(rows);
		
		for (int i = 0; i < rows; i++)
		{
			int rowOffset = i*clms;
			JSONArray jRow = new JSONArray(clms);
			for (int j = 0; j < clms; j++)
				jRow.put(FormatUtility.roundToDecimals(values[rowOffset + j], 1));
			jMatrix.put(jRow);
		}
		
		return jMatrix;
	}
}
