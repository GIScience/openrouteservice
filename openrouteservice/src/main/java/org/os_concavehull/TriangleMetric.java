/** Interface for controlling edge examination order during concave hull construction.
 * 
 * Author: Sheng Zhou (Sheng.Zhou@os.uk)
 * 
 * version 0.4
 * 
 * Date: 2019-01-31
 * 
 * Copyright (C) 2019 Ordnance Survey
 *
 * Licensed under the Open Government Licence v3.0 (the "License");
 * 
 * you may not use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 *
 *     http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
		
package org.os_concavehull;

//import org.locationtech.jts.geom.Coordinate;

import com.vividsolutions.jts.geom.Coordinate;

public interface TriangleMetric {
	/** return a metric to be used by concave hull constructor to determine the order of edge (coordS-coordE) examination. Edge with larger metric value will be checked earlier.
	 * @param coordS first vertex on the "outer" edge of the triangle (i.e. facing outwards of current hull (in CCW order) 
	 * @param coordE second vertex on the "outer" edge of the triangle (i.e. facing outwards of current hull (in CCW order)
	 * @param coordO third vertex of the triangle, which is in the interior of current hull
	 * @return
	 */
	double compMetric(Coordinate coordS, Coordinate coordE, Coordinate coordO);
}
