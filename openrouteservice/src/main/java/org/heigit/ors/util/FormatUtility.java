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
import org.heigit.ors.common.DistanceUnit;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtility {
	private FormatUtility() {}

	private static final ThreadLocal< NumberFormat > nfCoordRound = ThreadLocal.withInitial(() -> {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setMaximumFractionDigits(7);
		nf.setMinimumFractionDigits(7);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		return nf;
	});

	/**
	 * @param coord Coordinate
	 * @return result String
	 */
	public static String formatCoordinate(Coordinate coord) {
		return nfCoordRound.get().format(coord.x) + " " + nfCoordRound.get().format(coord.y);
	}

	public static double roundToDecimals(double d, int c) {
		double denom = Math.pow(10 , c);
	    return Math.round (d * denom) / denom;
	}

	public static int getUnitDecimals(DistanceUnit unit) {
		if (unit == DistanceUnit.METERS)
			return 1;
		else if (unit == DistanceUnit.KILOMETERS || unit == DistanceUnit.MILES)
			return 3;

		return 1;
	}

	public static double roundToDecimalsForUnits(double d, DistanceUnit unit) {
		return roundToDecimals(d, getUnitDecimals(unit));
	}

	public static void unload(){
		nfCoordRound.remove();
	}
}
