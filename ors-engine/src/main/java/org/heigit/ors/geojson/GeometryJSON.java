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
package org.heigit.ors.geojson;

import org.geotools.geometry.jts.coordinatesequence.CoordinateSequences;
import org.heigit.ors.util.FormatUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.*;

@SuppressWarnings("unchecked")
public class GeometryJSON {

	private static final int COORDINATE_PRECISION = 6;
	private static final GeometryFactory factory = new GeometryFactory();

	private GeometryJSON() {}

	public static org.json.simple.JSONArray toJSON(Polygon poly) {
		org.json.simple.JSONArray coords = new org.json.simple.JSONArray();

		LineString shell = poly.getExteriorRing();

		boolean inverse = shell.getNumPoints() > 1 && !CoordinateSequences.isCCW(shell.getCoordinateSequence());
		coords.add(toJSON(shell, inverse));

		if (poly.getNumInteriorRing() > 0) {
			int nRings = poly.getNumInteriorRing();

			for (int j = 0; j < nRings; ++j) {
				LineString ring = poly.getInteriorRingN(j);
				inverse = ring.getNumPoints() > 1 && CoordinateSequences.isCCW(ring.getCoordinateSequence());
				coords.add(toJSON(ring, inverse));
			}
		}

		return coords;
	}

	private static org.json.simple.JSONArray toJSON(LineString line, boolean inverseSeq) {
		// "coordinates": [ [100.0, 0.0], [101.0, 1.0] ]
		int size = line.getNumPoints();

		org.json.simple.JSONArray arrCoords = new org.json.simple.JSONArray();

		CoordinateSequence seq = line.getCoordinateSequence();

		for (int i = 0; i < size; ++i)  {
			Coordinate coord = seq.getCoordinate(inverseSeq ? size - i - 1: i);
			arrCoords.add(toJSON(coord));
		}

		return arrCoords;
	}

	private static org.json.simple.JSONArray toJSON(Point point) {
		return toJSON(point.getCoordinate());
	}

	public static org.json.simple.JSONArray toJSON(Coordinate c) {
		org.json.simple.JSONArray arrCoords =  new org.json.simple.JSONArray();
		arrCoords.add(FormatUtility.roundToDecimals(c.x, COORDINATE_PRECISION));
		arrCoords.add(FormatUtility.roundToDecimals(c.y, COORDINATE_PRECISION));

		return arrCoords;
	}

	public static org.json.simple.JSONArray toJSON(Coordinate[] coords, boolean includeElevation) {
		org.json.simple.JSONArray arrCoords =  new org.json.simple.JSONArray();
		for (Coordinate c : coords) {
			org.json.simple.JSONArray coord = new org.json.simple.JSONArray();
			coord.add(FormatUtility.roundToDecimals(c.x, COORDINATE_PRECISION));
			coord.add(FormatUtility.roundToDecimals(c.y, COORDINATE_PRECISION));
			if (includeElevation)
				coord.add(FormatUtility.roundToDecimals(c.z, 1));
			arrCoords.add(coord);
		}
		return arrCoords;
	}

	public static Geometry parse(JSONObject json) throws Exception {
		if (!json.has("type"))
			throw new Exception("type element is missing.");

		if (!json.has("coordinates"))
			throw new Exception("coordinates element is missing.");

		String type = json.getString("type");
		JSONArray arrCoords = json.getJSONArray("coordinates");
		switch(type) {
			case "Point":
				return readPoint(arrCoords);
			case "MultiPoint":
				return readMultiPoint(arrCoords);
			case "LineString":
				return readLineString(arrCoords);
			case "MultiLineString":
				return readMultiLineString(arrCoords);
			case "Polygon":
				return readPolygon(arrCoords);
			case "MultiPolygon":
				return readMultiPolygon(arrCoords);
			default:
				throw new Exception("invalid type: " + type);
		}
	}

	private static Point readPoint(JSONArray value) {
		Coordinate c = new Coordinate(value.getDouble(0), value.getDouble(1));
		return factory.createPoint(c);
	}

	private static MultiPoint readMultiPoint(JSONArray value) {
		return factory.createMultiPoint(readCoordinates(value));
	}

	private static LineString readLineString(JSONArray value) {
		return factory.createLineString(readCoordinates(value));
	}

	private static MultiLineString readMultiLineString(JSONArray value) {
		int n = value.length();
		LineString[] lineStrings = new LineString[n];

		for (int i = 0; i < n; i++) {
			JSONArray arrLineString = (JSONArray) value.get(i);
			lineStrings[i] = readLineString(arrLineString);
		}

		return factory.createMultiLineString(lineStrings);
	}

	private static MultiPolygon readMultiPolygon(JSONArray value) {
		int n = value.length();
		Polygon[] polys = new Polygon[n];

		for (int i = 0; i < n; i++) {
			JSONArray arrPoly = (JSONArray) value.get(i);
			polys[i] = readPolygon(arrPoly);
		}

		return factory.createMultiPolygon(polys);
	}

	private static Polygon readPolygon(JSONArray value) {
		int n = value.length();
		
		LinearRing shell = null;
		LinearRing[] holes = new LinearRing[n-1];

		for (int i = 0; i < n; i++) {
			JSONArray arrLineString = (JSONArray) value.get(i);
			if (i == 0)
				shell = factory.createLinearRing(readCoordinates(arrLineString));
			else
				holes[i-1] = factory.createLinearRing(readCoordinates(arrLineString));
		}

		if (holes.length == 0)
			return factory.createPolygon(shell);
		else
			return factory.createPolygon(shell, holes);
	}

	private static Coordinate[] readCoordinates(JSONArray value) {
		int n = value.length();

		Coordinate[] coords = new Coordinate[n];

		for (int i = 0; i < n; i++) {
			JSONArray arrCoord = value.getJSONArray(i);
			coords[i] = new Coordinate(arrCoord.getDouble(0), arrCoord.getDouble(1));
		}

		return coords;
	}
}
