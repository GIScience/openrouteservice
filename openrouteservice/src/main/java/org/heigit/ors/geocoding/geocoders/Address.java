/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */

package heigit.ors.geocoding.geocoders;

import java.util.HashMap;
import java.util.Map;

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
	
	public Map<String, String> toMap()
	{
		Map<String, String> map = new HashMap<String, String>();
		
		if (_address != null)
			map.put("address", _address);
		if (_neighbourhood != null)
			map.put("neighbourhood", _neighbourhood);
		if (_borough != null)
			map.put("borough", _borough);
		if (_locality != null)
			map.put("locality", _locality);
		if (_county != null)
			map.put("county", _county);
		if (_region != null)
			map.put("region", _region);
		if (_postalcode != null)
			map.put("postalcode", _postalcode);
		if (_country != null)
			map.put("country", _country);
		
		return map;
	}
	
	public String getAddress() 			{ return _address; }
	public String getNeighbourhood() 	{ return _neighbourhood; }
	public String getBorough() 			{ return _borough; } 
	public String getLocality()			{ return _locality; }
	public String getCounty()			{ return _county; }
	public String getRegion()			{ return _region; }
	public String getPostalcode()		{ return _postalcode; }
	public String getCountry()			{ return _country; }
}
