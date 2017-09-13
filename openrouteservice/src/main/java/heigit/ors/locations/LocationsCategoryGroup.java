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

public class LocationsCategoryGroup 
{
	private String _name;
	private int _id;
	private int _minCategoryId;
	private int _maxCategoryId;
	private Map<String, Integer> _categories;

	public LocationsCategoryGroup(int id, String name, int minId, int maxId, Map<String, Integer> categories)
	{
		_id = id;
		_name = name;
		_minCategoryId = minId;
		_maxCategoryId = maxId;
		_categories = categories;
	}

	public String getName() {
		return _name;
	}

	public int getId() {
		return _id;
	}

	public int getMinCategoryId() {
		return _minCategoryId;
	}

	public int getMaxCategoryId() {
		return _maxCategoryId;
	}
	
	public Map<String, Integer> getCategories()
	{
		return _categories;
	}
}
