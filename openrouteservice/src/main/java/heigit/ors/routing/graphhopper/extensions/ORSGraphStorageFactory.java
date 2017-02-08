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
import heigit.ors.routing.graphhopper.extensions.BikeAttributesType;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributesType;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GHDirectory;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphExtension.ExtendedStorageSequence;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorageFactory;
import com.graphhopper.storage.TurnCostExtension;

public class ORSGraphStorageFactory implements GraphStorageFactory {

	private Boolean dynamicWeighting;
	private Boolean surfaceInfo;
	private Boolean hillIndex;
	
	public ORSGraphStorageFactory(Boolean dynamicWeighting, Boolean surfaceInfo, Boolean hillIndex) {
		this.dynamicWeighting = dynamicWeighting;
		this.surfaceInfo = surfaceInfo;
		this.hillIndex = hillIndex;
	}

	@Override
	public GraphHopperStorage createStorage(GHDirectory dir, GraphHopper gh) {
		EncodingManager encodingManager = gh.getEncodingManager();
		GraphHopperStorage graph = null;

		GraphExtension geTurnCosts = null;
		ArrayList<GraphExtension> geRestrictions = new ArrayList<GraphExtension>();
		
		if (encodingManager.needsTurnCostsSupport())
		{
			Path path = Paths.get(dir.getLocation(), "turn_costs");
			File fileEdges  = Paths.get(dir.getLocation(), "edges").toFile();
			File fileTurnCosts = path.toFile();
			
			// First we need to check if turncosts are available. This check is required when we introduce a new feature, but an existing graph does not have it yet.
			if ((!hasGraph(gh) && !fileEdges.exists()) || (fileEdges.exists() && fileTurnCosts.exists()))
				geTurnCosts =  new TurnCostExtension();
		}

		if (gh.isCHEnabled())
		{
			//graph = new gh(dir, encodingManager, gh.hasElevation());
			//return graph;
			return null;
		}
		else if (dynamicWeighting) {
		
			if (gh.hasElevation() && hillIndex)
			{
				GraphExtension ext =  new HillIndexGraphStorage();
				geRestrictions.add(ext);
			}
			
			int attrTypes = 0;
			if (encodingManager.supports("heavyvehicle"))
			{
				attrTypes = HeavyVehicleAttributesType.WayType | HeavyVehicleAttributesType.VehicleType | HeavyVehicleAttributesType.Restrictions;
			    if (surfaceInfo)
			    	attrTypes |= HeavyVehicleAttributesType.WaySurface;
				GraphExtension ext = new HeavyVehicleAttributesGraphStorage(attrTypes);
				geRestrictions.add(ext);
			}			 
			
			if (encodingManager.supports("offroadvehicle"))
			{
				attrTypes = MotorcarAttributesType.WayType | MotorcarAttributesType.Restrictions | MotorcarAttributesType.Passability;
				
				 if (surfaceInfo)
				    attrTypes |= MotorcarAttributesType.WaySurface;
				 GraphExtension ext = new MotorcarAttributesGraphStorage(attrTypes);
				 geRestrictions.add(ext);
			}
			
			if (encodingManager.supports("bike") || encodingManager.supports("safetybike") || encodingManager.supports("cycletourbike") || encodingManager.supports("mtb")  || encodingManager.supports("racingbike"))
			{
				//gh.hasElevation()
				
				attrTypes = BikeAttributesType.WayType;
				 if (surfaceInfo)
					attrTypes |= BikeAttributesType.WaySurface;
				 if (gh.hasElevation())
					 attrTypes |= BikeAttributesType.DifficultyGrade;
				 
				GraphExtension ext = new BikeAttributesGraphStorage(attrTypes);
				geRestrictions.add(ext);
			}
			
			if (encodingManager.supports("wheelchair"))
			{
				GraphExtension ext = new WheelchairAttributesGraphStorage();
				geRestrictions.add(ext);
			}
			
			if (encodingManager.supports("car"))
			{
				attrTypes = MotorcarAttributesType.WayType | MotorcarAttributesType.Restrictions;
				if (surfaceInfo)
					attrTypes |= MotorcarAttributesType.WaySurface;
				GraphExtension ext = new MotorcarAttributesGraphStorage(attrTypes);
				geRestrictions.add(ext);
			}
		} 
		
		if (geTurnCosts == null && geRestrictions == null)
			graph = new GraphHopperStorage(dir, encodingManager, gh.hasElevation(), new GraphExtension.NoOpExtension());
		else if (geTurnCosts != null && geRestrictions.size() > 0)
		{
			ArrayList<GraphExtension> seq = new ArrayList<GraphExtension>();
			seq.addAll(geRestrictions);
			seq.add(geTurnCosts);
			graph = new GraphHopperStorage(dir, encodingManager, gh.hasElevation(), new ExtendedStorageSequence(seq));
		} 
		else if (geTurnCosts != null)
		{
			graph = new GraphHopperStorage(dir, encodingManager, gh.hasElevation(), geTurnCosts);
		}
		else if (geRestrictions.size() > 0)
		{
			ArrayList<GraphExtension> seq = new ArrayList<GraphExtension>();
			seq.addAll(geRestrictions);
			graph = new GraphHopperStorage(dir, encodingManager, gh.hasElevation(), new ExtendedStorageSequence(seq));
		}

		return graph;
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
