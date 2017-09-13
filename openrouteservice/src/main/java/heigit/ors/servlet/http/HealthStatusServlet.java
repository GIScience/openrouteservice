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
package heigit.ors.servlet.http;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONObject;

import heigit.ors.common.StatusCode;
import heigit.ors.routing.RoutingProfileManagerStatus;
import heigit.ors.servlet.util.ServletUtility;

public class HealthStatusServlet extends BaseHttpServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {

	}

	public void destroy() {
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try
		{
			JSONObject jStatus = new JSONObject();

			if (!RoutingProfileManagerStatus.isReady())
			{
				jStatus.put("status", "not ready");
				ServletUtility.write(response, jStatus, StatusCode.SERVICE_UNAVAILABLE);
			}
			else
			{
				jStatus.put("status", "ready");
				ServletUtility.write(response, jStatus, StatusCode.OK);
			}
		}
		catch (Exception ex) {
			writeError(response, ex);
		}
	}
}
