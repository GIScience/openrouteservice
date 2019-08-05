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
package heigit.ors.matrix;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.DistanceUnit;
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

	public boolean isValid(){
		return !(_sources == null && _destinations == null);
	}
}
