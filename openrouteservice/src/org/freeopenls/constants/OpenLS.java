

package org.freeopenls.constants;

/**
 * <p><b>Title: OpenLS</b></p>
 * <p><b>Description:</b> Class for constants OpenLS<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-06-05
 */
public class OpenLS {
	/** Constant for the content type of the response */
    public static final String CONTENT_TYPE_XML = "text/xml";
    /** Constant for the schema location OpenLS*/
    public static final String SCHEMA_LOCATION_OPENLS = "http://www.opengis.net/xls";
    /** Constant for the schame filename */
    public static final String SCHEMA_FILENAME_XLS = "XLS.xsd";
    
    /** Enum with names of XLSParameters */
    public enum XLSParameter{
        version, lang;
    }

    /** Enum with names of RequestParameters */
    public enum RequestParameter{
    	methodName, version, requestID, maximumResponses;
    }
}
