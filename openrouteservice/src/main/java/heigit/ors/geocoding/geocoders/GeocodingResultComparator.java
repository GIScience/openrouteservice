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
