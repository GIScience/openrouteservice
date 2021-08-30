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
package org.heigit.ors.routing.parameters;

import java.util.List;

public class WheelchairParameters extends ProfileParameters {
	private float maxIncline = Float.MAX_VALUE * -1.0f;
	private float maxSlopedKerb = -1.0F;
	private int surfaceType;
	private int trackType;
	private int smoothnessType;
	private float minWidth = 0.0f;
	private boolean surfaceQualityKnown = false;
	private boolean allowUnsuitable = false;

	public float getMaximumIncline() {
		return maxIncline;
	}

	public void setMaximumIncline(float maxIncline) {
		this.maxIncline = maxIncline;
	}

	public int getSurfaceType() {
		return surfaceType;
	}

	public void setSurfaceType(int surfaceType) {
		this.surfaceType = surfaceType;
	}

	public float getMaximumSlopedKerb() {
		return maxSlopedKerb;
	}

	public void setMaximumSlopedKerb(float maxSlopedKerb) {
		this.maxSlopedKerb = maxSlopedKerb;
	}

	public int getTrackType() {
		return trackType;
	}

	public void setTrackType(int trackType) {
		this.trackType = trackType;
	}

	public int getSmoothnessType() {
		return smoothnessType;
	}

	public void setSmoothnessType(int smoothnessType) {
		this.smoothnessType = smoothnessType;
	}

	public float getMinimumWidth() { return minWidth; }

	public void setMinimumWidth(float width) { minWidth = width; }

	public boolean isRequireSurfaceQualityKnown() { return surfaceQualityKnown; }

	public void setSurfaceQualityKnown(boolean surfaceQualityKnown) { this.surfaceQualityKnown = surfaceQualityKnown; }

	public boolean allowUnsuitable() { return allowUnsuitable; }

	public void setAllowUnsuitable(boolean allowUnsuitable) { this.allowUnsuitable = allowUnsuitable; }

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
