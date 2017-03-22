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

public class LocationsSearchFilter {
	private int[] _category_ids = null;
	private int[] _category_group_ids = null;
	private String _name = null;
	private String _wheelchair;
	private String _smoking;
	private Boolean _fee;
	//private Boolean _opennow; TODO

	public LocationsSearchFilter()
	{

	}

	public int[] getCategoryIds() {
		return _category_ids;
	}

	public void setCategoryIds(int[] values) {
		_category_ids = values;
	}

	public int[] getCategoryGroupIds() {
		return _category_group_ids;
	}

	public void setCategoryGroupIds(int[] values) {
		_category_group_ids = values;
	}

	public String getName() {
		return _name;
	}

	public void setName(String value) {
		_name = value;
	}

	public String getWheelchair() {
		return _wheelchair;
	}

	public void setWheelchair(String value) {
		_wheelchair = value;
	}

	public String getSmoking() {
		return _smoking;
	}

	public void setSmoking(String value) {
		_smoking = value;
	}

	public Boolean getFee() {
		return _fee;
	}

	public void setFee(Boolean value) {
		_fee = value;
	}
}
