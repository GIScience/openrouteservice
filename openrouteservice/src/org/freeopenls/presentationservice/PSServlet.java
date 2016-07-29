/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.presentationservice;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.freeopenls.filedelete.FileDelete;
import org.freeopenls.presentationservice.documents.ResponseXLSDocument;
import org.freeopenls.tools.HTTPUtility;
import org.freeopenls.tools.StreamUtility;
import org.freeopenls.tools.StringUtility;
import org.freeopenls.tools.TimeUtility;


/**
 * <p><b>Title: Class Servlet OpenLS Presentation Service </b></p>
 * <p><b>Description:</b>  </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-05-24
 */
public class PSServlet extends HttpServlet {
	/** Serial Version UID */
	static final long serialVersionUID = 1;
	/** Logger, used to log errors(exceptions) and additionally information */
    private static Logger mLogger = Logger.getLogger(PSServlet.class.getName());
    private static Logger mLoggerCounter = Logger.getLogger(PSServlet.class.getName()+".Counter");
	/** RequestOperator - handles the requests and send them up to the specific requestListeners */
    private RequestOperator mRequestOperator;
    /** PresentationConfigurator **/
    private PSConfigurator mPSConfigurator;
        
    /**
     * Method that initialize the OpenLS Presentation Service -> PresentationConfigurator.
     * 
     * @throws ServletException
     */
	public void init() {
		// Initialize configurator
		mPSConfigurator = PSConfigurator.getInstance();
		//Initialize FileDelete
		FileDelete.initFileDelete();
	}

    /**
     * Method to service requests. (POST) - <br>
     * the request will be passed to the PresentationListener
     * 
     * @param request
     * 				HttpServletRequest - incoming Request
     * @param response 
     * 				HttpServletResponse - response of the incoming request
     * @throws ServletException
     * @throws IOException
     */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		mLoggerCounter.info("Post ; "+HTTPUtility.getRemoteAddr(request)+" ; ; "+mPSConfigurator.getServiceName());
		long requestTime = System.currentTimeMillis();
		
		response.setContentType("text/html");		
		try{
            // Read the request
            InputStream in = request.getInputStream();
            String decodedString = StringUtility.decodeRequestString(StreamUtility.readStream(in));

            this.mRequestOperator = new RequestOperator();

            ResponseXLSDocument respXLS = this.mRequestOperator.doOperation(decodedString);
            doResponse(response, respXLS);

            in.close();

		}catch(Exception e){
			mLogger.error("doPost()", e);
		}

		mLoggerCounter.info("Finish ; "+HTTPUtility.getRemoteAddr(request)+" ; "+ TimeUtility.getElapsedTime(requestTime, true) +" ; "+mPSConfigurator.getServiceName());
	}

    /**
     * Method that removes the OpenLS Presentation Service from the server.
     * 
     */
    public void destroy() {
    	Logger.getRootLogger().removeAllAppenders();
    	FileDelete.stop();
    }
    
    /**
     * Method to service response. - <br>
     * writes the content of the Response to the OutputStream of the HttpServletResponse
     * 
     * @param resp
     * 			the HttpServletResponse to which the content will be written
     * @param XLSResponse
     * 			the RespXLSDoc, whose content will be written to the OutputStream of resp param
     */
    private void doResponse(HttpServletResponse resp, ResponseXLSDocument XLSResponse) {
        try {
            String contentType = XLSResponse.getContentType();
            int contentLength = XLSResponse.getContentLength();
            byte[] bytes = XLSResponse.getByteArray();
            resp.setContentLength(contentLength);
            OutputStream out = resp.getOutputStream();
            resp.setContentType(contentType);
            //log.info("doResponse");
            out.write(bytes);
            out.close();
        } catch (IOException ioe) {
            mLogger.error("doResponse()", ioe);
        }
    }
}
