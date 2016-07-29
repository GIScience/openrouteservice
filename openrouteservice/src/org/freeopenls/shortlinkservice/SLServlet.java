/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.shortlinkservice;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.freeopenls.filedelete.FileDelete;
import org.freeopenls.routeservice.documents.ResponseXLSDocument;
import org.freeopenls.routeservice.routing.RouteInfoUtils;
import org.freeopenls.routeservice.routing.RouteProfileManager;
import org.freeopenls.tools.HTTPUtility;
import org.freeopenls.tools.StreamUtility;
import org.freeopenls.tools.StringUtility;
import org.freeopenls.tools.TimeUtility;
import org.json.JSONException;
import org.json.JSONObject;

import com.graphhopper.util.Helper;

public class SLServlet extends HttpServlet {
	/** Serial Version UID */
	private static final long serialVersionUID = 1L;
	/** Logger, used to log errors(exceptions) and additionally information */
	private static Logger mLogger = Logger.getLogger(SLServlet.class.getName());
	private static Logger mLoggerCounter = Logger.getLogger(SLServlet.class.getName() + ".Counter");
	private SLConfigurator mSLConfigurator;

	public void init() throws ServletException {
		// Initialize Configurator
		mSLConfigurator = SLConfigurator.getInstance();
	}

	/**
	 * Method that removes the RSServlet from the server.
	 * 
	 */
	public void destroy() {
		// Remove all appenders
		Logger.getRootLogger().removeAllAppenders();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			InputStream in = request.getInputStream();
			String decodedString = StringUtility.decodeRequestString(StreamUtility.readStream(in));
			in.close();

			String shortenLink = getShortenLink(decodedString);
			response.setStatus(SC_OK);
			response.getWriter().append(shortenLink);
		} catch (Exception e) {
			mLogger.error("Error: ", e);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String decodedString = StringUtility.decodeRequestString(request.getQueryString());
			String shortenLink = getShortenLink(decodedString);
			response.setStatus(SC_OK);
			response.getWriter().append(shortenLink);
		} catch (Exception e) {
			mLogger.error("Error: ", e);
		}
	}

	private String getShortenLink(String longUrl) throws IOException, JSONException {
		String serviceUrl = "http://api.bitly.com/v3/shorten?callback=?";
		String reqParams = "format=json&apiKey=" + mSLConfigurator.getApiKey() + "&login="
				+ mSLConfigurator.getUserName() + "&longUrl=" + URLEncoder.encode(longUrl, "UTF-8");

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
