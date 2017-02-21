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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
    protected static Logger LOGGER = LoggerFactory.getLogger(BaseHttpServlet.class);

	protected void writeError(HttpServletResponse res, Exception ex) 
	{
		try {
			JSONObject json = new JSONObject();
			json.put("message", ex.getMessage());
			writeError(res, SC_BAD_REQUEST, json);
			
			LOGGER.error(ex.getMessage());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void writeError(HttpServletResponse res, int code, JSONObject json ) 
	{
		try
		{
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			res.setStatus(code);
			res.getWriter().append(json.toString(2));
		} catch (Exception ex)
		{
			LOGGER.error("Unable to write error " + ex.getMessage());
		}
	}
}
