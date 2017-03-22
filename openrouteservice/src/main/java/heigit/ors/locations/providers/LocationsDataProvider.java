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
package heigit.ors.locations.providers;

import java.util.List;
import java.util.Map;

import heigit.ors.locations.LocationsCategory;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResult;

public interface LocationsDataProvider 
{
	public void init(Map<String, Object> parameters) throws Exception;	
	
	public void close() throws Exception;
	
	public List<LocationsResult> findLocations(LocationsRequest request) throws Exception;
	
	public List<LocationsCategory> findCategories(LocationsRequest request) throws Exception;
	
	public String getName();
}
