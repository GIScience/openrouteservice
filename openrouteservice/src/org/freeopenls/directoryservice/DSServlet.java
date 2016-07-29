/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.directoryservice;

import java.io.*;
import java.net.HttpRetryException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.freeopenls.directoryservice.documents.ResponseXLSDocument;
import org.freeopenls.tools.HTTPUtility;
import org.freeopenls.tools.StreamUtility;
import org.freeopenls.tools.StringUtility;
import org.freeopenls.tools.TimeUtility;


/**
 * <p><b>Title: DSServlet</b></p>
 * <p><b>Description:</b> Class for Servlet OpenLS Directory Service (OpenLS DS) <br>
 * The servlet of the DS which receives the incoming HttpPost requests <br>
 * and sends the operation result documents to the client. </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-04-20
 */
public class DSServlet extends HttpServlet {
	/** Serial Version UID */
	private static final long serialVersionUID = 1L;
	/** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(DSServlet.class.getName());
    private static Logger mLoggerCounter = Logger.getLogger(DSServlet.class.getName()+".Counter");
    /** RequestOperator - handles the requests and send them up to the specific requestListeners */
    private RequestOperator mReqOperator;
    /** RSConfigurator **/
    private DSConfigurator mDSConfigurator;
    
    /**
     * Method that initialize the DirectoryService Servlet -> DirectoryServiceConfigurator.
     * 
     * @throws ServletException
     */
    public void init() throws ServletException {
		//Initialize Configurator
		mDSConfigurator = DSConfigurator.getInstance();
	}

    /**
     * Method that removes the RSServlet from the server.
     * 
     */
    public void destroy() {
    	//Close all DB connections
    	mDSConfigurator.getConnectionManager().closeAllConnections();
    	//Remove all appenders
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
		mLoggerCounter.info(mDSConfigurator.getServiceName() + "|POST|" + HTTPUtility.getRemoteAddr(request));
		
		long requestTime = System.currentTimeMillis();
		try{
            //Get the request
            InputStream in = request.getInputStream();
            String decodedString = StringUtility.decodeRequestString(StreamUtility.readStream(in));

            mReqOperator = new RequestOperator();
            ResponseXLSDocument directoryResponse = mReqOperator.doOperation(decodedString);
            doResponse(response, directoryResponse);
            
            in.close();
            
		}catch(Exception e){
			mLogger.error("doPost() Error: ", e);
		}
		
		mLoggerCounter.info(mDSConfigurator.getServiceName() + "|POST|took "
				+ TimeUtility.getElapsedTime(requestTime, true));
	}

    /**
     * Method to service response. - <br>
     * writes the content of the Response to the OutputStream of the HttpServletResponse
     * 
     * @param resp
     * 			the HttpServletResponse to which the content will be written
     * @param RouteServiceResponse
     * 			the RespRouteXLSDoc, whose content will be written to the OutputStream of resp param
     */
    public void doResponse(HttpServletResponse resp, ResponseXLSDocument RouteServiceResponse) {
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
        	mLogger.error("doResponse() Error: ", ioe);
        }
    }
}
