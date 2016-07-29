

package org.freeopenls.constants;

/**
 * <p><b>Title: DirectoryService</b></p>
 * <p><b>Description:</b> Class for constants OpenLS Directory Service<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-06-05
 * @version 1.1 2008-04-21
 */
public class DirectoryService {
    /** Constant for the schema filename DirectoryService*/
    public static final String SCHEMA_FILENAME_DIRECTORYSERVICE = "http://schemas.opengis.net/ols/1.1.0/DirectoryService.xsd";
    /** Constant for the service version */
    public static final String SERVICE_VERSION = "1.1";
    /** Constant for the methodname of the DirectoryRequest */
    public static final String METHODNAME = "DirectoryRequest";
    /** Constant for the default Response SpatialReferenceSystem (SRS) */
    public static final String DEFAULT_RESPONSE_SRS = "EPSG:4326";
    /** Constant for the Address SpatialReferenceSystem (SRS) */
    public static final String DATABASE_SRS = "EPSG:4326";
    /** Constant for the default Response Language) */
    public static final String DEFAULT_LANGUAGE = "en";

}