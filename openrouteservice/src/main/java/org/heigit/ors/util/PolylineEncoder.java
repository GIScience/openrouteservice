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
package org.heigit.ors.util;

import org.locationtech.jts.geom.Coordinate;

public class PolylineEncoder {
	private PolylineEncoder() {}

	public static String encode(final Coordinate[] coords, boolean includeElevation, StringBuilder buffer) {
		long lat;
		long lon;
		long prevLat = 0;
	    long prevLon = 0;
	    long elev = 0;
	    long prevEle = 0;
	    
	    buffer.setLength(0);

	    for (final Coordinate c : coords) {
	      lat = Math.round(c.y * 1e5);
	      lon = Math.round(c.x * 1e5);

	      encode(lat - prevLat, buffer);
	      encode(lon - prevLon, buffer);
	      
	      if (includeElevation)
	      {
	    	  elev = (long)Math.floor(c.z * 100);
	    	  encode(elev - prevEle, buffer);
              prevEle = elev;
	      }
	      
	      prevLat = lat;
	      prevLon = lon;
	    }
	    
	    return buffer.toString();
	}
		
	private static void encode(long v, StringBuilder buffer) {
	    v = v < 0 ? ~(v << 1) : v << 1;
	    
	    while (v >= 0x20) 
	    {
	      buffer.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
	      v >>= 5;
	    }
	    
	    buffer.append(Character.toChars((int) (v + 63)));
	}
}
