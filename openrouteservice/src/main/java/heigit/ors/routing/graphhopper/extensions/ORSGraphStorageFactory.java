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
package heigit.ors.routing.graphhopper.extensions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.graphhopper.routing.weighting.Weighting;
import org.apache.log4j.Logger;

import heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GHDirectory;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphExtension.ExtendedStorageSequence;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorageFactory;
import com.graphhopper.storage.TurnCostExtension;

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

		if(gh instanceof ORSGraphHopper) {
			if (((ORSGraphHopper) gh).isCoreEnabled())
				((ORSGraphHopper) gh).initCoreAlgoFactoryDecorator();
		}

		if (gh.getLMFactoryDecorator().isEnabled())
			gh.initLMAlgoFactoryDecorator();

		if (gh.getCHFactoryDecorator().isEnabled())
			gh.initCHAlgoFactoryDecorator();

		List<Weighting> weightings = new ArrayList<>();
		List<String> suffixes = new ArrayList<>();
		int chGraphs = 0;

		if(gh.isCHEnabled()) {
			weightings.addAll(gh.getCHFactoryDecorator().getWeightings());
			chGraphs = weightings.size();
			for (int i = 0; i < chGraphs; i++){
				suffixes.add("ch");
			}
		}

		if(((ORSGraphHopper) gh).isCoreEnabled()) {
			weightings.addAll(((ORSGraphHopper) gh).getCoreFactoryDecorator().getWeightings());
			for (int i = chGraphs; i < weightings.size(); i++) {
				suffixes.add("core");
			}
		}

		if (!weightings.isEmpty())
			return new GraphHopperStorage(weightings,
					dir,
					encodingManager,
					gh.hasElevation(),
					graphExtension,
					suffixes);

//		if (gh.isCHEnabled())
//			return new GraphHopperStorage(gh.getCHFactoryDecorator().getWeightings(), dir, encodingManager, gh.hasElevation(), graphExtension);
//		else if (((ORSGraphHopper) gh).isCoreEnabled()){
//			return new GraphHopperStorage(((ORSGraphHopper) gh).getCoreFactoryDecorator().getWeightings(),
//					dir,
//					encodingManager,
//					gh.hasElevation(),
//					graphExtension,
//					"core");
//		}
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
		{}

		return false;
	}
}
