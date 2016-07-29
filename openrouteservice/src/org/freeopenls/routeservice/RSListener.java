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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.freeopenls.constants.RouteService;
import org.freeopenls.constants.OpenLS.XLSParameter;
import org.freeopenls.error.ServiceError;
import org.freeopenls.routeservice.documents.ResponseXLSDocument;
import org.freeopenls.routeservice.documents.RequestXLSDocument;
import org.freeopenls.tools.CoordTransform;

import net.opengis.xls.AbstractBodyType;
import net.opengis.xls.AbstractHeaderType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.RequestHeaderType;
import net.opengis.xls.RequestType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;
import net.opengis.xls.XLSType;

/**
 * <p><b>Title: RSListener</b></p>
 * <p><b>Description:</b> Class RSListener (RouteServiceListener) - handles the RouteRequest to the Route Service </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-08-01
 * @version 1.1 2008-04-21
 */
public class RSListener {
	/** Logger, used to log errors(exceptions) and additionally information */
    private static final Logger mLogger = Logger.getLogger(RSListener.class.getName());

    /**
     * Method that validate the request, send the RouteRequest(s) to RouteXLSDoc<br>
     * and return the RouteResponse.<br>
     * Not supported in RequestHeader / Optional Parameters:<br>
     * - ClentName, ClientPassword<br>
     * - MSI - client-defined unique identifier
     * 
     * @param request
     *			XmlObject that contains the XLSDocument
     * @return RespRouteXLSDoc
     * 			- Returns Response XLSDocument from the RouteService
     */
    public synchronized ResponseXLSDocument receiveCompleteRequest(XmlObject request) {

    	ResponseXLSDocument response = null;
		XLSDocument xlsDoc = (XLSDocument) request;
		String sRequestID = null;

		try {
			///////////////////////////////////////
			//*** Validate XLSDoc ***
			validatexlsDoc(xlsDoc);
			
			//*** XLSType ***
			XLSType xlsType = xlsDoc.getXLS();
			String sXLSVersion = xlsType.getVersion().toString();		//Mandatory
			String sResponseLanguage = RouteService.DEFAULT_LANGUAGE;
			
			if(xlsType.isSetLang())
				sResponseLanguage = xlsType.getLang();
			
			if (!sXLSVersion.equalsIgnoreCase(RouteService.SERVICE_VERSION)) {
				ServiceError se = new ServiceError(SeverityType.ERROR);
				se.addError(ErrorCodeType.OTHER_XML, XLSParameter.version.toString(),
						"The value of the mandatory parameter '"+XLSParameter.version.toString()+"'"
						+ " must be '"+RouteService.SERVICE_VERSION+"'. Delivered value was: " + sXLSVersion);
				return new ResponseXLSDocument(se.getErrorListXLSDocument(sRequestID));
	        }
			
			//*** Header / Body ***
			AbstractHeaderType abstractheaderType = xlsType.getHeader();
			AbstractBodyType abType[] = xlsType.getBodyArray();
			
			//*** Header Information *** >>> OPTIONAL <<<
			RequestHeaderType reqheaderType = null;
			try{
				reqheaderType = (RequestHeaderType) abstractheaderType.changeType(RequestHeaderType.type);
			}catch(ClassCastException cce){
				ServiceError se = new ServiceError(SeverityType.ERROR);
				se.addError(ErrorCodeType.OTHER_XML, "RequestHeader","Element 'ResponseHeader' must be 'RequestHeader'!!");
				throw se;
			}
			
			//*** TODO ***
			//The client name, used for authentication
			//->String sClientName = reqheaderType.getClientName();
			
			//The client password, used for authentication
			//->String sClientPassword = reqheaderType.getClientPassword();
			
			//A client-defined unique session identifier, which should be returned in the response header
			String sSessionID = null;
			if(reqheaderType.isSetSessionID())				//Optional
				sSessionID = reqheaderType.getSessionID();
			
			//SRS for the Response - Default SRS => "EPSG:4326"
			String sSrsName = RouteService.DEFAULT_RESPONSE_SRS;
			if(reqheaderType.isSetSrsName()){
				sSrsName = reqheaderType.getSrsName();
				if(sSrsName.equals(""))
					sSrsName = RouteService.DEFAULT_RESPONSE_SRS;
				else
					sSrsName = CoordTransform.getEPSGCode(sSrsName);
			}
				
			//A client-defined unique identifier. Can be used for different purposes, for example billing
			//->String sMSID = reqheaderType.getMSID();

			RequestXLSDocument requestXSLDocument = new RequestXLSDocument(sSessionID, sSrsName, sResponseLanguage);
			
			///////////////////////////////////////
			//*** Handle the Request ***
			//Read every Request and "add" it to the ResponseDocument
			int iNumberReceivedRequests = abType.length;
			for(int i=0; i < iNumberReceivedRequests ; i++){
				RequestType reqType = (RequestType) abType[i].changeType(RequestType.type);
				sRequestID = reqType.getRequestID();
				requestXSLDocument.doRouteRequest(reqType);
			}
			//Get the XLS Document for the Response
			response = requestXSLDocument.getResponseXLSDocument();
				
		}
		catch (ServiceError se) {
			mLogger.error(ErrorCodeType.UNKNOWN + " OpenLS Route Service - RSListener, ServiceError \n Message: "+ se.getMessages());
	        return new ResponseXLSDocument(se.getErrorListXLSDocument(sRequestID));
		}
		catch (Exception e) {
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addInternalError(ErrorCodeType.UNKNOWN, "OpenLS Route Service - RSListener, Message: ", e);
			mLogger.error(ErrorCodeType.UNKNOWN + " OpenLS Route Service - RSListener, Exception \n Message: "+ se.getMessages());
            return new ResponseXLSDocument(se.getErrorListXLSDocument(sRequestID));
		}
		
		return response;
	}

    /**
     * Method that validate XLSDocument
     * 
     * @param xlsDoc
     *			XLSDocument
     * @throws ServiceError
     */
    private void validatexlsDoc(XLSDocument xlsDoc) throws ServiceError {
    	//Create an XmlOptions instance and set the error listener.
        ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);

        //Validate the XML document
        boolean isValid = xlsDoc.validate(validationOptions);

        //Create exception with error message if the xml document is invalid
        if (!isValid) {
            String message = null;
            String parameterName = null;

            //Get validation error and throw service exception for the first error
            Iterator<XmlError> iter = validationErrors.iterator();
            while (iter.hasNext()) {
                //Get name of the missing or invalid parameter
                message = iter.next().getMessage();
                if (message != null) {
                    String[] messageParts = message.split(" ");

                    if (messageParts.length > 3) {
                        parameterName = messageParts[2];
                    }
                    
                    //Create ServiceError
                    ServiceError se = new ServiceError(SeverityType.ERROR);
                    se.addError(ErrorCodeType.OTHER_XML, parameterName, "XmlBeans validation error: " + message);
                    throw se;
                }
            }
        }
    }
}