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
package heigit.ors.isochrones;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

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

	public double getArea(String units) throws Exception
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

	public double getArea(Boolean inMeters) throws Exception {
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
