/****************************************************
 Copyright (C) 2006 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.documents;

import java.math.BigDecimal;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;

import de.fhMainz.geoinform.aas.AASDocument;
import de.fhMainz.geoinform.aas.AASType;
import de.fhMainz.geoinform.aas.AbstractBodyType;
import de.fhMainz.geoinform.aas.AbstractHeaderType;
import de.fhMainz.geoinform.aas.ErrorCodeType;
import de.fhMainz.geoinform.aas.ErrorListType;
import de.fhMainz.geoinform.aas.ErrorType;
import de.fhMainz.geoinform.aas.ResponseHeaderType;
import de.fhMainz.geoinform.aas.ResponseType;
import de.fhMainz.geoinform.aas.SeverityType;

/**
 * Class ServiceError
 * 
 * @author Pascal Neis	pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2006-12-22
 */
public class ServiceError extends Exception {
	/** Serial Versin UID **/
    private static final  long serialVersionUID = 1L;
    /** ArrayList ErrorType **/
    private ArrayList<ErrorType> errs = new ArrayList<ErrorType>();
    /** Error Severity **/
    private SeverityType.Enum severity = null;
    
    private String mMessages = "";

    /**
     * Constructor - Sets the Error Severity
     */
    public ServiceError(SeverityType.Enum sev) {
        this.severity = sev;
    }

    /**
     * Method that adds an internal serivce Error to this service error with code, 
     * locator and the exception itself as parameters
     * 
     * @param code
     *        ErrorCode of the added error
     * @param locationPath
     *        String locator of the added error
     * @param e
     *        Exception which should be added
     */
    public void addInternalError(ErrorCodeType.Enum code, String locationPath, Exception e) {
        ErrorType et = ErrorType.Factory.newInstance();
        et.setErrorCode(code);
        et.setSeverity(this.severity);

        if (locationPath != null) {
            et.setLocationPath(locationPath);
        }

        String name = e.getClass().getName();
        String message = e.getMessage();
        StackTraceElement[] stackTraces = e.getStackTrace();

        StringBuffer sb = new StringBuffer();
        sb.append("Internal Service Exception");
        mMessages += "Internal Service Exception";
        if (this.severity.equals(SeverityType.WARNING)) {
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
                sb.append("[Exception]" + element.toString() + "\n");
                mMessages += " [Exception]" + element.toString() + "\n";
            }
        }
        et.setMessage(sb.toString());
        errs.add(et);
    }

    /**
     * Method that adds an Error with ErrorCode,locator and a single String message to this exception
     * 
     * @param code
     *        ErrorCode of the error to add
     * @param locationPath
     *        String locator of the error to add
     * @param message
     *        String message of the exception to add
     */
    public void addError(ErrorCodeType.Enum code, String locationPath, String message) {
        ErrorType et = ErrorType.Factory.newInstance();
        et.setErrorCode(code);
        et.setSeverity(this.severity);
        if (locationPath != null) {
            et.setLocationPath(locationPath);
        }
        et.setMessage(message);
        mMessages += message+"; ";
        errs.add(et);
    }
    
    /**
     * Method that returns a AASDocument with Errorlist and the added Errors
     * 
     * @return AASDocument
     *        - Return the AASDoc with ErrorList and Errors
     */
    public AASDocument getErrorListXLSDocument(String sRequestID) {
        
		//*** AASDocument ***
		AASDocument aasDocOut = AASDocument.Factory.newInstance();

		aasDocOut.documentProperties().setVersion("1.0");
		aasDocOut.documentProperties().setEncoding("UTF-8");
		aasDocOut.documentProperties().setSourceName("source");
		
		AASType aasTypeOut = aasDocOut.addNewAAS();
		aasTypeOut.setVersion(new BigDecimal("1.1"));

		//*** Header ***
		AbstractHeaderType ahTypeOut = aasTypeOut.addNewHeader();
		ResponseHeaderType rhTypeOut = (ResponseHeaderType) ahTypeOut.changeType(ResponseHeaderType.type);
		rhTypeOut.setSessionID("Error");
		
		//*** Body ***
		AbstractBodyType abTypeOut = aasTypeOut.addNewBody();
		ResponseType repType = (ResponseType) abTypeOut.changeType(ResponseType.type);
		repType.setRequestID(sRequestID);
		repType.setVersion("1.0");
		
		ErrorListType errorlistType = repType.addNewErrorList();
        errorlistType.setErrorArray(errs.toArray(new ErrorType[errs.size()]));
        
    //For well-formed xml
    //---
		XmlCursor cursor01 = aasDocOut.newCursor();
		if (cursor01.toFirstChild()) {
			cursor01.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"), "http://www.geoinform.fh-mainz.de/aas/AAS.xsd");
		}
		XmlCursor cursor02 = aasTypeOut.newCursor();
		if (cursor02.toChild(new QName("http://www.geoinform.fh-mainz.de/aas", "_Header"))) {
			cursor02.setName(new QName("http://www.geoinform.fh-mainz.de/aas","ResponseHeader"));
		}
		XmlCursor cursor03 = aasTypeOut.newCursor();
		if (cursor03.toChild(new QName("http://www.geoinform.fh-mainz.de/aas", "_Body"))) {
			cursor03.setName(new QName("http://www.geoinform.fh-mainz.de/aas","Response"));
		}
		cursor01.dispose();
		cursor02.dispose();
		cursor03.dispose();
	//---
		
        return aasDocOut;
    }
    
    public String getMessages(){
    	return mMessages;
    }
}
