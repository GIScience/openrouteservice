/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

package heigit.ors.geocoding.geocoders;

import org.json.JSONObject;

public class Address 
{
    private String _address;
    private String _neighbourhood;
    private String _borough;
    private String _locality;
    private String _county;
    private String _region;
    private String _postalcode;
    private String _country;

    public Address()
    {}
    
    public boolean isValid()
    {
    	return _address != null || _neighbourhood != null || _borough != null || _locality != null || _county != null || _region != null || _postalcode != null || _country != null;
    }
	
	public static Address fromJson(JSONObject json)
	{
		Address addr = new Address();
		if (json.has("address"))
			addr._address = json.get("address").toString();
		if (json.has("neighbourhood"))
			addr._neighbourhood = json.get("neighbourhood").toString();
		if (json.has("borough"))
			addr._borough = json.get("borough").toString();
		if (json.has("locality"))
			addr._locality = json.get("locality").toString();
		if (json.has("county"))
			addr._county = json.get("county").toString();
		if (json.has("region"))
			addr._region = json.get("region").toString();
		if (json.has("postalcode"))
			addr._postalcode = json.get("postalcode").toString();
		if (json.has("country"))
			addr._country = json.get("country").toString();
		
		return addr;
	}
	
	public String toString()
	{
		JSONObject json = new JSONObject(true);
		
		if (_address != null)
			json.put("address", _address);
		if (_neighbourhood != null)
			json.put("neighbourhood", _neighbourhood);
		if (_borough != null)
			json.put("borough", _borough);
		if (_locality != null)
			json.put("locality", _locality);
		if (_county != null)
			json.put("county", _county);
		if (_region != null)
			json.put("region", _region);
		if (_postalcode != null)
			json.put("postalcode", _postalcode);
		if (_country != null)
			json.put("country", _country);
		
		return json.toString();
	}
}
