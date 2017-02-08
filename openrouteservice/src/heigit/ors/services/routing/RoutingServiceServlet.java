/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.routeservice;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.freeopenls.filedelete.FileDelete;
import org.freeopenls.routeservice.documents.ResponseXLSDocument;
import org.freeopenls.routeservice.routing.RouteInfoUtils;
import org.freeopenls.routeservice.routing.RouteProfileManager;
import org.freeopenls.routeservice.traffic.RealTrafficDataProvider;
import org.freeopenls.tools.HTTPUtility;
import org.freeopenls.tools.StreamUtility;
import org.freeopenls.tools.StringUtility;
import org.freeopenls.tools.TimeUtility;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;

/**
 * <p>
 * <b>Title: RSServlet</b>
 * </p>
 * <p>
 * <b>Description:</b> Class for Servlet OpenLS Route Service (OpenLS RS) <br>
 * The servlet of the RS which receives the incoming HttpPost requests <br>
 * and sends the operation result documents to the client.
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2008
 * </p>
 * <p>
 * <b>Institution:</b> University of Bonn, Department of Geography
 * </p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-07-20
 * @version 1.1 2008-04-20
 */
public class RSServlet extends HttpServlet {
	/** Serial Version UID */
	private static final long serialVersionUID = 1L;
	/** Logger, used to log errors(exceptions) and additionally information */
	private static Logger mLogger = Logger.getLogger(RSServlet.class.getName());
	private static Logger mLoggerCounter = Logger.getLogger(RSServlet.class.getName() + ".Counter");
	/**
	 * RequestOperator - handles the requests and send them up to the specific
	 * requestListeners
	 */
	private RequestOperator mReqOperator;
	/** RSConfigurator **/
	private RSConfigurator mRSConfigurator;

	/**
	 * Method that initialize the RouteService Servlet ->
	 * RouteServiceConfigurator.
	 * 
	 * @throws ServletException
	 */
	public void init() throws ServletException {
		// Initialize Configurator
		mRSConfigurator = RSConfigurator.getInstance();
		// Initialize FileDelete
		FileDelete.initFileDelete();
	}

	/**
	 * Method that removes the RSServlet from the server.
	 * 
	 */
	public void destroy() {
		FileDelete.stop();
		try {
			RouteProfileManager.getInstance().destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Remove all appenders
		Logger.getRootLogger().removeAllAppenders();
	}

	/**
	 * Method to service requests. (POST) - <br>
	 * the request will be passed to the RequestOperator
	 * 
	 * @param request
	 *            HttpServletRequest - incoming Request
	 * @param response
	 *            HttpServletResponse - response of the incoming request
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		mLoggerCounter.info(mRSConfigurator.getServiceName() + "|POST|" + HTTPUtility.getRemoteAddr(request));
		long requestTime = System.currentTimeMillis();

		try {
			if (request.getParameterMap().containsKey("info")) {
				RouteInfoUtils.writeRouteInfo(request, response);
			} else {
				// Get the request
				InputStream in = request.getInputStream();
				String decodedString = StringUtility.decodeRequestString(StreamUtility.readStream(in));
				in.close();

				mReqOperator = new RequestOperator();
				ResponseXLSDocument routeserviceResp = mReqOperator.doOperation(decodedString);
				doResponse(response, routeserviceResp);
			}
		} catch (Exception e) {
			mLogger.error("Error: ", e);
		}

		mLoggerCounter.info(mRSConfigurator.getServiceName() + "|POST|took "
				+ TimeUtility.getElapsedTime(requestTime, true));
	}

	/**
	 * Method to service requests. (GET) - <br>
	 * the request will be passed to the RequestOperator
	 * 
	 * @param request
	 *            HttpServletRequest - incoming Request
	 * @param response
	 *            HttpServletResponse - response of the incoming request
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		mLoggerCounter.info("GET|" + HTTPUtility.getRemoteAddr(request) + "|" + mRSConfigurator.getServiceName());
		long requestTime = System.currentTimeMillis();

		try {
		
			if (request.getParameterMap().containsKey("info")) {
				RouteInfoUtils.writeRouteInfo(request, response);
			} else if (request.getParameterMap().containsKey("tmc") && RealTrafficDataProvider.getInstance().isInitialized()) {
			  String bbox =	request.getParameter("bbox");
			  Envelope env = null;
			  if (!Helper.isEmpty(bbox))
			  {
				  String[] bboxValues = bbox.split(",");
				  env = new Envelope(Double.parseDouble(bboxValues[0]), Double.parseDouble(bboxValues[2]), Double.parseDouble(bboxValues[1]), Double.parseDouble(bboxValues[3]));
			  }
			  
			  String json = RealTrafficDataProvider.getInstance().getTmcInfoAsJson(env);
			  
			  response.setCharacterEncoding("UTF-8");
			  response.setContentType("application/json");
			  response.setStatus(SC_OK);
			  response.getWriter().append(json);
			}
			else {
				String strRequest = request.getQueryString();

				mReqOperator = new RequestOperator();
				ResponseXLSDocument routeserviceResp = mReqOperator.doOperation(strRequest);
				doResponse(response, routeserviceResp);
			}
		} catch (Exception e) {
			mLogger.error("Error: ", e);
		}

		long responseTime = System.currentTimeMillis();
		long handlingTime = responseTime - requestTime;
		double handlingTimeSeconds = (double) handlingTime / 1000;

		mLoggerCounter.info(mRSConfigurator.getServiceName() + "|GET|took "
				+ Double.toString(handlingTimeSeconds).replace(".", ",") + "s");
	}
	
	/**
	 * Method to service response. - <br>
	 * writes the content of the RS Response to the OutputStream of the
	 * HttpServletResponse
	 * 
	 * @param resp
	 *            the HttpServletResponse to which the content will be written
	 * @param RouteServiceResponse
	 *            the RespRouteXLSDoc, whose content will be written to the
	 *            OutputStream of resp param
	 */
	public void doResponse(HttpServletResponse resp, ResponseXLSDocument routeServiceResponse) {
		try {
			String contentType = routeServiceResponse.getContentType();
			int contentLength = routeServiceResponse.getContentLength();
			byte[] bytes = routeServiceResponse.getByteArray();
			resp.setContentLength(contentLength);
			OutputStream out = resp.getOutputStream();
			resp.setHeader("Content-Type", contentType);
			// resp.setCharacterEncoding("ISO-8859-1");
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType(contentType);
			// resp.setHeader("Cache-Control", "no-cache");
			// log.info("doResponse");
			out.write(bytes);
			out.close();
		} catch (IOException ioe) {
			mLogger.error(mRSConfigurator.getServiceName() + "|POST|Error:", ioe);
		}
	}
}
