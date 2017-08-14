
package heigit.ors.routing;

import com.vividsolutions.jts.geom.Coordinate;

public class RouteStepManeuver {
	private Coordinate _location;
	private int _bearingBefore;
	private int _bearingAfter;
	
	public RouteStepManeuver()
	{}

	public Coordinate getLocation() {
		return _location;
	}

	public void setLocation(Coordinate location) {
		_location = location;
	}

	public int getBearingBefore() {
		return _bearingBefore;
	}

	public void setBearingBefore(int value) {
		_bearingBefore = value;
	}
	
	public int getBearingAfter() {
		return _bearingAfter;
	}

	public void setBearingAfter(int value) {
		_bearingAfter = value;
	}
}
