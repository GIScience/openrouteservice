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
package heigit.ors.matrix;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.DistanceUnit;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.services.ServiceRequest;

public class MatrixRequest extends ServiceRequest
{
	private int _profileType = -1;
	private Coordinate[] _sources;
	private Coordinate[] _destinations;
	private int _metrics =  MatrixMetricsType.Duration;
	private String _weightingMethod; 
	private DistanceUnit _units = DistanceUnit.Meters;
	private boolean _resolveLocations = false;
	private boolean _flexibleMode = false;
	private String _algorithm;

	public MatrixRequest()
	{

	}

	public Coordinate[] getSources()
	{
		return _sources;
	}

	public void setSources(Coordinate[] sources)
	{
		_sources = sources;
	}

	public Coordinate[] getDestinations()
	{
		return _destinations;
	}    

	public void setDestinations(Coordinate[] destinations)
	{
		_destinations = destinations;
	}

	public int getMetrics() {
		return _metrics;
	}

	public void setMetrics(int metrics) 
	{
		_metrics = metrics;
	}

	public boolean getResolveLocations() 
	{
		return _resolveLocations;
	}

	public void setResolveLocations(boolean resolveLocations) 
	{
		_resolveLocations = resolveLocations;
	}

	public int getProfileType() {
		return _profileType;
	}

	public void setProfileType(int profile) {
		_profileType = profile;
	}

	public DistanceUnit getUnits() {
		return _units;
	}

	public void setUnits(DistanceUnit units) {
		_units = units;
	}

	public int getTotalNumberOfLocations()
	{
		return _destinations.length * _sources.length;
	}

	public String getWeightingMethod() {
		return _weightingMethod;
	}

	public void setWeightingMethod(String weighting) {
		_weightingMethod = weighting;
	}

	public boolean getFlexibleMode() {
		return _flexibleMode;
	}

	public void setFlexibleMode(boolean flexibleMode) {
		this._flexibleMode = flexibleMode;
	}

	public String getAlgorithm() {
		return _algorithm;
	}

	public void setAlgorithm(String _algorithm) {
		this._algorithm = _algorithm;
	}
}
