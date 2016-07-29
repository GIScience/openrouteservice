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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;



import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.freeopenls.accessibilityanalyseservice.documents.RequestAASDocument;
import org.freeopenls.accessibilityanalyseservice.documents.ResponseAASDocument;
import org.freeopenls.accessibilityanalyseservice.documents.ServiceError;
import org.freeopenls.tools.CoordTransform;

import de.fhMainz.geoinform.aas.AASDocument;
import de.fhMainz.geoinform.aas.AASType;
import de.fhMainz.geoinform.aas.AbstractBodyType;
import de.fhMainz.geoinform.aas.AbstractHeaderType;
import de.fhMainz.geoinform.aas.ErrorCodeType;
import de.fhMainz.geoinform.aas.RequestHeaderType;
import de.fhMainz.geoinform.aas.RequestType;
import de.fhMainz.geoinform.aas.SeverityType;


/**
 * <p><b>Title: AASListener</b></p>
 * <p><b>Description:</b> Class AASListener - handles the requests to the AAS </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 3.0 2006-12-22
 */
public class AASListener {
	/** Logger, used to log errors(exceptions) and additionally information **/
    private static final Logger mLogger = Logger.getLogger(AASListener.class.getName());

    /**
     * Method that validate the request, send the AccessibilityRequest(s) to RouteXLSDoc<br>
     * and return the AccessibilityResponse.<br>
     * 
     * @param request
     *			XmlObject that contains the AASDocument
     * @return RespAASDoc
     * 			- Returns Response AASDocument from the AccessibilityService
     */
	public synchronized ResponseAASDocument doRequest(XmlObject request) {
		ResponseAASDocument response = null;
		AASDocument aasDoc = (AASDocument) request;
		
		try {
				validatexlsDoc(aasDoc);

				//*** AASType ***
				AASType aasType = aasDoc.getAAS();
				String sAASVersion = aasType.getVersion().toString();	//Mandatory
				String sSRSName = "EPSG:4326";
				
				if (!sAASVersion.equalsIgnoreCase("1.0")) {
		        	ServiceError se = new ServiceError(SeverityType.ERROR);
		            se.addError(ErrorCodeType.OTHER_XML, "Version",
		                                 "The value of the mandatory parameter 'Version' must be '1.0'. Delivered value was: " + sAASVersion);
		            return new ResponseAASDocument(se.getErrorListXLSDocument("XY"));
		        }
				
				//*** Header / Body ***
				AbstractHeaderType abstractheaderType = aasType.getHeader();
				AbstractBodyType abType[] = aasType.getBodyArray();
								
				//*** Header Information *** >>> OPTIONAL <<< 
				RequestHeaderType reqheaderType = null;
				try{
					reqheaderType = (RequestHeaderType) abstractheaderType.changeType(RequestHeaderType.type);
				}catch(ClassCastException cce){
                    ServiceError se = new ServiceError(SeverityType.ERROR);
                    se.addError(ErrorCodeType.OTHER_XML, "RequestHeader",
                                         "Element 'ResponseHeader' must be 'RequestHeader'!!");
                    throw se;
				}

				//The client name, used for authentication
//				if(reqheaderType.isSetClientName()){
//					String sClientName = reqheaderType.getClientName();
//					if(!this.AASconfig.getClientName().equals(sClientName)){
//	                    ServiceError se = new ServiceError(SeverityType.ERROR);
//	                    se.addError(ErrorCodeType.OTHER_XML, "ClientName",
//	                                         "Authentication failed for ClientName = '"+sClientName+"'!!!");
//	                    throw se;
//					}
//
//				}
//				else{
//                    ServiceError se = new ServiceError(SeverityType.ERROR);
//                    se.addError(ErrorCodeType.OTHER_XML, "ClientName",
//                                         "'ClientName'-Attribute is mising!!");
//                    throw se;
//				}
				//The client password, used for authentication
//				if(reqheaderType.isSetClientPassword()){
//					String sClientPassword = reqheaderType.getClientPassword();
//					if(!this.AASconfig.getClientPasswd().equals(sClientPassword)){
//	                    ServiceError se = new ServiceError(SeverityType.ERROR);
//	                    se.addError(ErrorCodeType.OTHER_XML, "ClientPassword",
//	                                         "Authentication failed, wrong Password!!!");
//	                    throw se;
//					}
//				}
//				else{
//                    ServiceError se = new ServiceError(SeverityType.ERROR);
//                    se.addError(ErrorCodeType.OTHER_XML, "ClientPassword",
//                                         "'ClientPassword'-Attribute is mising!!");
//                    throw se;
//				}
				
				//A client-defined unique session identifier, which should be returned in the response header
				String sSessionID = null;
				if(reqheaderType.isSetSessionID())				//Optional
					sSessionID = reqheaderType.getSessionID();
				
				//In general this reference points to a CRS instance of gml:CoordinateReferenceSystemType 
				//(see coordinateReferenceSystems.xsd). For well known references it is not required that 
				//the CRS description exists at the location the URI points to (Note: These "WKCRS"-ids 
				//still have to be specified).  If no srsName attribute is given, the CRS must be specified
				//as part of the larger context this geometry element is part of, e.g. a geometric aggregate. 
				if(reqheaderType.isSetSrsName()){
					sSRSName = reqheaderType.getSrsName();
					if(sSRSName == null || sSRSName.equals("")){
			        	ServiceError se = new ServiceError(SeverityType.ERROR);
			            se.addError(ErrorCodeType.OTHER_XML,
			                                 "SRSName",
			                                 "The value of the optional attribute '" 
			                                 + "SRSName"+"'"
	                                         + " is 'null'. Possible value is e.g. 'EPSG:4326'!");
			            throw se;
					}
					sSRSName = CoordTransform.getEPSGCode(sSRSName);
				}
				
				//A client-defined unique Iidentifier. Can be used for different purposes, for example billing
				//->String sMSID = reqheaderType.getMSID();

				///////////////////////////////////////
				//*** AnalyseAASDoc ***
				RequestAASDocument anaylseAASDoc = new RequestAASDocument(sSessionID);
				//Read every Request and "add" it to the ResponseDocument
				int iNumberReceivedRequests = abType.length;
				for(int i=0; i < iNumberReceivedRequests ; i++){
					RequestType reqType = (RequestType) abType[i].changeType(RequestType.type);
					anaylseAASDoc.addAnalyseRequest(sSRSName, reqType);
				}
					
				response = new ResponseAASDocument(anaylseAASDoc.getAASDoc());
				
		}
		catch (ServiceError se) {
			mLogger.error(ErrorCodeType.UNKNOWN + " AAS - AASListener, ServiceError \n Message: "+ se.getMessages());
			return new ResponseAASDocument(se.getErrorListXLSDocument("123456789"));
		}
		catch (Exception e) {
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addInternalError(ErrorCodeType.UNKNOWN, "AAS - AccessibilityListener", e);
			mLogger.error(ErrorCodeType.UNKNOWN + " AAS - AASListener, Exception \n Message: "+ se.getMessages());
			return new ResponseAASDocument(se.getErrorListXLSDocument("123456789"));
		}
		
		return response;
	}
		
    /**
     * Method that validate AASDocument
     * 
     * @param aasDoc
     *			AASDocument
     * @throws ServiceError
     */
    private void validatexlsDoc(AASDocument aasDoc) throws ServiceError {
    	//Create an XmlOptions instance and set the error listener.
        ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);

        //Validate the XML document
        boolean isValid = aasDoc.validate(validationOptions);

        //Create Exception with error message if the xml document is invalid
        if (!isValid) {
            String message = null;
            String parameterName = null;

            //Get Validation error and throw service exception for the first error
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
                    se.addError(ErrorCodeType.OTHER_XML, parameterName,
                                         "XmlBeans validation error: " + message);
                    throw se;
                }
            }
        }
    }
}
