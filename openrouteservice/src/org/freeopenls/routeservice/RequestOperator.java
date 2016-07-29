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

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.freeopenls.error.ServiceError;
import org.freeopenls.routeservice.documents.ResponseXLSDocument;

/**
 * <p><b>Title: RequestOperator</b></p>
 * <p><b>Description:</b> Class RequestOperator <br>
 * After parsing the request through the doOperation() method, the<br>
 * request is send up to the RSListener</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-08-01
 * @version 1.1 2008-04-20
 */
public class RequestOperator {
    /** Logger, used to log errors(exceptions) and additionally information */
    protected static Logger mLoggger = Logger.getLogger(RequestOperator.class.getName());

    /**
     * Method that parse the transfered String, check what instance it is and
     * send, if it is a XLSDoc, the doc to the RSListener(). Return the response
     * of the RSListener().
     * 
     * @param sRequest
     *			String that contains the XMLRequest
     * @return RespRouteXLSDoc
     * 			- Returns Response XLSDocument from the RSListener()
     */
    public ResponseXLSDocument doOperation(String sRequest) {
    	
    	ResponseXLSDocument response = null;
        XLSDocument doc = null;

        try{
        	if (sRequest == null)
        		sRequest = "";
        	
        	// 2015.05.22 In order to make the old scheme compatible with the new one.
        	sRequest = sRequest.replace("<xls:RoutePreference>Fastest</xls:RoutePreference>", "<xls:RoutePreference>Car</xls:RoutePreference>");
        	sRequest = sRequest.replace("<xls:RoutePreference>Shortest</xls:RoutePreference>", "<xls:RoutePreference>Car</xls:RoutePreference>");
        	
        	doc = (XLSDocument)XmlObject.Factory.parse(sRequest);
        	
            RSListener routeListener = new RSListener();
        	response = routeListener.receiveCompleteRequest(doc);

        } catch (XmlException xmle) {
        	mLoggger.info("- doOperation() - Request is NOT well-formed! -");
        	ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.OTHER_XML, null, xmle.getMessage());
            return new ResponseXLSDocument(se.getErrorListXLSDocument(""));
        }

        return response;
    }
}