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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import heigit.ors.util.FormatUtility;
import heigit.ors.util.GeomUtility;
import heigit.ors.util.UnitsConverter;

public class Isochrone {
	private Geometry geometry;
	private double value;
	private double area = 0.0;
	private double maxRadius;
	private Envelope envelope;

	public Isochrone(Geometry geometry, double value, double maxRadius) {
		this.geometry = geometry;
		this.value = value;
		this.maxRadius = maxRadius;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public double getValue() {
		return value;
	}

	public double getMaxRadius(String units)
	{
		if (units != null)
		{
			switch(units)
			{
			case "m":
				return maxRadius;
			case "mi":
				return UnitsConverter.SqMetersToSqMiles(maxRadius);
			case "km":
				return UnitsConverter.SqMetersToSqKilometers(maxRadius); 
			}
		}

		return maxRadius;
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
				return UnitsConverter.SqMetersToSqMiles(area);
			case "km":
				return UnitsConverter.SqMetersToSqKilometers(area); 
			}
		}

		return area;
	}

	public double getArea(Boolean inMeters) throws Exception {
		if (area == 0.0) {
			area = FormatUtility.roundToDecimals(GeomUtility.getArea(geometry, inMeters), 2);
		}

		return area;
	}

	public Envelope getEnvelope() {
		if (envelope == null)
			envelope = geometry.getEnvelopeInternal();

		return envelope;
	}
}
