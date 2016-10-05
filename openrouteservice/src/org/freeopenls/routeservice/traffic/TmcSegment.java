/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors:  P. Neis, E. Steiger, M. Rylov 

/* Class descriptor: array creation of retrieved osm way objects */

package org.freeopenls.routeservice.traffic;

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
	private long from = -1;
	private long to = -1;
	private List<Integer> edgeIDs;
	private Geometry geom = null;
	private Integer direction;
	private double distance;

	public TmcSegment(long id, String roadnumber, Long from, Long to, Integer direction, double distance, Geometry geom, List<Integer> edgeIDs) {
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

	public Long getFrom() {
		return this.from;
	}

	public Long getTo() {
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
