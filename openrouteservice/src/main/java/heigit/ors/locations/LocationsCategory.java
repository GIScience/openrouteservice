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
package heigit.ors.locations;

import java.util.Map;

public class LocationsCategory 
{
	private String _categoryName;
	private Map<Integer, Long> _stats; 
	private long _totalCount;

	public LocationsCategory(String category, Map<Integer, Long> stats, long totalCount)
	{
		_categoryName = category;
		_stats = stats;
		_totalCount = totalCount;
	}

	public String getCategoryName() 
	{
		return _categoryName;
	}  
	
	public long getTotalCount()
	{
		return _totalCount;
	}

	public Map<Integer, Long> getStats() 
	{
		return _stats;
	}
}
