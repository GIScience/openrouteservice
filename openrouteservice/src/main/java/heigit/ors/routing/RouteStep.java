/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
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
}
