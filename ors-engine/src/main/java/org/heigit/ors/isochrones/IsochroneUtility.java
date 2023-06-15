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
package org.heigit.ors.isochrones;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import org.heigit.ors.common.Pair;

public class IsochroneUtility {
	private IsochroneUtility() {}

	public static List<IsochronesIntersection> computeIntersections(IsochroneMapCollection isochroneMaps) {
		List<IsochronesIntersection> result = new ArrayList<>();
		if (isochroneMaps.size() == 1)
			return result;

		int im = 0;
		for (IsochroneMap isoMap : isochroneMaps.getIsochroneMaps()) {
			int ii = 0;
			for (Isochrone isoLine : isoMap.getIsochrones())  {
				List<IsochronesIntersection> isoIntersection = computeIntersection(isoLine, ii, isoMap, im, isochroneMaps);
				if (!isoIntersection.isEmpty()) {
					result.addAll(isoIntersection);
				}
				ii++;
			}
			im++;
		}

		// Find intersections between IsochronesIntersection objects
		if (result.size() > 1) {
			List<IsochronesIntersection> isoIntersections = new ArrayList<>();
			int i = 0;
			for (IsochronesIntersection isoIntersection : result) {
				List<IsochronesIntersection> overlaps = computeIntersection(isoIntersection, i, result);
				if (!overlaps.isEmpty()) {
					isoIntersections.addAll(overlaps);
				}
                i++;
			}
			if (!isoIntersections.isEmpty())
				result.addAll(isoIntersections);
		}
		return result;
	}

	private static List<IsochronesIntersection> computeIntersection(IsochronesIntersection isoIntersection, Integer intersectionIndex, List<IsochronesIntersection> intersections) {
		List<IsochronesIntersection> result = new ArrayList<>();
		for  (int i = intersectionIndex + 1 ; i < intersections.size(); i++) {
			IsochronesIntersection isoIntersection2 = intersections.get(i);
			if (isoIntersection.intersects(isoIntersection2)) {
				Geometry geomIntersection = isoIntersection.getGeometry().intersection(isoIntersection2.getGeometry());
				if (geomIntersection != null) {
					IsochronesIntersection isoIntersectionNew = new IsochronesIntersection(geomIntersection);
					isoIntersectionNew.addContourRefs(isoIntersection.getContourRefs());
					isoIntersectionNew.addContourRefs(isoIntersection2.getContourRefs());
				}
			}
		}
		return result;
	}

	private static List<IsochronesIntersection> computeIntersection(Isochrone isoLine, Integer isoIndex, IsochroneMap isoMap, Integer isoMapIndex, IsochroneMapCollection isochroneMaps) {
		List<IsochronesIntersection> result = new ArrayList<>();
		Envelope isoEnvelope = isoLine.getEnvelope();
		Geometry isoGeometry = isoLine.getGeometry();
		for (int im = isoMapIndex + 1; im < isochroneMaps.size(); im++) {
			IsochroneMap isoMap2 =  isochroneMaps.getIsochrone(im);
			if (!Objects.equals(isoMap2, isoMap) && isoMap2.getEnvelope().intersects(isoEnvelope)) {
				int ii = 0;
				for (Isochrone isoLine2 : isoMap2.getIsochrones())  {
					if (isoEnvelope.intersects(isoLine2.getEnvelope())) {
						Geometry geomIntersection =  isoGeometry.intersection(isoLine2.getGeometry());
						if (geomIntersection != null && !geomIntersection.isEmpty()) {
							IsochronesIntersection isoIntersection = new IsochronesIntersection(geomIntersection);
							isoIntersection.addContourRefs(new Pair<>(isoMapIndex, isoIndex));
							isoIntersection.addContourRefs(new Pair<>(im, ii));
							result.add(isoIntersection);
						}
					}
					ii++;
				}
			}
		}
		return result;
	}
}
