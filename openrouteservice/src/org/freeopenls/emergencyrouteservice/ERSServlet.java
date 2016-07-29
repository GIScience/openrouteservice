/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.emergencyrouteservice;


import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.freeopenls.emergencyrouteservice.RespRouteXLSDoc;
import org.freeopenls.tools.HTTPUtility;
import org.freeopenls.tools.StreamUtility;
import org.freeopenls.tools.TimeUtility;


/**
 * <p><b>Title: Class for Servlet Emergency Route Service </b></p>
 * <p><b>Description:</b> The servlet of the ERS which receives the incoming HttpPost requests
 * and sends the operation result documents to the client. </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-11-08
 */
public class ERSServlet extends HttpServlet {
	/** Serial Version UID */
	private static final long serialVersionUID = 1L;
	/** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(ERSServlet.class.getName());
    private static Logger mLoggerCounter = Logger.getLogger(ERSServlet.class.getName()+".Counter");
    /** RequestOperator - handles the requests and send them up to the specific requestListeners */
    private RequestOperator mRequestOperator;
    /** ERSConfigurator **/
    private ERSConfigurator mERSConfigurator;
    
    /**
     * Method that initialize the ERS Servlet -> ERSConfigurator.
     * 
     * @throws ServletException
     */
    public void init() throws ServletException {

		//Initialize Configurator
		mERSConfigurator = ERSConfigurator.getInstance();
	}

    /**
     * Method that removes the ERS Servlet from the server.
     * 
     */
    public void destroy() {
    	Logger.getRootLogger().removeAllAppenders();
    }

    /**
     * Method to service requests. (POST) - <br>
     * the request will be passed to the RequestOperator
     * 
     * @param request
     * 				HttpServletRequest - incoming Request
     * @param response 
     * 				HttpServletResponse - response of the incoming request
     * @throws ServletException
     * @throws IOException
     */
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		
		mLoggerCounter.info("Post | "+HTTPUtility.getRemoteAddr(request)+" | "+mERSConfigurator.getServiceName());
		long requestTime = System.currentTimeMillis();
		try{
            //Get the request
            InputStream in = request.getInputStream();
            String decodedString = "";
            String inputString = StreamUtility.readStream(in);
            //log.info("Input Request: "+inputString);
            //Discard "REQUEST="-Input String header
            if (inputString.startsWith("REQUEST=")) {
                inputString = inputString.substring(8, inputString.length());
            }
            else if(inputString.startsWith("---")){
            	int iIndexStart = inputString.indexOf( "<?xml" );
            	inputString = inputString.substring(iIndexStart, inputString.length());
            	int iIndexEnd = inputString.indexOf( "---");
            	inputString = inputString.substring(0, iIndexEnd);
            }

            //Decode the application/x-www-form-url encoded query string
            decodedString = java.net.URLDecoder.decode(inputString, "ISO-8859-1");//"UTF-8");
            //log.info("Decoded Request: "+decodedString);

            mRequestOperator = new RequestOperator();
            RespRouteXLSDoc routeserviceResp = mRequestOperator.doOperation(decodedString);
            doResponse(response, routeserviceResp);
            
            in.close();
            
		}catch(Exception e){
			mLogger.error(e);
		}

		mLoggerCounter.info("Finish ; "+HTTPUtility.getRemoteAddr(request)+" ; "+ TimeUtility.getElapsedTime(requestTime, true)+" ; "+mERSConfigurator.getServiceName());
	}

    /**
     * Method to service response. - <br>
     * writes the content of the ERS Response to the OutputStream of the
     * HttpServletResponse
     * 
     * @param resp
     * 			the HttpServletResponse to which the content will be written
     * @param RouteServiceResponse
     * 			the RespRouteXLSDoc, whose content will be written to the OutputStream of resp param
     */
    public void doResponse(HttpServletResponse resp, RespRouteXLSDoc RouteServiceResponse) {
        try {
            String contentType = RouteServiceResponse.getContentType();
            int contentLength = RouteServiceResponse.getContentLength();
            byte[] bytes = RouteServiceResponse.getByteArray();
            resp.setContentLength(contentLength);
            OutputStream out = resp.getOutputStream();
            resp.setContentType(contentType);
            //log.info("doResponse");
            out.write(bytes);
            out.close();
        } catch (IOException ioe) {
        	mLogger.error("OpenlS ERS - doPost - Error: ", ioe);
        }
    }
}
