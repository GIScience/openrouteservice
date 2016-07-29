

package org.freeopenls.constants;

/**
 * <p><b>Title: RouteService</b></p>
 * <p><b>Description:</b> Class for constants OpenLS RouteService<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-06-05
 * @version 1.1 2008-04-21
 */
public class RouteService {
    /** Constant for the schema filename RouteService*/
    public static final String SCHEMA_FILENAME_ROUTESERVICE = "http://schemas.opengis.net/ols/1.1.0/RouteService.xsd";
    /** Constant for the service version */
    public static final String SERVICE_VERSION = "1.1";
    /** Constant for the methodname of the RouteRequest */
    public static final String METHODNAME = "RouteRequest";
    /** Constant for the Graph SpatialReferenceSystem (SRS) */
    public static final String GRAPH_SRS = "EPSG:4326";
    /** Constant for the default Response SpatialReferenceSystem (SRS) */
    public static final String DEFAULT_RESPONSE_SRS = "EPSG:4326";
    /** Constant for the default Response Language) */
    public static final String DEFAULT_LANGUAGE = "en";

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
