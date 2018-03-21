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
package heigit.ors.routing.parameters;

public class WheelchairParameters extends ProfileParameters 
{
	private float _maxIncline = 0.0F;
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
}
