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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.freeopenls.error.ServiceError;
import org.freeopenls.presentationservice.documents.RequestXLSDocument;
import org.freeopenls.presentationservice.documents.ResponseXLSDocument;

import net.opengis.xls.AbstractBodyType;
import net.opengis.xls.AbstractHeaderType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.RequestHeaderType;
import net.opengis.xls.RequestType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;
import net.opengis.xls.XLSType;


/**
 * <p><b>Title: PresentationListener </b></p>
 * <p><b>Description:</b> Class PresentationListener - handles the Request to the OpenLS Presentation Service </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-05-24
 */
public class PSListener {
	/** Logger, used to log errors(exceptions) and additionally information */
    private static final Logger mLogger = Logger.getLogger(PSListener.class.getName());

    /**
     * Method that validate the request, send the Request(s) to XLSDoc<br>
     * and return the Response.<br>
     * 
     * @param request
     *			XmlObject that contains the XLSDocument
     * @return RespXLSDoc
     * 			- Returns Response XLSDocument from the Serivcce
     */
    public ResponseXLSDocument doRequest(XmlObject request) {

		ResponseXLSDocument response = null;
		XLSDocument xlsDoc = (XLSDocument) request;
		String sRequestID = null;

		try {
			///////////////////////////////////////
			//*** Validate XLSDoc ***
			validatexlsDoc(xlsDoc);

			//*** XLSType ***
			XLSType xlsType = xlsDoc.getXLS();
			String sXLSVersion = xlsType.getVersion().toString();	//Mandatory
			//String sResponseLanguage = "de";	//Optional
			
//			if(xlsType.isSetLang())
//					sResponseLanguage = xlsType.getLang();
			
			if (!sXLSVersion.equalsIgnoreCase("1.1")) {
	        	ServiceError se = new ServiceError(SeverityType.ERROR);
	            se.addError(ErrorCodeType.OTHER_XML, "1.1",
	                                 "The value of the mandatory parameter 'Version'"
                                     + " must be '1.1'. Delivered value was: " + sXLSVersion);
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
                se.addError(ErrorCodeType.OTHER_XML, "RequestHeader",
                                     "Element 'ResponseHeader' must be 'RequestHeader'!!");
                throw se;
			}
			//TODO
			//The client name, used for authentication
			//->String sClientName = reqheaderType.getClientName();
			//The client password, used for authentication
			//->String sClientPassword = reqheaderType.getClientPassword();
			//A client-defined unique session Iidentifier, which should be returned in the response header
			String sSessionID = null;
			if(reqheaderType.isSetSessionID())				//Optional
				sSessionID = reqheaderType.getSessionID();
			//In general this reference points to a CRS instance of gml:CoordinateReferenceSystemType 
			//(see coordinateReferenceSystems.xsd). For well known references it is not required that 
			//the CRS description exists at the location the URI points to (Note: These "WKCRS"-ids 
			//still have to be specified).  If no srsName attribute is given, the CRS must be specified
			//as part of the larger context this geometry element is part of, e.g. a geometric aggregate.
			String sSRSName = "EPSG:4326";
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
			}
			
			//A client-defined unique Identifier. Can be used for different purposes, for example billing
			//->String sMSID = reqheaderType.getMSID();

			RequestXLSDocument presentationXLSDoc = new RequestXLSDocument(sSessionID, sSRSName);
			
			///////////////////////////////////////
			//*** Handle the Request ***
			//Read every Request and "add" it to the ResponseDocument
			int iNumberReceivedRequests = abType.length;
			for(int i=0; i < iNumberReceivedRequests ; i++){
				RequestType reqType = (RequestType) abType[i].changeType(RequestType.type);
				sRequestID = reqType.getRequestID();
				presentationXLSDoc.addRequest(reqType);
			}
				
			response = presentationXLSDoc.getResponseXLSDocument();
			
		}
		catch (ServiceError se) {
			mLogger.error(ErrorCodeType.UNKNOWN + " OpenLS Presentation Service - PSListener, ServiceError \n Message: "+ se.getMessages());
			return new ResponseXLSDocument(se.getErrorListXLSDocument(""));
		}
		catch (Exception e) {
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addInternalError(ErrorCodeType.UNKNOWN, "OpenLS Presentation Service", e);
            mLogger.error(ErrorCodeType.UNKNOWN + " OpenLS Presentation Service - PSListener, Exception \n Message: "+ se.getMessages());
            return new ResponseXLSDocument(se.getErrorListXLSDocument(""));
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