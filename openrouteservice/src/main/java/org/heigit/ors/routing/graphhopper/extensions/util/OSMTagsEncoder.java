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
package heigit.ors.routing.graphhopper.extensions.util;

public class OSMTagsEncoder {

	public static int getTrackValue(String trackType) {
		if ("grade1".equals(trackType))
			return 1;
		else if ("grade2".equals(trackType))
			return 2;
		else if ("grade3".equals(trackType))
			return 3;
		else if ("grade4".equals(trackType))
			return 4;
		else if ("grade5".equals(trackType))
			return 5;
		else if ("grade6".equals(trackType))
			return 6;
		else if ("grade7".equals(trackType))
			return 7;
		else if ("grade8".equals(trackType))
			return 8;
		else
			return 0;
	}

	public static int getSmoothnessValue(String smoothness) {
		if ("excellent".equals(smoothness))
			return 1;
		else if ("good".equals(smoothness))
			return 2;
		else if ("intermediate".equals(smoothness))
			return 3;
		else if ("bad".equals(smoothness))
			return 4;
		else if ("very_bad".equals(smoothness))
			return 5;
		else if ("horrible".equals(smoothness))
			return 6;
		else if ("very_horrible".equals(smoothness))
			return 7;
		else if ("impassable".equals(smoothness))
			return 8;
		else
			return 0;
	}
	
	public static int getSurfaceValue(String surface) {
		if ("paved".equals(surface))
			return 1;
		else if ("asphalt".equals(surface))
			return 2;
		else if ("cobblestone".equals(surface))
			return 3;
		else if ("cobblestone:flattened".equals(surface))
			return 4;
		else if ("concrete".equals(surface))
			return 5;
		else if ("concrete:lanes".equals(surface))
			return 6;
		else if ("concrete:plates".equals(surface))
			return 7;
		else if ("paving_stones".equals(surface))
			return 8;
		else
			return 0;
	}
}
