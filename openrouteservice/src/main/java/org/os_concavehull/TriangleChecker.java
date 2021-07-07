/** Interface for checkers to determine if a boundary edge should be removed 
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

public interface TriangleChecker {
	/** 
	 * @param coordS starting point of a boundary edge (CCW order)
	 * @param coordE ending point of a boundary edge
	 * @param coordO internal point of triangle s-e-o that is being examined
	 * @return true if this triangle can be dug according predefined criteria; false if not
	 */
	boolean removeable(Coordinate coordS, Coordinate coordE, Coordinate coordO);
}
