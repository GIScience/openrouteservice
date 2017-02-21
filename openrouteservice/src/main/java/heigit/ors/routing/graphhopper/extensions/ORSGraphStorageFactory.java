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

// Authors: M. Rylov

package heigit.ors.routing.graphhopper.extensions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import heigit.ors.routing.graphhopper.extensions.storages.*;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributesType;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.GHDirectory;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphExtension.ExtendedStorageSequence;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorageFactory;
import com.graphhopper.storage.TurnCostExtension;

public class ORSGraphStorageFactory implements GraphStorageFactory {

	private int _storageType;
	
	public ORSGraphStorageFactory(int storageType) {
		_storageType = storageType;
	}

	@Override
	public GraphHopperStorage createStorage(GHDirectory dir, GraphHopper gh) {
		EncodingManager encodingManager = gh.getEncodingManager();
		GraphHopperStorage graph = null;

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

		if (gh.hasElevation() && GraphStorageType.isSet(_storageType, GraphStorageType.HillIndex))
		{
			GraphExtension ext =  new HillIndexGraphStorage();
			graphExtensions.add(ext);
		}
		
	    if (GraphStorageType.isSet(_storageType, GraphStorageType.WaySurfaceType))
		{
			GraphExtension ext = new WaySurfaceTypeGraphStorage();
			graphExtensions.add(ext);
		}	

		if  (GraphStorageType.isSet(_storageType, GraphStorageType.WayCategory)) 
		{
			GraphExtension ext = new WayCategoryGraphStorage();
			graphExtensions.add(ext);
		}

		if (gh.isCHEnabled())
		{
    		gh.initCHAlgoFactories();
            return new GraphHopperStorage(new ArrayList<Weighting>(gh.getAlgorithmFactories2()), dir, encodingManager, gh.hasElevation(), getExtension(graphExtensions));
		}
		else  {
			int attrTypes = 0;
			
			if (encodingManager.supports("heavyvehicle"))
			{
				if  (GraphStorageType.isSet(_storageType, GraphStorageType.VehicleType)) 
					attrTypes |= HeavyVehicleAttributesType.VehicleType;		
				if  (GraphStorageType.isSet(_storageType, GraphStorageType.Restrictions)) 
					attrTypes |= HeavyVehicleAttributesType.Restrictions;						
			 
			    if (attrTypes > 0)
			    {
			    	GraphExtension ext = new HeavyVehicleAttributesGraphStorage(attrTypes);
			    	graphExtensions.add(ext);
			    }
			}			 
			
			if (encodingManager.supports("car") || encodingManager.supports("offroadvehicle"))
			{
			}

			if (encodingManager.supports("bike") || encodingManager.supports("safetybike") || encodingManager.supports("cycletourbike") || encodingManager.supports("mtb")  || encodingManager.supports("racingbike") || encodingManager.supports("electrobike"))
			{
			}
			
			if (encodingManager.supports("wheelchair"))
			{
				GraphExtension ext = new WheelchairAttributesGraphStorage();
				graphExtensions.add(ext);
			}
		} 
		
		if (geTurnCosts == null && graphExtensions == null)
			graph = new GraphHopperStorage(dir, encodingManager, gh.hasElevation(), new GraphExtension.NoOpExtension());
		else if (geTurnCosts != null && graphExtensions.size() > 0)
		{
			ArrayList<GraphExtension> seq = new ArrayList<GraphExtension>();
			seq.addAll(graphExtensions);
			seq.add(geTurnCosts);
			
			graph = new GraphHopperStorage(dir, encodingManager, gh.hasElevation(), getExtension(seq));
		} 
		else if (geTurnCosts != null)
		{
			graph = new GraphHopperStorage(dir, encodingManager, gh.hasElevation(), geTurnCosts);
		}
		else if (graphExtensions.size() > 0)
		{
			graph = new GraphHopperStorage(dir, encodingManager, gh.hasElevation(), getExtension(graphExtensions));
		}

		return graph;
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
		{}
		
		return false;
	}
}
