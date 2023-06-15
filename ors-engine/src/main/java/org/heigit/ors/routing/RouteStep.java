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
package org.heigit.ors.routing;

public class RouteStep {
	private double distance = 0.0;
	private double duration = 0.0;
	private String message;
	private int messageType = -1;
	private String instruction = "";
	private String name = null;
	private int exitNumber = -1;
	private int type;
	private int[] wayPoints;
    private RouteStepManeuver maneuver;
    private int[] roundaboutExitBearings;
    
	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}
	
	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getExitNumber() {
		return exitNumber;
	}

	public void setExitNumber(int exitNumber) {
		this.exitNumber = exitNumber;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String text) {
		instruction = text;
	}

	public int[] getWayPoints() {
		return wayPoints;
	}

	public void setWayPoints(int[] wayPoints) {
		this.wayPoints = wayPoints;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public RouteStepManeuver getManeuver() {
		return maneuver;
	}

	public void setManeuver(RouteStepManeuver maneuver) {
		this.maneuver = maneuver;
	}

	public int[] getRoundaboutExitBearings() {
		return roundaboutExitBearings;
	}

	public void setRoundaboutExitBearings(int[] roundaboutExitBearings) {
		this.roundaboutExitBearings = roundaboutExitBearings;
	}
}
