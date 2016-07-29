/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.emergencyrouteservice;


/**
 * <p><b>Title: Class for ERS Constants </b></p>
 * <p><b>Description:</b> ERS Constants </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-06-05
 */
public class ERSConstants {
	/** Constant for the content type of the response */
    public static final String CONTENT_TYPE_XML = "text/xml";
    /** Constant for the schema location OpenLS*/
    public static String SCHEMA_LOCATION_OPENLS = "http://www.opengis.net/xls";
    /** Constant for the schema location SLD*/
    public static String SCHEMA_LOCATION_SLD = "http://www.opengis.net/ows";
    /** Constant for the schame filename */
    public static final String SCHEMA_FILENAME_XLS = "XLS.xsd";
    /** Constant for the schame filename RouteService*/
    public static final String SCHEMA_FILENAME_ROUTESERVICE = "RouteService.xsd";
    /** Constant for the schame filename GetMap*/
    public static final String SCHEMA_FILENAME_GETMAP = "GetMap.xsd";
    /** Constant for the service version */
    public static final String SERVICE_VERSION = "1.1";
    /** Constant for the mathodname of the RouteRequest */
    public static final String METHODNAME = "RouteRequest";
    /** Constant for the SpatialReferenceSystem (SRS) in which the Graph is */
    public static String GRAPH_SRS = "EPSG:4326";
    /** Constant for the default Response SpatialReferenceSystem (SRS) */
    public static final String DEFAULT_RESPONSE_SRS = "EPSG:4326";
    /** Constant for the default Response Language) */
    public static final String DEFAULT_LANGUAGE = "en";
    
    
    /** Enum with names of XLSParameters */
    public enum XLSParameter{
        version, lang;
    }

    /** Enum with names of RequestParameters */
    public enum RequestParameter{
    	methodName, version, requestID, maximumResponses;
    }

    /** Enum with names of RouteHandleParameters */
    public enum RouteHandleParameter{
    	RouteID, ServiceID;
    }

    /** Enum with names of RouteMapRequestParameters */
    public enum RouteMapRequestParameter{
    	width, height, format;
    }

    /** Enum with names of RouteInstructionRequestParameters */
    public enum RouteInstructionRequestParameter{
    	format;
    }
}
