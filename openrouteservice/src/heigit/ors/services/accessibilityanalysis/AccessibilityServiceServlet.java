/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.accessibilityanalyseservice;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.freeopenls.accessibilityanalyseservice.documents.ResponseAASDocument;
import org.freeopenls.filedelete.FileDelete;
import org.freeopenls.tools.HTTPUtility;
import org.freeopenls.tools.StreamUtility;
import org.freeopenls.tools.TimeUtility;



/**
 * <p><b>Title: AASServlet</b></p>
 * <p><b>Description:</b> Class for Servlet AAS <br>
 * The servlet of the AAS which receives the incoming HttpPost requests <br>
 * and sends the operation result documents to the client. </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version @version 3.0 2006-12-22
 */
public class AASServlet extends HttpServlet {
	/** Serial Version UID */
	static final long serialVersionUID = 1;
	/** Logger, used to log errors(exceptions) and additionally information */
	private static Logger mLogger = Logger.getLogger(AASServlet.class.getName());
    private static Logger mLoggerCounter = Logger.getLogger(AASServlet.class.getName()+".Counter");
	/** RequestOperator - handles the requests and send them up to the specific requestListeners */
    private RequestOperator reqop;
    /** AAS Configurator */
    private AASConfigurator mAASConfigurator;

    /**
     * Method that initialize the Accessibility Analyse Servlet -> AccessibilityConfigurator.
     * 
     * @throws ServletException
     */
	public void init() {
		// Initialize configurator
		mAASConfigurator = AASConfigurator.getInstance();
		//Initialize FileDelete
		FileDelete.initFileDelete();
	}

    /**
     * Method to service requests. (POST) - <br>
     * the request will be passed to the AccessibilityListener
     * 
     * @param request
     * 				HttpServletRequest - incoming Request
     * @param response 
     * 				HttpServletResponse - response of the incoming request
     * @throws ServletException
     * @throws IOException
     */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		mLoggerCounter.info(mAASConfigurator.getServiceName() + "|POST|" + HTTPUtility.getRemoteAddr(request));
		long requestTime = System.currentTimeMillis();
			
		try{
			response.setContentType("text/html");
			
            // Read the request
            InputStream in = request.getInputStream();
            String decodedString = "";
            String inputString = StreamUtility.readStream(in);
            
            //log.info("Input Request: "+inputString);
            //discard "REQUEST="-Input String header
            if (inputString.startsWith("REQUEST=")) {
                inputString = inputString.substring(8, inputString.length());
            }
            else if(inputString.startsWith("---")){
            	int iIndexStart = inputString.indexOf( "<?xml" );
            	inputString = inputString.substring(iIndexStart, inputString.length());
            	int iIndexEnd = inputString.indexOf( "---");
            	inputString = inputString.substring(0, iIndexEnd);
            }

            //decode the application/x-www-form-url encoded query string
            decodedString = java.net.URLDecoder.decode(inputString, "ISO-8859-1");//"UTF-8");
            //mLogger.info("Decoded Request: "+decodedString);
            //System.out.println("Decoded Request: "+decodedString);

            this.reqop = new RequestOperator();
            
            ResponseAASDocument routeserviceResp = this.reqop.doOperation(decodedString);
            doResponse(response, routeserviceResp);
            
            in.close();
            
		}catch(Exception e){
			mLogger.error(AASServlet.class.getName()+" doPost "+e);
		}
		
		mLoggerCounter.info(mAASConfigurator.getServiceName() + "|POST|took "
				+ TimeUtility.getElapsedTime(requestTime, true));
	}
	
    /**
     * Method that removes the AAS Servlet from the server.
     * 
     */
    public void destroy() {
    	Logger.getRootLogger().removeAllAppenders();
    	FileDelete.stop();
    }
    
    /**
     * Method to service response. - <br>
     * writes the content of the AASResponse to the OutputStream of the
     * HttpServletResponse
     * 
     * @param resp
     * 			the HttpServletResponse to which the content will be written
     * @param AccessibilityServiceResponse
     * 			the RespAASDoc, whose content will be written to the OutputStream of resp param
     */
    private void doResponse(HttpServletResponse resp, ResponseAASDocument AASResponse) {
        try {
            String contentType = AASResponse.getContentType();
            int contentLength = AASResponse.getContentLength();
            byte[] bytes = AASResponse.getByteArray();
            resp.setContentLength(contentLength);
            OutputStream out = resp.getOutputStream();
            resp.setContentType(contentType);
            //log.info("doResponse");
            out.write(bytes);
            out.close();
        } catch (IOException ioe) {
            mLogger.error(AASServlet.class.getName()+" doResponse "+ioe);
        }
    }
}
