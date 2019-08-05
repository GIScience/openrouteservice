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
package heigit.ors.servlet.http;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.AppInfo;

public class BaseHttpServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
    protected static Logger LOGGER = Logger.getLogger(BaseHttpServlet.class.getName());

    protected void writeError(HttpServletResponse res, Exception ex)
    {
      writeError(res, ex, StatusCode.BAD_REQUEST);
    }
    
    protected void writeError(HttpServletResponse res, Exception ex, int statusCode)
    {
    	try
		{
			JSONObject json = new JSONObject();
			
			JSONObject jError = new JSONObject();
			jError.put("message", ex.getMessage());
			json.put("error", jError);
			
			JSONObject jInfo = new JSONObject();
			jInfo.put("engine", AppInfo.getEngineInfo());
			jInfo.put("timestamp", System.currentTimeMillis());
			json.put("info", jInfo);

			int errorCode = -1;
			
			if (ex instanceof InternalServerException)
			{
				InternalServerException ise = (InternalServerException)ex;
				statusCode = StatusCode.INTERNAL_SERVER_ERROR;
				errorCode = ise.getInternalCode();
			}
			else if (ex instanceof StatusCodeException)
			{
				StatusCodeException sce = (StatusCodeException)ex;
				statusCode = sce.getStatusCode();
				errorCode = sce.getInternalCode();
			}
			
			if (errorCode > 0)
			{
				jError.put("code", errorCode);
				writeError(res, statusCode, json);
			}					
			else
				writeError(res, statusCode, json);

			if(LOGGER.isDebugEnabled()) {
				// Log also the stack trace
				LOGGER.error("Exception", ex);
			} else {
				// Log only the error message
				LOGGER.error(ex);
			}
		} catch (JSONException e) {
			LOGGER.error(e);
		}
    }
    
	protected void writeError(HttpServletResponse resp, int httpStatusCode, JSONObject json ) 
	{
		try
		{
			ServletUtility.write(resp, json, "UTF-8", httpStatusCode);
		} catch (Exception ex)
		{
			LOGGER.error(ex);
		}
	}
}
