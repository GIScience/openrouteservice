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

public class GeocodingResult  {
	public String country;
	public String countryCode;
	public String region;
	public String county;
	public String municipality;
	public String locality;
	public String borough;
	public String neighbourhood;
	public String postalCode;
	public String street;
	public String name;
	public String houseNumber;
	public String objectName;
	public String placeType;
	
	public double latitude;
	public double longitude;
	
	public double distance = 0.0;
	
	public float confidence = 0.5F;
}
