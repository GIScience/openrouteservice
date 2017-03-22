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
