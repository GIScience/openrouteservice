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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphhopper.util.Helper;
import com.typesafe.config.ConfigFactory;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.FileUtility;
import heigit.ors.util.StringUtility;

public class RoutingManagerConfiguration 
{
	public RouteUpdateConfiguration UpdateConfig;
	public TrafficInformationConfiguration TrafficInfoConfig;
	public RouteProfileConfiguration[] Profiles;

	public static RoutingManagerConfiguration loadFromFile(String path) throws IOException, Exception
	{
		RoutingManagerConfiguration gc = new RoutingManagerConfiguration();

		if (!Helper.isEmpty(path))
			RoutingServiceSettings.loadFromFile(path);

		// Read profile settings
		List<RouteProfileConfiguration> profiles = new ArrayList<RouteProfileConfiguration>();
		List<String> profileList = RoutingServiceSettings.getParametersList("profiles.active");
		Map<String,Object> defaultParams = RoutingServiceSettings.getParametersMap("profiles.default_params", true);
		String rootGraphsPath = (defaultParams != null && defaultParams.containsKey("graphs_root_path")) ? StringUtility.trim(defaultParams.get("graphs_root_path").toString(), '"') : null;

		for(String item : profileList)
		{
			String profileRef = "profiles.profile-" + item;

			RouteProfileConfiguration profile = new RouteProfileConfiguration();
			profile.setName(item);
			profile.setEnabled(true);
			profile.setProfiles(RoutingServiceSettings.getParameter(profileRef + ".profiles"));

			String graphPath = RoutingServiceSettings.getParameter(profileRef + ".graph_path", false);
			if (!Helper.isEmpty(rootGraphsPath))
			{
				if (Helper.isEmpty(graphPath))
					graphPath = Paths.get(rootGraphsPath, item).toString();
				else if (!FileUtility.isAbsolutePath(graphPath))
					graphPath = Paths.get(rootGraphsPath, graphPath).toString();
			}

			profile.setGraphPath(graphPath);

			Map<String, Object> profileParams = RoutingServiceSettings.getParametersMap(profileRef + ".parameters", true);

			if (profileParams == null)
				profileParams = defaultParams;
			else if (defaultParams != null)
			{
				for(Map.Entry<String, Object> defParamItem : defaultParams.entrySet())
				{
					if (!profileParams.containsKey(defParamItem.getKey()))
						profileParams.put(defParamItem.getKey(), defParamItem.getValue());
				}				
			}

			if (profileParams != null)
			{
				for(Map.Entry<String, Object> paramItem : profileParams.entrySet())
				{
					switch(paramItem.getKey())
					{
					case "preparation":
						profile.setPreparationOpts(ConfigFactory.parseString(paramItem.getValue().toString()));
						break;
					case "execution":
						profile.setExecutionOpts(ConfigFactory.parseString(paramItem.getValue().toString()));
						break;
					case "encoder_options":
						profile.setEncoderOptions(StringUtility.trimQuotes(paramItem.getValue().toString()));
						break;
					case "optimize":
						profile.setOptimize(Boolean.parseBoolean(paramItem.getValue().toString()));
						break;
					case "encoder_flags_size":
						profile.setEncoderFlagsSize(Integer.parseInt(paramItem.getValue().toString()));
						break;
					case "instructions":
						profile.setInstructions(Boolean.parseBoolean(paramItem.getValue().toString()));
						break;
					case "elevation":
						if (Boolean.parseBoolean(paramItem.getValue().toString()))
						{
							profile.setElevationProvider(StringUtility.trimQuotes(profileParams.get("elevation_provider").toString()));
							if (profileParams.get("elevation_data_access") != null)
								profile.setElevationDataAccess( StringUtility.trimQuotes(profileParams.get("elevation_data_access").toString()));
							profile.setElevationCachePath( StringUtility.trimQuotes(profileParams.get("elevation_cache_path").toString()));

							if (profileParams.get("elevation_cache_clear") != null)
							{
								String clearCache =  StringUtility.trimQuotes(profileParams.get("elevation_cache_clear").toString());
								if (!Helper.isEmpty(clearCache))
									profile.setElevationCacheClear(Boolean.parseBoolean(clearCache));
							}
						}
						break;
					case "ext_storages":
						@SuppressWarnings("unchecked") 
						Map<String, Object> storageList = (Map<String, Object>)paramItem.getValue();

						for(Map.Entry<String, Object> storageEntry : storageList.entrySet())
						{
							@SuppressWarnings("unchecked")
							Map<String, Object> entryValue = (Map<String, Object>)storageEntry.getValue();
							Map<String, String> storageParams = new HashMap<String, String>();

							if (storageParams != null)		
							{		
								for(Map.Entry<String, Object> entry : entryValue.entrySet())
								{	
									storageParams.put(entry.getKey(), StringUtility.trimQuotes(entry.getValue().toString()));
								}
							}

							profile.getExtStorages().put(storageEntry.getKey(), storageParams);
						}
						break;
					case "graph_processors":
						@SuppressWarnings("unchecked") 
						Map<String, Object> storageList2 = (Map<String, Object>)paramItem.getValue();

						for(Map.Entry<String, Object> storageEntry : storageList2.entrySet())
						{
							@SuppressWarnings("unchecked")
							Map<String, Object> entryValue = (Map<String, Object>)storageEntry.getValue();
							Map<String, String> storageParams = new HashMap<String, String>();

							if (storageParams != null)		
							{		
								for(Map.Entry<String, Object> entry : entryValue.entrySet())
								{	
									storageParams.put(entry.getKey(), StringUtility.trimQuotes(entry.getValue().toString()));
								}
							}

							profile.getGraphBuilders().put(storageEntry.getKey(), storageParams);
						}
						break;
					case "traffic":
						profile.setUseTrafficInformation(Boolean.parseBoolean(paramItem.getValue().toString()));
						break;
					case "maximum_distance":
						profile.setMaximumDistance(Double.parseDouble(paramItem.getValue().toString()));
						break;
					case "maximum_segment_distance_with_dynamic_weights":
						profile.setMaximumSegmentDistanceWithDynamicWeights(Double.parseDouble(paramItem.getValue().toString()));
						break;
					case "maximum_waypoints":
						profile.setMaximumWayPoints(Integer.parseInt(paramItem.getValue().toString()));
						break;
					case "extent":
						@SuppressWarnings("unchecked") 
						List<Double> bbox = (List<Double>)paramItem.getValue();

						if (bbox.size() != 4)
							throw new Exception("'extent' element must contain 4 elements.");
						profile.setExtent(new Envelope(bbox.get(0),bbox.get(1),bbox.get(2),bbox.get(3)));
					}
				}
			}

			profiles.add(profile);
		}

		gc.Profiles = (RouteProfileConfiguration[])profiles.toArray(new RouteProfileConfiguration[profiles.size()]);

		// Read update settings
		RouteUpdateConfiguration ruc = new RouteUpdateConfiguration();
		ruc.Enabled = Boolean.parseBoolean(RoutingServiceSettings.getParameter("update.enabled"));
		ruc.Time = RoutingServiceSettings.getParameter("update.time");
		ruc.DataSource = RoutingServiceSettings.getParameter("update.source");
		ruc.Extent = RoutingServiceSettings.getParameter("update.extent");
		ruc.WorkingDirectory = RoutingServiceSettings.getParameter("update.working_directory");

		gc.UpdateConfig = ruc;

		// Read traffic settings
		TrafficInformationConfiguration tic = new TrafficInformationConfiguration();
		tic.Enabled = Boolean.parseBoolean(RoutingServiceSettings.getParameter("traffic.enabled"));
		if (tic.Enabled)
		{
			tic.LocationCodesPath = RoutingServiceSettings.getParameter("traffic.location_codes_path");
			tic.MessagesDatasource = RoutingServiceSettings.getParameter("traffic.source");
			tic.OutputDirectory = RoutingServiceSettings.getParameter("traffic.output_directory");
			tic.UpdateInterval = Integer.parseInt(RoutingServiceSettings.getParameter("traffic.update_interval"));
		}
		gc.TrafficInfoConfig = tic;

		return gc;
	}
}
