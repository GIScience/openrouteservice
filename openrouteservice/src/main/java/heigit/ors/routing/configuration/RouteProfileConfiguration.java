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
package heigit.ors.routing.configuration;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.typesafe.config.Config;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.routing.RoutingProfileType;

public class RouteProfileConfiguration
{
	private String _name = "";
	private Boolean _enabled = true;
	private String _profiles; // comma separated
	private String _graphPath;
	private Map<String, Map<String, String>> _extStorages;
	private Map<String, Map<String, String>> _graphBuilders;
	private Double _maximumDistance = 0.0;
	private Double _maximumDistanceDynamicWeights = 0.0;
	private Double _maximumDistanceAvoidAreas = 0.0;
	private Integer _maximumWayPoints = 0;
	private boolean _useTrafficInformation = false;
	private boolean _instructions = true;
	private boolean _optimize = false;
	
	private int _encoderFlagsSize = 4;
	private String _encoderOptions = null;
	
	private Config _preparationOpts;
	private Config _executionOpts;
	
	private String _elevationProvider = null; 
	private String _elevationCachePath = null;
	private String _elevationDataAccess = "MMAP";
	private boolean _elevationCacheClear = true;
	private int _maximumSnappingRadius;
	
	private Envelope _extent;
	private boolean _hasMaximumSnappingRadius = false;

	public RouteProfileConfiguration()
	{
		_extStorages = new HashMap<String, Map<String, String>>();
		_graphBuilders = new HashMap<String, Map<String, String>>();
	}
	
	public Integer[] getProfilesTypes()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		String[] elements = _profiles.split("\\s*,\\s*");
		
		for (int i = 0; i< elements.length; i++)
		{
			int profileType = (int)RoutingProfileType.getFromString(elements[i]);
			
			if (profileType != (int)RoutingProfileType.UNKNOWN)
			{
				list.add(profileType);
			}
		}
		
