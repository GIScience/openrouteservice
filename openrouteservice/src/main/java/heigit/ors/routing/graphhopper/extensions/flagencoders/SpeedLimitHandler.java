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
package heigit.ors.routing.graphhopper.extensions.flagencoders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.reader.ReaderWay;

import heigit.ors.util.FileUtility;

public class SpeedLimitHandler {
	private static final Logger LOGGER = Logger.getLogger(SpeedLimitHandler.class.getName());
	
	private Map<String, Integer> _defaultSpeeds = new HashMap<String, Integer>();
	private Map<String, Integer> _surfaceSpeeds = new HashMap<String, Integer>();
	private Map<String, Integer> _trackTypeSpeeds = new HashMap<String, Integer>();
	private Map<String, Integer> _countryMaxSpeeds = new HashMap<String, Integer>();
	
	public SpeedLimitHandler(String encoderName, Map<String, Integer> defaultSpeeds, Map<String, Integer> surfaceSpeeds, Map<String, Integer> trackTypeSpeeds)
	{
		_defaultSpeeds.putAll(defaultSpeeds);
		_surfaceSpeeds.putAll(surfaceSpeeds);
		_trackTypeSpeeds.putAll(trackTypeSpeeds);
		
		Path path = Paths.get(FileUtility.getResourcesPath().toString(), "services", "routing", "speed_limits", encoderName + ".json");
		
		if (Files.exists(path))
		{
			try {
				JSONObject json = new JSONObject(FileUtility.readFile(path.toString()));
				
				readSpeedValues(json, "default", _defaultSpeeds);
				readSpeedValues(json, "max_speeds", _countryMaxSpeeds);
				readSpeedValues(json, "surface", _surfaceSpeeds);
				readSpeedValues(json, "tracktype", _trackTypeSpeeds);
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
	}
	
	private void readSpeedValues(JSONObject json, String keyName,  Map<String, Integer> speeds)
	{
		if (json.has(keyName))
		{
			JSONObject jLimits = json.getJSONObject(keyName);
			JSONArray jKeys = jLimits.names();
			
			for(int i = 0; i < jKeys.length(); i++)
			{  
				String key = jKeys.getString(i);
				speeds.put(key.toLowerCase(), jLimits.getInt(key));
			}
		}
	}
	
    public Integer getMaxSpeed(ReaderWay way)
    {
    	// check if maxspeed is explicitly given
    	if (way.hasTag("maxspeed"))
    		return -1;
    	
    	String key = way.getTag("zone:maxspeed");
    	if (key == null)
    		key = way.getTag("zone:traffic");
    	
    	if (key == null)
    		return -1;
    	
    	Integer res = _countryMaxSpeeds.get(key.toLowerCase());
    	
    	return res == null ? -1 : res;
    }
    
    public Integer getTrackTypeSpeed(String tracktype)
    {
    	Integer res = _trackTypeSpeeds.get(tracktype);
    	return res == null ? -1 : res;
    }
    
    public Integer getSurfaceSpeed(String surface)
    {
    	Integer res = _surfaceSpeeds.get(surface);
    	return res == null ? -1 : res;
    }
    
    public Integer getSpeed(String highway)
    {
    	return _defaultSpeeds.get(highway);
    }
    
    public boolean hasSpeedValue(String highway)
    {
    	return _defaultSpeeds.containsKey(highway);
    }
}
