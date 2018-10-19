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

import java.util.Comparator;

import com.graphhopper.util.Helper;

public class GeocodingResultComparator implements Comparator<GeocodingResult> {
	@Override
	public int compare(GeocodingResult gr1, GeocodingResult gr2) {
		if (gr1 != null && gr2 != null)
		{
			// compare results with the same accuracy value
			if (gr1.confidence == gr2.confidence)
			{
				boolean gr1HasHouseNumber = !Helper.isEmpty(gr1.houseNumber) && !Helper.isEmpty(gr1.street);
				boolean gr2HasHouseNumber = !Helper.isEmpty(gr2.houseNumber) && !Helper.isEmpty(gr2.street);

				if (gr1HasHouseNumber != gr2HasHouseNumber)
				{
					if (gr1HasHouseNumber && !gr2HasHouseNumber)
						return -1;
					else if (!gr1HasHouseNumber && gr2HasHouseNumber)
						return 1;
				}
			}
			else
				return Float.compare(gr2.confidence, gr1.confidence);
		}
		
		return 0;
	}
}
