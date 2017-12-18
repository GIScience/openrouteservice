/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
			
			LOGGER.error("Exception", ex);
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
