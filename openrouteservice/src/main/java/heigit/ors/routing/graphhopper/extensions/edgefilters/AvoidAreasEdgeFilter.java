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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import java.io.Serializable;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class AvoidAreasEdgeFilter implements EdgeFilter {

	private Envelope env; 
	private Polygon[] polys;
	private DefaultCoordinateSequence coordSequence;
	private GeometryFactory geomFactory = new GeometryFactory();
	
	private double eMinX = Double.MAX_VALUE;
	private double eMinY = Double.MAX_VALUE;
	private double eMaxX = Double.MIN_VALUE;
	private double eMaxY = Double.MIN_VALUE;
	
	/**
	 * Creates an edges filter which accepts both direction of the specified vehicle.
	 */
	public AvoidAreasEdgeFilter(Polygon[] polys)
	{
		this.polys = polys;

		if (polys != null && polys.length > 0)
		{
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			double maxY = Double.MIN_VALUE;

			for (int i = 0; i< polys.length; i++)
			{
				Polygon poly = polys[i];
				Envelope env = poly.getEnvelopeInternal();
				if (env.getMinX() < minX)
					minX = env.getMinX();
				if (env.getMinY() < minY)
					minY = env.getMinY();
				if (env.getMaxX() > maxX)
					maxX = env.getMaxX();
				if (env.getMaxY() > maxY)
					maxY = env.getMaxY();
			}

			env = new Envelope(minX, maxX, minY, maxY);

			coordSequence = new DefaultCoordinateSequence(new Coordinate[1], 1);
		}
	}

	@Override
	public final boolean accept(EdgeIteratorState iter ) {
		if (env == null)
			return true;

		boolean inEnv = false;
		//   PointList pl = iter.fetchWayGeometry(2); // does not work
		PointList pl = iter.fetchWayGeometry(3);
		int size = pl.getSize();

		eMinX = Double.MAX_VALUE;
		eMinY = Double.MAX_VALUE;
		eMaxX = Double.MIN_VALUE;
		eMaxY = Double.MIN_VALUE;

		for (int j = 0; j < pl.getSize(); j++)
		{
			double x = pl.getLon(j);
			double y = pl.getLat(j);
			if (env.contains(x, y))
			{
				inEnv = true;
				break;
			}

			if (x < eMinX)
				eMinX = x;
			if (y < eMinY)
				eMinY = y;
			if (x > eMaxX)
				eMaxX = x;
			if (y > eMaxY)
				eMaxY = y;
		}

		if (inEnv || !(eMinX > env.getMaxX() || eMaxX < env.getMinX() || eMinY > env.getMaxY() || eMaxY < env.getMinY()))
		{
			// We have to reset the coordinate sequence else for some reason the envelopes for the edge are wrong
			coordSequence = new DefaultCoordinateSequence(new Coordinate[1], 1);
			if (size >= 2)
			{
				// resize sequence if needed
				coordSequence.resize(size);

				for (int j = 0; j < size; j++)
				{
					double x = pl.getLon(j);
					double y = pl.getLat(j);
					Coordinate c =  coordSequence.getCoordinate(j);

					if (c == null)
					{
						c = new Coordinate(x, y);
						coordSequence.setCoordinate(j, c);
					}
					else
					{
						c.x = x;
						c.y = y;
					}
				}

				LineString ls = geomFactory.createLineString(coordSequence);

				for (int i = 0; i < polys.length; i++)
				{
					Polygon poly = polys[i];
					if (poly.contains(ls) || ls.crosses(poly))
					{
						return false;
					}
				}
			}
			else
			{
				return false;
			}
		}
			/*else
			{
				// Check if edge geomery intersects env.
				if (!(eMinX > env.getMaxX() || eMaxX < env.getMinX() || eMinY > env.getMaxY() || eMaxY < env.getMinY()))
				{
					
				}
			}	*/

		return true;
	}

	/**
	 * The CoordinateSequence implementation that Geometries use by default. In
	 * this implementation, Coordinates returned by #toArray and #get are live --
	 * parties that change them are actually changing the
	 * DefaultCoordinateSequence's underlying data.
	 *
	 * @version 1.7
	 */
	class DefaultCoordinateSequence implements CoordinateSequence, Serializable
	{
		//With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
		private static final long serialVersionUID = -915438501601840650L;
		private Coordinate[] coordinates;
		private int size;
		/**
		 * Constructs a DefaultCoordinateSequence based on the given array (the
		 * array is not copied).
		 *
		 * @param coordinates the coordinate array that will be referenced.
		 */
		public DefaultCoordinateSequence(Coordinate[] coordinates, int size) {
			if (coordinates == null)
			{
				throw new IllegalArgumentException("Null coordinate");
			}
			this.coordinates = coordinates;
			this.size = size;
		}

		/**
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getDimension()
		 */
		public int getDimension() { return 3; }
		/**
		 * Get the Coordinate with index i.
		 *
		 * @param i
		 * the index of the coordinate
		 * @return the requested Coordinate instance
		 */
		public Coordinate getCoordinate(int i) {
			return coordinates[i];
		}

		public void setCoordinate(int i, Coordinate c) {
			coordinates[i] = c;
		}

		/**
		 * Get a copy of the Coordinate with index i.
		 *
		 * @param i the index of the coordinate
		 * @return a copy of the requested Coordinate
		 */
		public Coordinate getCoordinateCopy(int i) {
			return new Coordinate(coordinates[i]);
		}
		/**
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
		 */
		public void getCoordinate(int index, Coordinate coord) {
			coord.x = coordinates[index].x;
			coord.y = coordinates[index].y;
		}
		/**
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
		 */
		public double getX(int index) {
			return coordinates[index].x;
		}
		/**
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getY(int)
		 */
		public double getY(int index) {
			return coordinates[index].y;
		}
		/**
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getOrdinate(int, int)
		 */
		public double getOrdinate(int index, int ordinateIndex)
		{
			switch (ordinateIndex) {
			case CoordinateSequence.X: return coordinates[index].x;
			case CoordinateSequence.Y: return coordinates[index].y;
			case CoordinateSequence.Z: return coordinates[index].z;
			}
			return Double.NaN;
		}
		/**
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#setOrdinate(int, int, double)
		 */
		public void setOrdinate(int index, int ordinateIndex, double value)
		{
			switch (ordinateIndex) {
			case CoordinateSequence.X: coordinates[index].x = value;
			case CoordinateSequence.Y: coordinates[index].y = value;
			case CoordinateSequence.Z: coordinates[index].z = value;
			}
		}
		/**
		 * Creates a deep copy of the Object
		 *
		 * @return The deep copy
		 */
		public Object clone() {
			Coordinate[] cloneCoordinates = new Coordinate[size()];
			for (int i = 0; i < coordinates.length; i++) {
				cloneCoordinates[i] = (Coordinate) coordinates[i].clone();
			}
			return new DefaultCoordinateSequence(cloneCoordinates, size);
		}

		/**
		 * Returns the size of the coordinate sequence
		 *
		 * @return the number of coordinates
		 */
		public int size() {
			return size;
		}

		public void resize(int size)
		{
			if (size > this.size)
			{
				coordinates = new Coordinate[size];
			}

			this.size = size;
		}

		/**
		 * This method exposes the internal Array of Coordinate Objects
		 *
		 * @return the Coordinate[] array.
		 */
		public Coordinate[] toCoordinateArray() {
			return coordinates;
		}
		public Envelope expandEnvelope(Envelope env)
		{
			for (int i = 0; i < coordinates.length; i++ ) {
				env.expandToInclude(coordinates[i]);
			}
			return env;
		}
		/**
		 * Returns the string Representation of the coordinate array
		 *
		 * @return The string
		 */
		public String toString() {
			if (coordinates.length > 0) {
				StringBuffer strBuf = new StringBuffer(17 * coordinates.length);
				strBuf.append('(');
				strBuf.append(coordinates[0]);
				for (int i = 1; i < coordinates.length; i++) {
					strBuf.append(", ");
					strBuf.append(coordinates[i]);
				}
				strBuf.append(')');
				return strBuf.toString();
			} else {
				return "()";
			}
		}
	}
}
