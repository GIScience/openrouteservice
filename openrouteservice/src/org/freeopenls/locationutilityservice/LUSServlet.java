/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.locationutilityservice;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.freeopenls.locationutilityservice.documents.ResponseXLSDocument;
import org.freeopenls.tools.StreamUtility;
import org.freeopenls.tools.StringUtility;


/**
 * <p><b>Title: LUSServlet</b></p>
 * <p><b>Description:</b> Class Servlet Location Utility Service </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-02-09
 * @version 1.1 2008-04-29
 */
public class LUSServlet extends HttpServlet {
	/** Serial Version UID */
	static final long serialVersionUID = 1;
	/** Logger, used to log errors(exceptions) and additionally information */
	private static Logger mLogger = Logger.getLogger(LUSConfigurator.class.getName());
	/** RequestOperator - handles the requests and send them up to the specific requestListeners */
    private RequestOperator mRequestOperator;

    /** LocationUtilityConfigurator **/
    private LUSConfigurator mLUSConfigurator;

    /**
     * Method that initialize the Location Utility Service -> LocationUtilityConfigurator.
     * 
     * @throws ServletException
     */
	public void init() {
		// Initialize configurator
		mLUSConfigurator = LUSConfigurator.getInstance();
	}

    /**
     * Method to service requests. (POST) - <br>
     * the request will be passed to the LocationUtilityListener
     * 
     * @param request
     * 				HttpServletRequest - incoming Request
     * @param response 
     * 				HttpServletResponse - response of the incoming request
     * @throws ServletException
     * @throws IOException
     */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		mLogger.info("PostRequest;IP:"+request.getRemoteAddr()+";"+mLUSConfigurator.getServiceName());
		long requestTime = System.currentTimeMillis();
		
		response.setContentType("text/html");
		
		try{
			InputStream in = request.getInputStream();
			String decodedString = StringUtility.decodeRequestString(StreamUtility.readStream(in, "UTF-8"));
			in.close();
		
            this.mRequestOperator = new RequestOperator();
           
            ResponseXLSDocument respXLS = this.mRequestOperator.doOperation(decodedString);
            doResponse(response, respXLS);
            
            in.close();
            
		}catch(Exception e){
			mLogger.error("OpenLS LUS - doPost - Error: ", e);
		}
		
		long responseTime = System.currentTimeMillis();
		long handlingTime = responseTime-requestTime;
		double handlingTimeSeconds = (double)handlingTime/1000;

		mLogger.info("DoneRequest;IP:"+request.getRemoteAddr()+";Time:"+Double.toString(handlingTimeSeconds).replace(".",",")+";"+mLUSConfigurator.getServiceName());
	}
	
    /**
     * Method that removes the Location Utility Servlet from the server.
     * 
     */
    public void destroy() {
    	//Remove all appenders
    	Logger.getRootLogger().removeAllAppenders();
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
            mLogger.error("OpenLS LUS - doResponse() - Error: ", ioe);
        }
    }
}
