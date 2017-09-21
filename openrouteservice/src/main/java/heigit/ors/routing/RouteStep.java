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
package heigit.ors.routing;

public class RouteStep 
{
	private double _distance = 0.0;
	private double _duration = 0.0;
	private String _message;
	private int _messageType = -1;
	private String _instruction = "";
	private String _name = null;
	private int _exitNumber = -1;
	private int _type;
	private int[] _wayPoints;
    private RouteStepManeuver _maneuver;
    private int[] _roundaboutExitBearings;
    
	public double getDuration() {
		return _duration;
	}

	public void setDuration(double duration) {
		_duration = duration;
	}
	
	public double getDistance() {
		return _distance;
	}

	public void setDistance(double distance) {
		_distance = distance;
	}

	public String getMessage() {
		return _message;
	}

	public void setMessage(String message) {
		_message = message;
	}

	public int getExitNumber() {
		return _exitNumber;
	}

	public void setExitNumber(int exitNumber) {
		_exitNumber = exitNumber;
	}

	public String getInstruction() {
		return _instruction;
	}

	public void setInstruction(String text) {
		_instruction = text;
	}

	public int[] getWayPoints() {
		return _wayPoints;
	}

	public void setWayPoints(int[] wayPoints) {
		_wayPoints = wayPoints;
	}

	public int getType() {
		return _type;
	}

	public void setType(int type) {
		this._type = type;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public int getMessageType() {
		return _messageType;
	}

	public void setMessageType(int messageType) {
		_messageType = messageType;
	}

	public RouteStepManeuver getManeuver() {
		return _maneuver;
	}

	public void setManeuver(RouteStepManeuver maneuver) {
		_maneuver = maneuver;
	}

	public int[] getRoundaboutExitBearings() {
		return _roundaboutExitBearings;
	}

	public void setRoundaboutExitBearings(int[] roundaboutExitBearings) {
		_roundaboutExitBearings = roundaboutExitBearings;
	}
}
