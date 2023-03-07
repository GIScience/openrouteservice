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
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.GeomUtility;
import org.heigit.ors.util.UnitsConverter;
import org.heigit.ors.common.Pair;

public class IsochronesIntersection {
	private final Geometry geometry;
	private Envelope envelope;
	private double area = 0.0;
	private final List<Pair<Integer, Integer>> contourRefs;

	public IsochronesIntersection(Geometry geometry) {
		this.geometry = geometry;
		contourRefs = new ArrayList<>();
	}

	public List<Pair<Integer, Integer>> getContourRefs()
	{
		return contourRefs;
	}

	public void addContourRefs(Pair<Integer, Integer> ref)
	{
		contourRefs.add(ref);
	}

	public void addContourRefs(Collection<Pair<Integer, Integer>> refs)
	{
		contourRefs.addAll(refs);
	}

	public Geometry getGeometry()
	{
		return geometry;
	}

	public double getArea(String units) throws InternalServerException {
		if (area == 0.0) {
			area = FormatUtility.roundToDecimals(GeomUtility.getArea(geometry, true), 2);
		}
		if (units == null)
			units = "m";
		switch(units) {
			default:
			case "m":
				return area;
			case "mi":
				return UnitsConverter.sqMetersToSqMiles(this.area);
			case "km":
				return UnitsConverter.sqMetersToSqKilometers(this.area);
		}
	}

	public boolean intersects(IsochronesIntersection other) {
		if (!getEnvelope().intersects(other.getEnvelope()))
			return false;

		return geometry.intersects(other.geometry);
	}

	public Envelope getEnvelope() {
		if(envelope == null)
			envelope = geometry.getEnvelopeInternal();

		return envelope;
	}
}
