/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.util;

import com.vividsolutions.jts.geom.Coordinate;

public class PolylineEncoder {
	public static String encode(final Coordinate[] coords, boolean includeElevation, StringBuffer buffer) {
		long lat, lon;
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
	
	private static void encode(long v, StringBuffer buffer) {
	    v = v < 0 ? ~(v << 1) : v << 1;
	    
	    while (v >= 0x20) 
	    {
	      buffer.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
	      v >>= 5;
	    }
	    
	    buffer.append(Character.toChars((int) (v + 63)));
	}
}