		return (Integer[])list.toArray(new Integer[list.size()]);
	}
	
	public RouteProfileConfiguration clone()
	{
		RouteProfileConfiguration rpc = new RouteProfileConfiguration();
		
		rpc._name = this._name;
		rpc._enabled = this._enabled;
		rpc._profiles = this._profiles;
		rpc._graphPath = this._graphPath;
			
		rpc._maximumDistance = this._maximumDistance;
		rpc._maximumDistanceDynamicWeights = this._maximumDistanceDynamicWeights;
		rpc._maximumDistanceAvoidAreas = this._maximumDistanceAvoidAreas;
		rpc._maximumWayPoints = this._maximumWayPoints;
		rpc._useTrafficInformation = this._useTrafficInformation;
		rpc._optimize = this._optimize;
		rpc._instructions = this._instructions;
		
		rpc._encoderFlagsSize = this._encoderFlagsSize;
		rpc._encoderOptions = this._encoderOptions;
		rpc._preparationOpts = this._preparationOpts;
		rpc._executionOpts = this._executionOpts;

		rpc._extStorages = this._extStorages;
		rpc._graphBuilders = this._graphBuilders;

		rpc._elevationCachePath = this._elevationCachePath;
		rpc._elevationCacheClear = this._elevationCacheClear;
		rpc._elevationProvider = this._elevationProvider;
		rpc._elevationDataAccess = this._elevationDataAccess;

		rpc._maximumSnappingRadius = this._maximumSnappingRadius;

		rpc._extent = this._extent;
		
		return rpc;
	}
	
	public void setName(String value)
	{
		_name = value; 
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setEnabled(Boolean value)
	{
		_enabled = value; 
	}
	
	public Boolean getEnabled()
	{
		return _enabled;
	}
	
	public void setProfiles(String value)
	{
		_profiles = value; 
	}
	
	public String getProfiles()
	{
		return _profiles;
	}
	
	public void setGraphPath(String value)
	{
		_graphPath = value; 
	}
	
	public String getGraphPath()
	{
		return _graphPath;
	}
	
	public void setExtStorages(Map<String, Map<String, String>> value)
	{
		_extStorages = value; 
	}
	
	public Map<String, Map<String, String>> getExtStorages()
	{
		return _extStorages;
	}
	
	public void setGraphBuilders(Map<String, Map<String, String>> value)
	{
		_graphBuilders = value; 
	}
	
	public Map<String, Map<String, String>> getGraphBuilders()
	{
		return _graphBuilders;
	}
	
	public void setInstructions(Boolean value)
	{
		_instructions = value; 
	}
	
	public Boolean getInstructions()
	{
		return _instructions;
	}
	
	public void setMaximumDistance(Double value)
	{
		_maximumDistance = value; 
	}
	
	public Double getMaximumDistance()
	{
		return _maximumDistance;
	}
	
	public void setMaximumDistanceDynamicWeights(Double value)
	{
		_maximumDistanceDynamicWeights = value;
	}
	
	public Double getMaximumDistanceDynamicWeights()
	{
		return _maximumDistanceDynamicWeights;
	}

	public void setMaximumDistanceAvoidAreas(Double value)
	{
		_maximumDistanceAvoidAreas = value;
	}

	public Double getMaximumDistanceAvoidAreas()
	{
		return _maximumDistanceAvoidAreas;
	}
	
	public void setMaximumWayPoints(Integer value)
	{
		_maximumWayPoints = value; 
	}
	
	public Integer getMaximumWayPoints()
	{
		return _maximumWayPoints;
	}
	
	public void setUseTrafficInformation(Boolean value)
	{
		_useTrafficInformation = value; 
	}
	
	public Boolean getUseTrafficInformation()
	{
		return _useTrafficInformation;
	}
	
	public void setEncoderFlagsSize(Integer value)
	{
		_encoderFlagsSize = value; 
	}
	
	public Integer getEncoderFlagsSize()
	{
		return _encoderFlagsSize;
	}
	
	public void setEncoderOptions(String value)
	{
		_encoderOptions = value; 
	}
	
	public String getEncoderOptions()
	{
		return _encoderOptions;
	}
	
	public void setExtent(Envelope value)
	{
		_extent = value; 
	}
	
	public Envelope getExtent()
	{
		return _extent;
	}
	
	public void setElevationProvider(String value)
	{
		_elevationProvider = value; 
	}
	
	public String getElevationProvider()
	{
		return _elevationProvider;
	}
	
	public void setElevationCachePath(String value)
	{
		_elevationCachePath = value; 
	}
	
	public String getElevationCachePath()
	{
		return _elevationCachePath;
	}
	
	public void setElevationDataAccess(String value)
	{
		_elevationDataAccess = value; 
	}
	
	public String getElevationDataAccess()
	{
		return _elevationDataAccess;
	}
	
	public void setElevationCacheClear(Boolean value)
	{
		_elevationCacheClear = value; 
	}
	
	public Boolean getElevationCacheClear()
	{
		return _elevationCacheClear;
	}

	public Config getPreparationOpts() {
		return _preparationOpts;
	}

	public void setPreparationOpts(Config preparationOpts) {
		_preparationOpts = preparationOpts;
	}

	public Config getExecutionOpts() {
		return _executionOpts;
	}

	public void setExecutionOpts(Config executionOpts) {
		this._executionOpts = executionOpts;
	}

	public boolean getOptimize() {
		return _optimize;
	}

	public void setOptimize(boolean optimize) {
		this._optimize = optimize;
	}

	public boolean hasMaximumSnappingRadius() {
		return _hasMaximumSnappingRadius;
	}

	public int getMaximumSnappingRadius() {
		return _maximumSnappingRadius;
	}

	public void setMaximumSnappingRadius(int _maximumSnappingRadius) {
		this._maximumSnappingRadius = _maximumSnappingRadius;
		this._hasMaximumSnappingRadius = true;
	}
}