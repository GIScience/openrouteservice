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
