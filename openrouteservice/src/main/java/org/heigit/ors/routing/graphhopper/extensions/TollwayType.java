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
package org.heigit.ors.routing.graphhopper.extensions;

public class TollwayType {

	public static final int NONE = 0;

	// https://en.wikipedia.org/wiki/Vehicle_category

	public static final int M1 = 1;
	public static final int M2 = 2;
	public static final int M3 = 4;
	public static final int M = M1 | M2 | M3;

	public static final int N1 = 8;
	public static final int N2 = 16;
	public static final int N3 = 32;

	public static final int N = N1 | N2 | N3;

	public static final int GENERAL = M | N;

	// OSM classification
	public static final int MOTORCAR = M1;
	public static final int GOODS = N1;
	public static final int HGV = N2 | N3;

	private TollwayType() {}

    public static boolean isSet(int flag, int value) {
        return (flag & value) == flag;
    }

	public static boolean isType(int flag, int value) {
		return (flag & value) != 0;
	}

	public static boolean isMType(int value) {
		return isType(M, value);
	}

	public static boolean isNType(int value) {
		return isType(N, value);
	}

}
