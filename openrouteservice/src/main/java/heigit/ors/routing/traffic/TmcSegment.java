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
