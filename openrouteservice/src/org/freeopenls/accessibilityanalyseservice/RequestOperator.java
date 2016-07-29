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


import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.freeopenls.accessibilityanalyseservice.documents.ResponseAASDocument;
import org.freeopenls.accessibilityanalyseservice.documents.ServiceError;

import de.fhMainz.geoinform.aas.AASDocument;
import de.fhMainz.geoinform.aas.ErrorCodeType;
import de.fhMainz.geoinform.aas.SeverityType;


/**
 * <p><b>Title: RequestOperator</b></p>
 * <p><b>Description:</b> Class RequestOperator <br>
 * After parsing the request through the doOperation() method, the<br>
 * request is send up to the AASListener</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-01-05
 */
public class RequestOperator {
    /** Logger, used to log errors(exceptions) and additonaly information */
    protected static Logger mLogger = Logger.getLogger(RequestOperator.class.getName());

    /**
     * Method that parse the transfered String, check what instance it is and
     * send, if it is a AASDoc, the doc to the AccessibilityListener(). Return the response
     * of the AccessibilityListener().
     * 
     * @param sRequest
     *			String that contains the XMLRequest
     * @return RespRouteXLSDoc
     * 			- Returns Response AASDocument from the AccessibilityListener()
     */
    public ResponseAASDocument doOperation(String sRequest) {
    	
        ResponseAASDocument response = null;
        XmlObject doc = null;

        try{
        	doc = XmlObject.Factory.parse(sRequest);
        } catch (XmlException xmle) {
        	mLogger.info("- doOperation() - Request is NOT well-formed! -");
        	ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.OTHER_XML, null, xmle.getMessage());
            return new ResponseAASDocument(se.getErrorListXLSDocument(""));
        }
        
        //Check what Doc it is
        if (doc instanceof AASDocument) {
            AASListener accessibilityListener = new AASListener();
        	response = accessibilityListener.doRequest(doc);
        }else{
        	mLogger.error("- doOperation() - Request is NOT a AASDoc! -");
        }
        
        return response;
    }
}