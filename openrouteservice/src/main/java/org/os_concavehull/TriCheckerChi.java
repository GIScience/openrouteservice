/** For concave hull construction based on Chi criterion
 * 
 * Original paper: Matt Duckham et al 2008 Efficient generation of simple polygons for characterizing the shape of a set of points in the plane
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
 ** 
 */
package org.os_concavehull;

//import org.locationtech.jts.geom.Coordinate;

import com.vividsolutions.jts.geom.Coordinate;

public class TriCheckerChi implements TriangleChecker {
	double length = Double.MAX_VALUE;
	Coordinate sp = new Coordinate();
	Coordinate ep = new Coordinate();
	
	public TriCheckerChi(double L) {
		length = L;
	}
	public double getLength() {
		return length;
	}
	public boolean removeable(Coordinate coordS, Coordinate coordE, Coordinate coordO) {
		sp.setCoordinate(coordS);
		ep.setCoordinate(coordE);
		double distance = sp.distance(ep);
		return sp.distance(ep) > length;
	}

}
