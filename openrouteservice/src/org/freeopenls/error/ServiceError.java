
package org.freeopenls.error;

import java.math.BigDecimal;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import net.opengis.xls.AbstractBodyType;
import net.opengis.xls.AbstractHeaderType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.ErrorListType;
import net.opengis.xls.ErrorType;
import net.opengis.xls.ResponseHeaderType;
import net.opengis.xls.ResponseType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.XLSDocument;
import net.opengis.xls.XLSType;

import org.apache.xmlbeans.XmlCursor;
import org.freeopenls.constants.OpenLS;
import org.freeopenls.constants.RouteService;


/**
 * <p><b>Title: ServiceError</b></p>
 * <p><b>Description:</b> Class for ServiceError<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class ServiceError extends Exception {
	/** Serial Version UID **/
    private static final  long serialVersionUID = 1L;
    /** ArrayList ErrorType **/
    private ArrayList<ErrorType> m_Errors = new ArrayList<ErrorType>();
    /** Error Severity **/
    private SeverityType.Enum m_Severity = null;
    
    private String mMessages = "";

    /**
     * Constructor - Sets the Error Severity
     */
    public ServiceError(SeverityType.Enum sev) {
        m_Severity = sev;
    }

    /**
     * Method that adds an internal SerivceError to this service error with code, 
     * locator and the exception itself as parameters
     * 
     * @param errorcode
     *        ErrorCode of the added error
     * @param sLocationPath
     *        Locaion Path of the added error
     * @param e
     *        Exception
     */
    public void addInternalError(ErrorCodeType.Enum errorcode, String sLocationPath, Exception e) {
        ErrorType et = ErrorType.Factory.newInstance();
        et.setErrorCode(errorcode);
        et.setSeverity(m_Severity);

        if (sLocationPath != null) {
            et.setLocationPath(sLocationPath);
        }

        String name = e.getClass().getName();
        String message = e.getMessage();
        StackTraceElement[] stackTraces = e.getStackTrace();

        StringBuffer sb = new StringBuffer();
        sb.append("Internal Service Exception");
        mMessages += "Internal Service Exception";
        if (m_Severity.equals(SeverityType.WARNING)) {
            sb.append(". Message: " + message);
            mMessages += ". Message: " + message;
        }
        else{//SeverityType.ERROR
            sb.append(": " + name + "\n");
            mMessages += ": " + name + " ";
            sb.append("Internal Service Exception Message: " + message + "\n");
            mMessages += "Internal Service Exception Message: " + message + "\n";
            for (int i = 0; i < stackTraces.length; i++) {
                StackTraceElement element = stackTraces[i];
                sb.append(" [Exception]" + element.toString() + "\n");
                mMessages += " [Exception]" + element.toString() + "\n";
            }
        }
        et.setMessage(sb.toString());
        m_Errors.add(et);
    }

    /**
     * Method that adds an ServiceError with ErrorCode,locator and a single String message to this exception
     * 
     * @param errorcode
     * 			ErrorCode of the error to add
     * @param locationPath
     * 			Location Path of the added error
     * @param message
     * 			Message of the exception
     */
    public void addError(ErrorCodeType.Enum errorcode, String locationPath, String message) {
        ErrorType et = ErrorType.Factory.newInstance();
        et.setErrorCode(errorcode);
        et.setSeverity(m_Severity);
        if (locationPath != null) {
            et.setLocationPath(locationPath);
        }
        et.setMessage(message);
        mMessages += message+"; ";
        m_Errors.add(et);
    }
    
    /**
     * Method that returns a XLSDocument with Errorlist and the added Errors
     * 
     * @return XLSDocument
     * 			XLSDoc with ErrorList and Errors
     */
    public XLSDocument getErrorListXLSDocument(String sRequestID) {
        
		//*** XLSDocument ***
		XLSDocument xlsDocOut = XLSDocument.Factory.newInstance();

		xlsDocOut.documentProperties().setVersion("1.0");
		xlsDocOut.documentProperties().setEncoding("UTF-8");
		xlsDocOut.documentProperties().setSourceName("source");
		
		XLSType xlsTypeOut = xlsDocOut.addNewXLS();
		xlsTypeOut.setVersion(new BigDecimal("1.1"));

		//*** Header ***
		AbstractHeaderType ahTypeOut = xlsTypeOut.addNewHeader();
		ResponseHeaderType rhTypeOut = (ResponseHeaderType) ahTypeOut.changeType(ResponseHeaderType.type);
		rhTypeOut.setSessionID("Error");
		
		//*** Body ***
		AbstractBodyType abTypeOut = xlsTypeOut.addNewBody();
		ResponseType repType = (ResponseType) abTypeOut.changeType(ResponseType.type);
		repType.setRequestID(sRequestID);
		repType.setVersion(RouteService.SERVICE_VERSION);
		
		ErrorListType errorlistType = repType.addNewErrorList();
        errorlistType.setErrorArray(m_Errors.toArray(new ErrorType[m_Errors.size()]));
        
	    //For well-formed xml
	    //---
			XmlCursor cursorXLSDoc = xlsDocOut.newCursor();
			if (cursorXLSDoc.toFirstChild()) {
				cursorXLSDoc.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"), OpenLS.SCHEMA_LOCATION_OPENLS+OpenLS.SCHEMA_FILENAME_XLS);
			}
			XmlCursor cursorXLSType = xlsTypeOut.newCursor();
			if (cursorXLSType.toChild(new QName("http://www.opengis.net/xls", "_Header"))) {
				cursorXLSType.setName(new QName("http://www.opengis.net/xls","ResponseHeader"));
			}
			if (cursorXLSType.toChild(new QName("http://www.opengis.net/xls", "_Body"))) {
				cursorXLSType.setName(new QName("http://www.opengis.net/xls","Response"));
			}
			cursorXLSDoc.dispose();
			cursorXLSType.dispose();
		//---
		
        return xlsDocOut;
    }
    
    public String getMessages(){
    	return mMessages;
    }
}
