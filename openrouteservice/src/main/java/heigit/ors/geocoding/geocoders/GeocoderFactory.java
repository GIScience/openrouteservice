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

public class GeocoderFactory {
  public static Geocoder createGeocoder(String name, String geocodingURL, String reverseGeocodingURL, String userAgent) throws Exception
  {
	  switch(name.toLowerCase())
	  {
	  	case "nominatim":
		  return new NominatimGeocoder(geocodingURL, reverseGeocodingURL, userAgent);
	  
	  	case "photon":
	  	  return new PhotonGeocoder(geocodingURL, reverseGeocodingURL, userAgent);
	  	  
	  	case "pelias":
		  	  return new PeliasGeocoder(geocodingURL, reverseGeocodingURL, userAgent);
	  }
	  
      throw new Exception("Unknown geocoder name.");
  }
}
