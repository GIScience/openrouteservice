

package org.freeopenls.constants;

/**
 * <p><b>Title: LocationUtilityService</b></p>
 * <p><b>Description:</b> Class for constants OpenLS Location Utility Service<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-06-05
 * @version 1.1 2008-04-21
 */
public class LocationUtilityService {
    /** Constant for the schema filename RouteService*/
    public static final String SCHEMA_FILENAME_LOCATIONUTILITYSERVICE = "http://schemas.opengis.net/ols/1.1.0/LocationUtilityService.xsd";
    /** Constant for the service version */
    public static final String SERVICE_VERSION = "1.1";
    /** Constants for the methodname of the LocationUtilityRequest */
    public static final String METHODNAME_GEOCODE = "GeocodeRequest";
    public static final String METHODNAME_REVERSEGEOCODE = "ReverseGeocodeRequest";
    /** Constant for the Address SpatialReferenceSystem (SRS) */
    public static final String ADDRESS_SRS = "EPSG:4326";
    /** Constant for the default Response SpatialReferenceSystem (SRS) */
    public static final String DEFAULT_RESPONSE_SRS = "EPSG:4326";
    
    /** Enum with names of rows in the address-book table */
    public enum AddressBookRowNames{
    	postalcode, municipal, strname;
    }
}
