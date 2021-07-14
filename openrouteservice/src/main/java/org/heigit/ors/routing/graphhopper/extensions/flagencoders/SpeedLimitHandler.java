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
package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderWay;
import org.apache.log4j.Logger;
import org.heigit.ors.util.FileUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SpeedLimitHandler {
	private static final Logger LOGGER = Logger.getLogger(SpeedLimitHandler.class.getName());
	
	private final Map<String, Integer> defaultSpeeds = new HashMap<>();
	private final Map<String, Integer> surfaceSpeeds = new HashMap<>();
	private final Map<String, Integer> trackTypeSpeeds = new HashMap<>();
	private final Map<String, Integer> countryMaxSpeeds = new HashMap<>();
	
	public SpeedLimitHandler(String encoderName, Map<String, Integer> defaultSpeeds, Map<String, Integer> surfaceSpeeds, Map<String, Integer> trackTypeSpeeds) {
		this.defaultSpeeds.putAll(defaultSpeeds);
		this.surfaceSpeeds.putAll(surfaceSpeeds);
		this.trackTypeSpeeds.putAll(trackTypeSpeeds);

		encoderName = FlagEncoderNames.getBaseName(encoderName);
		
		Path path = Paths.get(FileUtility.getResourcesPath().toString(), "services", "routing", "speed_limits", encoderName + ".json");

		try {
			JSONObject json = new JSONObject(FileUtility.readFile(path.toString()));

			readSpeedValues(json, "default", this.defaultSpeeds);
			readSpeedValues(json, "max_speeds", countryMaxSpeeds);
			readSpeedValues(json, "surface", this.surfaceSpeeds);
			readSpeedValues(json, "tracktype", this.trackTypeSpeeds);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}
	
	private void readSpeedValues(JSONObject json, String keyName,  Map<String, Integer> speeds) {
		if (json.has(keyName)) {
			JSONObject jLimits = json.getJSONObject(keyName);
			JSONArray jKeys = jLimits.names();
			for(int i = 0; i < jKeys.length(); i++) {
				String key = jKeys.getString(i);
				speeds.put(key.toLowerCase(), jLimits.getInt(key));
			}
		}
	}
	
    public Integer getMaxSpeed(ReaderWay way) {
    	// check if maxspeed is explicitly given
    	if (way.hasTag("maxspeed"))
    		return -1;
    	
    	String key = way.getTag("zone:maxspeed");
    	if (key == null)
    		key = way.getTag("zone:traffic");
    	
    	if (key == null)
    		return -1;
    	
    	Integer res = countryMaxSpeeds.get(key.toLowerCase());
    	
    	return res == null ? -1 : res;
    }
    
    public Integer getTrackTypeSpeed(String tracktype) {
    	Integer res = trackTypeSpeeds.get(tracktype);
    	return res == null ? -1 : res;
    }
    
    public Integer getSurfaceSpeed(String surface)
    {
    	Integer res = surfaceSpeeds.get(surface);
    	return res == null ? -1 : res;
    }
    
    public Integer getSpeed(String highway)
    {
    	return defaultSpeeds.get(highway);
    }
    
    public boolean hasSpeedValue(String highway)
    {
    	return defaultSpeeds.containsKey(highway);
    }
}
