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

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.freeopenls.error.ServiceError;
import org.freeopenls.locationutilityservice.documents.ResponseXLSDocument;


/**
 * <p><b>Title: RequestOperator</b></p>
 * <p><b>Description:</b> Class RequestOperator <br>
 * After parsing the request through the doOperation() method, the
 * request is send up to the Location Utility Listener</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-02-09
 */
public class RequestOperator {
    /** Logger, used to log errors(exceptions) and additionally information */
    protected static Logger mLogger = Logger.getLogger(org.freeopenls.locationutilityservice.RequestOperator.class.getName());

    /**
     * Method that parse the transfered String, check what instance it is and
     * send, if it is a XLSDoc, the doc to the LocationUtilityListener(). Return the response
     * of the LocationUtilityListener().
     * 
     * @param sRequest
     *			String that contains the XMLRequest
     * @return RespXLSDoc
     * 			- Returns Response XLSDocument from the LocationUtilityListener()
     */
    public ResponseXLSDocument doOperation(String sRequest) {
    	
        ResponseXLSDocument response = null;
        XmlObject doc = null;

        try{
        	doc = XmlObject.Factory.parse(sRequest);
        } catch (XmlException xmle) {
        	mLogger.info("- doOperation() - Request is NOT well-formed! -");
        	ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.OTHER_XML, null, xmle.getMessage());
            return new ResponseXLSDocument(se.getErrorListXLSDocument(""));
        }
        
        //Check what Doc it is
        if (doc instanceof XLSDocument) {
            LUSListener locationlistener = new LUSListener();
        	response = locationlistener.receiveCompleteRequest(doc);
        }else{
        	mLogger.info("- doOperation() - Request is NOT a XLSDoc! -");
        }
        
        return response;
    }
}