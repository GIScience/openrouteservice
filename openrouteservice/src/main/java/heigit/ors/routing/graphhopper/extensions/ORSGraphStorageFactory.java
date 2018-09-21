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
package heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.*;
import heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ORSGraphStorageFactory implements GraphStorageFactory {

	private static Logger LOGGER = Logger.getLogger(ORSGraphStorageFactory.class.getName());
	
	private List<GraphStorageBuilder> _graphStorageBuilders;
	
	public ORSGraphStorageFactory(List<GraphStorageBuilder> graphStorageBuilders) {
		_graphStorageBuilders = graphStorageBuilders;
	}

	@Override
	public GraphHopperStorage createStorage(GHDirectory dir, GraphHopper gh) {
		EncodingManager encodingManager = gh.getEncodingManager();
		GraphExtension geTurnCosts = null;
		ArrayList<GraphExtension> graphExtensions = new ArrayList<GraphExtension>();
		
		if (encodingManager.needsTurnCostsSupport())
		{
			Path path = Paths.get(dir.getLocation(), "turn_costs");
			File fileEdges  = Paths.get(dir.getLocation(), "edges").toFile();
			File fileTurnCosts = path.toFile();
			
			// First we need to check if turncosts are available. This check is required when we introduce a new feature, but an existing graph does not have it yet.
			if ((!hasGraph(gh) && !fileEdges.exists()) || (fileEdges.exists() && fileTurnCosts.exists()))
				geTurnCosts =  new TurnCostExtension();
		}

		if (_graphStorageBuilders != null && _graphStorageBuilders.size() > 0)
		{
			for(GraphStorageBuilder builder : _graphStorageBuilders)
			{
				try
				{
					GraphExtension ext = builder.init(gh);
					if (ext != null)
						graphExtensions.add(ext);
				}
				catch(Exception ex)
				{
					LOGGER.error(ex);
				}
			}
		}

		GraphExtension graphExtension = null;
		
		if (geTurnCosts == null && graphExtensions.size() == 0)
			graphExtension =  new GraphExtension.NoOpExtension();
		else if (geTurnCosts != null && graphExtensions.size() > 0)
		{
			ArrayList<GraphExtension> seq = new ArrayList<GraphExtension>();
			seq.add(geTurnCosts);
			seq.addAll(graphExtensions);
			
			graphExtension = getExtension(seq);
		} 
		else if (geTurnCosts != null)
		{
			graphExtension = geTurnCosts;
		}
		else if (graphExtensions.size() > 0)
		{
			graphExtension = getExtension(graphExtensions);
		}
		
	    if (gh.getLMFactoryDecorator().isEnabled())
          	gh.initLMAlgoFactoryDecorator();

	    if (gh.getCHFactoryDecorator().isEnabled()) 
            gh.initCHAlgoFactoryDecorator();
	     
		if (gh.isCHEnabled())
            return new GraphHopperStorage(gh.getCHFactoryDecorator().getWeightings(), dir, encodingManager, gh.hasElevation(), graphExtension);
		else
			return new GraphHopperStorage(dir, encodingManager, gh.hasElevation(), graphExtension);
	}
	
	private GraphExtension getExtension(ArrayList<GraphExtension> graphExtensions)
	{
		if (graphExtensions.size() > 1)
	    {
	    	ArrayList<GraphExtension> seq = new ArrayList<GraphExtension>();
	    	seq.addAll(graphExtensions);
	    	return new ExtendedStorageSequence(seq);
	    }
	    else
	    	return graphExtensions.size() == 0 ? new GraphExtension.NoOpExtension() : graphExtensions.get(0);
	}
	
	private boolean hasGraph(GraphHopper gh)
	{
		try
		{
			gh.getGraphHopperStorage();
			return true;
		}
		catch(Exception ex)
		{
			if(ex instanceof IllegalStateException){

			}else {
				ex.printStackTrace();
			}
		}
		
		return false;
	}
}
