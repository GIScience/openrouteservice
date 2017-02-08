package heigit.ors.routing.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.graphhopper.util.Helper;

import heigit.ors.services.routing.RoutingServiceSettings;

public class RouteManagerConfiguration {
	public String SourceFile;
	public String ConfigPathsRoot;
	public int InitializationThreads;
	public String Mode = "Normal"; // Normal or PrepareGraphs
	public double DynamicWeightingMaxDistance = 0;
	public RouteUpdateConfiguration UpdateConfig;
	public TrafficInformationConfiguration TrafficInfoConfig;
    public RouteProfileConfiguration[] Profiles;
    
    public static RouteManagerConfiguration loadFromFile(String path) throws IOException, Exception
    {
    	RouteManagerConfiguration gc = new RouteManagerConfiguration();

    	if (!Helper.isEmpty(path))
    		RoutingServiceSettings.loadFromFile(path);
    	
    	gc.SourceFile = RoutingServiceSettings.getParameters("sources").get(0);
    	gc.Mode = RoutingServiceSettings.getParameter("mode");
    	gc.ConfigPathsRoot = RoutingServiceSettings.getParameter("config_path");
    	gc.InitializationThreads = Math.max(1, Integer.parseInt(RoutingServiceSettings.getParameter("init_threads")));
    	gc.DynamicWeightingMaxDistance = Double.parseDouble(RoutingServiceSettings.getParameter("dynamic_weighting_max_distance"));
   	   
    	// Read profile settings
    	List<RouteProfileConfiguration> profiles = new ArrayList<RouteProfileConfiguration>();
    	List<String> profileList = RoutingServiceSettings.getParameters("profiles.active");
    	
    	for(String item : profileList)
    	{
    		String profileRef = "profiles.profile-" + item;

    		RouteProfileConfiguration profile = new RouteProfileConfiguration();
    		profile.Enabled = true;
    		profile.GraphPath = RoutingServiceSettings.getParameter(profileRef + ".graph_path", true);
    		profile.ConfigPath = RoutingServiceSettings.getParameter(profileRef + ".config_path", true);
			profile.Profiles = RoutingServiceSettings.getParameter(profileRef + ".profiles");
    		
    		List<String> paramList = RoutingServiceSettings.getParameters(profileRef + ".parameters");
    		if (paramList != null)
    		{
    			for(String paramItem : paramList)
    	    	{
    				String[] paramValues = paramItem.split("=");
    				switch(paramValues[0].toLowerCase())
    				{
    				case "dynamic_weights":
    					profile.DynamicWeighting = Boolean.parseBoolean(paramValues[1]);
    					break;
    				case "surface_info":
    					profile.SurfaceInformation = Boolean.parseBoolean(paramValues[1]);
    					break;
    				case "hill_index":
    					profile.HillIndex = Boolean.parseBoolean(paramValues[1]);
    					break;
    				case "traffic":
    					profile.UseTrafficInformation = Boolean.parseBoolean(paramValues[1]);
    					break;
    				case "minimum_distance":
    					profile.MinimumDistance = Double.parseDouble(paramValues[1]);
    					break;
    				case "maximum_distance":
    					profile.MaximumDistance = Double.parseDouble(paramValues[1]);
    					break;
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
    	tic.ConfigPath =  RoutingServiceSettings.getParameter("traffic.config_path");
    	
    	gc.TrafficInfoConfig = tic;
    
    	return gc;
    }
}
