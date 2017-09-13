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

public class RouteSegmentItem {
	private int _from;
	private int _to;
	private int _value;
	private double _distance;

	public RouteSegmentItem(int from, int to, int value, double distance)
	{
		_from = from;
		_to = to;
		_value  = value;
		_distance = distance;
	}

	public int getFrom() {
		return _from;
	}

	public void setFrom(int from) {
		_from = from;
	}
	
	public int getTo() {
		return _to;
	}
	
	public void setTo(int to) {
		_to = to;
	}

	public int getValue() {
		return _value;
	}

	public void setValue(int value) {
		_value = value;
	}	
	
	public double getDistance()
	{
		return _distance;
	}
	
	public void setDistance(double value)
	{
		_distance = value;
	}
}
