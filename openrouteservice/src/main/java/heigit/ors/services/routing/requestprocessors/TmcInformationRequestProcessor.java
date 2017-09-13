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
package heigit.ors.services.routing.requestprocessors;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;

public class TmcInformationRequestProcessor extends AbstractHttpRequestProcessor 
{
	public TmcInformationRequestProcessor(HttpServletRequest request) throws Exception 
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws IOException  
	{
		if (RealTrafficDataProvider.getInstance().isInitialized())
		{
			String bbox =	_request.getParameter("bbox");
			Envelope env = null;
			if (!Helper.isEmpty(bbox))
			{
				String[] bboxValues = bbox.split(",");
				env = new Envelope(Double.parseDouble(bboxValues[0]), Double.parseDouble(bboxValues[2]), Double.parseDouble(bboxValues[1]), Double.parseDouble(bboxValues[3]));
			}

			String json = RealTrafficDataProvider.getInstance().getTmcInfoAsJson(env);

			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().append(json);
		}
		else
		{
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/text");
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.getWriter().append("Tmc service is unavailable.");

		}
	}
}
