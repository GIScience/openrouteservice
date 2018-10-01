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
/* Class descriptor: array creation of retrieved osm way objects */

package heigit.ors.routing.traffic;

import com.vividsolutions.jts.geom.Geometry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

;

public class TmcSegment implements Serializable {
	private static final long serialVersionUID = 7526472295622776147L;

	private long id = -1L;
	private String roadnumber = null;
	private Integer from = -1;
	private Integer to = -1;
	private List<Integer> edgeIDs;
	private Geometry geom = null;
	private Integer direction;
	private double distance;

	public TmcSegment(long id, String roadnumber, Integer from, Integer to, Integer direction, double distance, Geometry geom, List<Integer> edgeIDs) {
		this.id = id;
		this.roadnumber = roadnumber;
		this.from = from;
		this.to = to;
		this.edgeIDs = edgeIDs == null ? new ArrayList<Integer>() : edgeIDs;
		this.geom = geom;
		this.direction = direction;
		this.distance = distance;
	}

	public String toString() {
		return "Segment ID: " + this.id + " Roadnumber: " + this.roadnumber + " - From: " + this.from + " - To: "
				+ this.to + " \n Edge IDs:" + this.edgeIDs + " \n Geometry: " + this.geom;
	}

	public Integer getFrom() {
		return this.from;
	}

	public Integer getTo() {
		return this.to;
	}
	
	public Integer getDirection()
	{
		return this.direction;
	}
	
	public double getDistance()
	{
		return this.distance;
	}

	public List<Integer> getEdgeIDs() {
		return this.edgeIDs;
	}

	public Geometry getGeometry() {
		return this.geom;
	}

	public String getRoadnumber() {
		return this.roadnumber;
	}

	public long getId() {
		return this.id;
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		aInputStream.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.defaultWriteObject();
	}
}
