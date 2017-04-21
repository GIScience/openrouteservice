package heigit.ors.routing.configuration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.FileUtility;

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
		Map<String,Object> defaultParams = RoutingServiceSettings.getParametersMap("profiles.default_params");
		String rootGraphsPath = (defaultParams != null && defaultParams.containsKey("graphs_root_path")) ? defaultParams.get("graphs_root_path").toString() : null;

		for(String item : profileList)
		{
			String profileRef = "profiles.profile-" + item;

			RouteProfileConfiguration profile = new RouteProfileConfiguration();
			profile.Name = item;
			profile.Enabled = true;
			profile.Profiles = RoutingServiceSettings.getParameter(profileRef + ".profiles");

			String graphPath = RoutingServiceSettings.getParameter(profileRef + ".graph_path", false);

			if (!Helper.isEmpty(rootGraphsPath))
			{
				if (Helper.isEmpty(graphPath))
					graphPath = Paths.get(rootGraphsPath, item).toString();
				else if (!FileUtility.isAbsolutePath(graphPath))
					graphPath = Paths.get(rootGraphsPath, graphPath).toString();
			}

			profile.GraphPath = graphPath;

			Map<String, Object> profileParams = RoutingServiceSettings.getParametersMap(profileRef + ".parameters");

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
					case "ch_weighting":
						profile.CHWeighting = paramItem.getValue().toString();
						break;
					case "ch_threads":
						profile.CHThreads = Integer.parseInt(paramItem.getValue().toString());
						break;
					case "encoder_options":
						profile.EncoderOptions = paramItem.getValue().toString();
						break;
					case "encoder_flags_size":
						profile.EncoderFlagsSize = Integer.parseInt(paramItem.getValue().toString());
						break;
					case "instructions":
						profile.Instructions = Boolean.parseBoolean(paramItem.getValue().toString());
						break;
					case "elevation":
						if (Boolean.parseBoolean(paramItem.getValue().toString()))
						{
							profile.ElevationProvider = profileParams.get("elevation_provider").toString();
							if (profileParams.get("elevation_data_access") != null)
								profile.ElevationDataAccess  = profileParams.get("elevation_data_access").toString();
							profile.ElevationCachePath = profileParams.get("elevation_cache_path").toString();

							if (profileParams.get("elevation_cache_clear") != null)
							{
								String clearCache = profileParams.get("elevation_cache_clear").toString();
								if (!Helper.isEmpty(clearCache))
									profile.ElevationCacheClear = Boolean.parseBoolean(clearCache);
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
									storageParams.put(entry.getKey(), entry.getValue().toString());
								}
							}

							profile.ExtStorages.put(storageEntry.getKey(), storageParams);
						}
						//
						//paramItem.getValue();
						//profile.ExtStorages = paramItem.getValue().toString();
						break;
					case "traffic":
						profile.UseTrafficInformation = Boolean.parseBoolean(paramItem.getValue().toString());
						break;
					case "maximum_distance":
						profile.MaximumDistance = Double.parseDouble(paramItem.getValue().toString());
						break;
					case "extent":
						@SuppressWarnings("unchecked") 
						List<Double> bbox = (List<Double>)paramItem.getValue();

						if (bbox.size() != 4)
							throw new Exception("'extent' element must contain 4 elements.");
						profile.BBox = new Envelope(bbox.get(0),bbox.get(1),bbox.get(2),bbox.get(3));
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
		tic.LocationCodesPath = RoutingServiceSettings.getParameter("traffic.location_codes_path");
		tic.MessagesDatasource = RoutingServiceSettings.getParameter("traffic.source");
		tic.OutputDirectory = RoutingServiceSettings.getParameter("traffic.output_directory");
		tic.UpdateInterval = Integer.parseInt(RoutingServiceSettings.getParameter("traffic.update_interval"));

		gc.TrafficInfoConfig = tic;

		return gc;
	}
}
