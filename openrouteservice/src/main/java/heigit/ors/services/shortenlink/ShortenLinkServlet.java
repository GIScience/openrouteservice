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
package heigit.ors.services.shortenlink;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.*;
import java.net.URLEncoder;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.servlet.http.BaseHttpServlet;
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.util.HTTPUtility;
import heigit.ors.util.StringUtility;

import com.graphhopper.util.Helper;

public class ShortenLinkServlet extends BaseHttpServlet {

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {

	}

	public void destroy() {
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try
		{
			String url = ServletUtility.readRequestContent(request);

			String shortenLink = getShortenLink(url);
			response.setStatus(SC_OK);
			response.getWriter().append(shortenLink);
		}
		catch(Exception ex)
		{
			writeError(response, ex);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try
		{
			if (!ShortenLinkServiceSettings.getEnabled())
				throw new StatusCodeException(StatusCode.SERVICE_UNAVAILABLE, 0, "Shortenlink service is not enabled.");
			
			String decodedString = StringUtility.decodeRequestString(request.getQueryString());
			String shortenLink = getShortenLink(decodedString);
			response.setStatus(SC_OK);
			response.getWriter().append(shortenLink);
		}
		catch(Exception ex)
		{
			writeError(response, ex);
		}
	}

	private String getShortenLink(String longUrl) throws IOException, JSONException {
		String serviceUrl = "http://api.bitly.com/v3/shorten?callback=?";
		String reqParams = "format=json&apiKey=" + ShortenLinkServiceSettings.getApiKey() + "&login="
				+ ShortenLinkServiceSettings.getUserName() + "&longUrl=" + URLEncoder.encode(longUrl, "UTF-8");

		String resp = HTTPUtility.getResponse(serviceUrl + reqParams, 2000, "OpenRouteService", "UTF-8");
		if (!Helper.isEmpty(resp)) {
			String str = "?format=json(";
			int index1 = resp.indexOf("?format=json(") + str.length();
			int index2 = resp.indexOf(")");
			resp = resp.substring(index1, index2);
			JSONObject json = new JSONObject(resp);
			JSONObject jsonData = (JSONObject) json.get("data");
			String result = jsonData.getString("url");

			return result;
		} else {
			throw new IOException("The response from api.bitly.com is empty.");
		}
	}
}
