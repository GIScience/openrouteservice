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
package heigit.ors.routing.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WheelchairParameters extends ProfileParameters
{
	private float _maxIncline = Float.MAX_VALUE * -1.0f;
	private float _maxSlopedKerb = -1.0F;
	private int _surfaceType;
	private int _trackType;
	private int _smoothnessType;
	private float _minWidth = 0.0f;

	public WheelchairParameters()
	{

	}

	public float getMaximumIncline() {
		return _maxIncline;
	}

	public void setMaximumIncline(float maxIncline) {
		_maxIncline = maxIncline;
	}

	public int getSurfaceType() {
		return _surfaceType;
	}

	public void setSurfaceType(int surfaceType) {
		_surfaceType = surfaceType;
	}

	public float getMaximumSlopedKerb() {
		return _maxSlopedKerb;
	}

	public void setMaximumSlopedKerb(float maxSlopedKerb) {
		_maxSlopedKerb = maxSlopedKerb;
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

	public float getMinimumWidth() { return _minWidth; }

	public void setMinimumWidth(float width) { _minWidth = width; }

	@Override
	public List<String> getValidRestrictions() {
		List<String> valid = super.getValidRestrictions();

		valid.add("surface_type");
		valid.add("track_type");
		valid.add("smoothness_type");
		valid.add("maximum_sloped_kerb");
		valid.add("maximum_incline");
		valid.add("minimum_width");

		return valid;
	}
}
