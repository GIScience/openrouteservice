/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;

public class GraphStorageBuilderService {
	private static final Logger LOGGER = Logger.getLogger(GraphStorageBuilderService.class.getName());
	
	private static GraphStorageBuilderService _service;
    private ServiceLoader<GraphStorageBuilder> _loader;
    private Object _lockObj;
    
    private GraphStorageBuilderService() {
    	_loader = ServiceLoader.load(GraphStorageBuilder.class);
    	_lockObj = new Object();
    }

    public static synchronized GraphStorageBuilderService getInstance() {
        if (_service == null) {
        	_service = new GraphStorageBuilderService();
        }
        return _service;
    }
    
    public List<GraphStorageBuilder> createBuilders(Map<String, Map<String, String>> parameters)
    {
    	List<GraphStorageBuilder> result = new ArrayList<GraphStorageBuilder>();
    	
    	if (parameters != null && parameters.size() > 0)
		{
			for(Map.Entry<String, Map<String, String>> storageEntry : parameters.entrySet())
			{
				GraphStorageBuilder builder = GraphStorageBuilderService.getInstance().createBuilder(storageEntry.getKey(), storageEntry.getValue());
				
				if (builder != null)
				{
					builder.setParameters(storageEntry.getValue());
					result.add(builder);
				}
				else
					LOGGER.warn(String.format("GraphStorageBuilder '%s' was not found.", storageEntry.getKey()));
			}
		}
    	
    	return result;
    }

	public GraphStorageBuilder createBuilder(String name, Map<String, String> parameters)
	{
		GraphStorageBuilder builder = null;

		try
		{
			// ServiceLoader is not threadsafe
			synchronized(_lockObj)
			{
				Iterator<GraphStorageBuilder> entries = _loader.iterator();
				while (builder == null && entries.hasNext()) {
					GraphStorageBuilder entry = entries.next();
					if (entry.getName().equalsIgnoreCase(name))
					{
						builder =  entry.getClass().newInstance();
						break;
					}
				}
			}
		}
		catch (ServiceConfigurationError | InstantiationException | IllegalAccessException se) 
		{
			builder = null;
			se.printStackTrace();
		}
		
		return builder;
	}
}
