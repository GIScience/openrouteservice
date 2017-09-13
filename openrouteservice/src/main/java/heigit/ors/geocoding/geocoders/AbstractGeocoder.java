/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.geocoding.geocoders;

public abstract class AbstractGeocoder implements Geocoder {

	protected String geocodingURL;
	protected String reverseGeocodingURL;
	protected String userAgent;
	
	public AbstractGeocoder(String geocodingURL, String reverseGeocodingURL, String userAgent)
	{
		this.geocodingURL = geocodingURL;
		this.reverseGeocodingURL = reverseGeocodingURL;
		this.userAgent = userAgent;
	}
	
	public abstract GeocodingResult[] geocode(String address, String languages, SearchBoundary searchBoundary, int limit) throws Exception;
	
	public abstract GeocodingResult[] geocode(Address address, String languages, SearchBoundary boundary, int limit) throws Exception;
	
	public abstract GeocodingResult[] reverseGeocode(double lon, double lat, int limit) throws Exception;
}
