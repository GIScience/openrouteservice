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
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.*;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ORSGraphStorageFactory implements GraphStorageFactory {

	private static final Logger LOGGER = Logger.getLogger(ORSGraphStorageFactory.class.getName());

	private final List<GraphStorageBuilder> graphStorageBuilders;

	public ORSGraphStorageFactory(List<GraphStorageBuilder> graphStorageBuilders) {
		this.graphStorageBuilders = graphStorageBuilders;
	}

	@Override
	public GraphHopperStorage createStorage(GHDirectory dir, GraphHopper gh) {
		EncodingManager encodingManager = gh.getEncodingManager();
		GraphExtension geTurnCosts = null;
		ArrayList<GraphExtension> graphExtensions = new ArrayList<>();

		if (encodingManager.needsTurnCostsSupport()) {
			Path path = Paths.get(dir.getLocation(), "turn_costs");
			File fileEdges  = Paths.get(dir.getLocation(), "edges").toFile();
			File fileTurnCosts = path.toFile();

			// TODO: Clarify what this is about. TurnCost are handled differently now.
			// First we need to check if turncosts are available. This check is required when we introduce a new feature, but an existing graph does not have it yet.
			if ((!hasGraph(gh) && !fileEdges.exists()) || (fileEdges.exists() && fileTurnCosts.exists())) {
				//	geTurnCosts =  new TurnCostExtension();
			}
		}

		if (graphStorageBuilders != null) {
			List<GraphStorageBuilder> iterateGraphStorageBuilders = new ArrayList<>(graphStorageBuilders);
			for(GraphStorageBuilder builder : iterateGraphStorageBuilders) {
				try {
					GraphExtension ext = builder.init(gh);
					if (ext != null)
						graphExtensions.add(ext);
				} catch(Exception ex) {
					graphStorageBuilders.remove(builder);
					LOGGER.error(ex);
				}
			}
		}

		if(gh instanceof ORSGraphHopper) {
			if (((ORSGraphHopper) gh).isCoreEnabled()) {
			// TODO:	((ORSGraphHopper) gh).initCoreAlgoFactoryDecorator();
			}
			if (((ORSGraphHopper) gh).isCoreLMEnabled()) {
				//TODO: ((ORSGraphHopper) gh).initCoreLMAlgoFactoryDecorator();
			}
		}

		// TODO: AlgorithmFactoryDecorators are gone. Do we need to init algos differently?
//		if (gh.getCHFactoryDecorator().isEnabled())
//			gh.initCHAlgoFactoryDecorator();
//
		List<CHProfile> profiles = new ArrayList<>();
//
//		if (gh.isCHEnabled()) {
//			profiles.addAll(gh.getCHFactoryDecorator().getCHProfiles());
//		}
		if (((ORSGraphHopper)gh).isCoreEnabled()) {
			// TODO: profiles.addAll(((ORSGraphHopper)gh).getCorePreparationHandler().getCHProfiles());
		}

		GraphHopperStorage ghs = new GraphHopperStorage(dir, encodingManager, gh.hasElevation());
		ExtendedStorageSequence extendedStorages = new ExtendedStorageSequence(graphExtensions);
		extendedStorages.init(ghs.getBaseGraph(), dir);
		ghs.setExtendedStorages(extendedStorages);
		return ghs;
	}

	private boolean hasGraph(GraphHopper gh) {
		try {
			gh.getGraphHopperStorage();
			return true;
		} catch (IllegalStateException ex){
			// do nothing
		} catch(Exception ex) {
			LOGGER.error(ex.getStackTrace());
		}
		return false;
	}
}
