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
package heigit.ors.isochrones;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.exceptions.InternalServerException;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.GeomUtility;
import heigit.ors.util.UnitsConverter;
import heigit.ors.common.Pair;

public class IsochronesIntersection {
	private Geometry _geometry;
	private Envelope _envelope;
	private double _area = 0.0;
	private List<Pair<Integer, Integer>> _contourRefs;

	public IsochronesIntersection(Geometry geometry)
	{
		_geometry = geometry;
		_contourRefs = new ArrayList<Pair<Integer, Integer>>();
	}

	public List<Pair<Integer, Integer>> getContourRefs()
	{
		return _contourRefs;
	}

	public void addContourRefs(Pair<Integer, Integer> ref)
	{
		_contourRefs.add(ref);
	}

	public void addContourRefs(Collection<Pair<Integer, Integer>> refs)
	{
		_contourRefs.addAll(refs);
	}

	public Geometry getGeometry()
	{
		return _geometry;
	}

	public double getArea(String units) throws InternalServerException
	{
		double area = getArea(true);

		if (units != null)
		{
			switch(units)
			{
			case "m":
				return area;
			case "mi":
				return UnitsConverter.SqMetersToSqMiles(_area);
			case "km":
				return UnitsConverter.SqMetersToSqKilometers(_area); 
			}
		}

		return area;
	}

	public double getArea(Boolean inMeters) throws InternalServerException {
		if (_area == 0.0) {
			_area = FormatUtility.roundToDecimals(GeomUtility.getArea(_geometry, inMeters), 2);
		}

		return _area;
	}

	public boolean intersects(IsochronesIntersection other)
	{
		if (!getEnvelope().intersects(other.getEnvelope()))
			return false;

		return _geometry.intersects(other._geometry);
	}

	public Envelope getEnvelope()
	{
		if(_envelope == null)
			_envelope = _geometry.getEnvelopeInternal();

		return _envelope;
	}
}
