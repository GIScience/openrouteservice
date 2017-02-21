package heigit.ors.routing;

import heigit.ors.routing.parameters.ProfileParameters;

public class WheelchairParameters extends ProfileParameters {
	private double _maxIncline = 0.0;
	private double _maxSlopedCurb;
	private int _surfaceType;
	private int _trackType;
	private int _smoothnessType;

	public WheelchairParameters()
	{

	}

	public double getMaximumIncline() {
		return _maxIncline;
	}

	public void setMaximumIncline(double maxIncline) {
		_maxIncline = maxIncline;
	}

	public int getSurfaceType() {
		return _surfaceType;
	}

	public void setSurfaceType(int surfaceType) {
		_surfaceType = surfaceType;
	}

	public double getMaximumSlopedCurb() {
		return _maxSlopedCurb;
	}

	public void setMaximumSlopedCurb(double maxSlopedCurb) {
		_maxSlopedCurb = maxSlopedCurb;
	}

	public int getTrackType() {
		return _trackType;
	}

	public void setTrackType(int trackType) {
		_trackType = trackType;
	}

	public int getSmoothnessType() {
		return _smoothnessType;
	}

	public void setSmoothnessType(int smoothnessType) {
		_smoothnessType = smoothnessType;
	}
}
