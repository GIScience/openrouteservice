/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.matrix.algorithms.dijkstra;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.GraphHopper;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Helper;

import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.MatrixSearchData;
import heigit.ors.matrix.PathMetricsExtractor;
import heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;
import heigit.ors.routing.DijkstraOneToMany;
import heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.services.matrix.MatrixServiceSettings;

public class DijkstraMatrixAlgorithm extends AbstractMatrixAlgorithm {
	private Graph _graph;
	private Weighting _weighting;
	private PathMetricsExtractor _pathMetricsExtractor;
	
	public void init(MatrixRequest req, GraphHopper gh, FlagEncoder encoder)
	{
		super.init(req, gh, encoder);

		_graph = gh.getGraphHopperStorage().getBaseGraph();
		HintsMap hintsMap = new HintsMap();
		hintsMap.setWeighting(Helper.isEmpty(req.getWeightingMethod()) ? "fastest" : req.getWeightingMethod());
		_weighting = new ORSWeightingFactory(RealTrafficDataProvider.getInstance()).createWeighting(hintsMap, encoder, _graph, null, null);
		_pathMetricsExtractor = new PathMetricsExtractor(req.getMetrics(), _graph, _encoder, _weighting, req.getUnits());
	}

	@Override
	public MatrixResult compute(MatrixSearchData srcData, MatrixSearchData dstData, int metrics) throws Exception {
		MatrixResult mtxResult = new MatrixResult(srcData.getLocations(), dstData.getLocations());

		float[] times = new float[srcData.size() * dstData.size()];
		float[] distances = null; 
		float[] weights = null;

		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Distance) || MatrixMetricsType.isSet(metrics, MatrixMetricsType.Weight))
		{
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Distance)) 
				distances = new float[srcData.size() * dstData.size()];
			if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Weight))
				weights = new float[srcData.size() * dstData.size()];
		}

		DijkstraOneToMany algorithm = new DijkstraOneToMany(_graph, _weighting, TraversalMode.NODE_BASED);
		algorithm.setMaxVisitedNodes(MatrixServiceSettings.getMaximumVisitedNodes());
		IntObjectMap<SPTEntry> targets =  new GHIntObjectHashMap<SPTEntry>(dstData.size());
		for (int nodeId : dstData.getNodeIds())
		{
			if (nodeId != -1)
				targets.put(nodeId, new SPTEntry(EdgeIterator.NO_EDGE, 1, 1));
		}
		algorithm.setTargets(targets);

		int sourceId = -1;
		
		for (int srcIndex = 0; srcIndex < srcData.size(); srcIndex++) {
			sourceId = srcData.getNodeId(srcIndex);
			
			if (sourceId == -1)
			{
				_pathMetricsExtractor.setEmptyValues(srcIndex, srcData, dstData, times, distances, weights);
			}
			else
			{
				algorithm.reset();
				algorithm.calcPath(sourceId, -1);
				
				if (algorithm.getFoundTargets() != targets.size())
					throw new Exception("Search exceeds the limit of visited nodes.");
				
				targets.get(sourceId).edge = EdgeIterator.NO_EDGE;

				_pathMetricsExtractor.calcValues(srcIndex, targets, srcData, dstData, times, distances, weights);
			}
		}

		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Duration))
			mtxResult.setTable(MatrixMetricsType.Duration, times);
		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Distance))
			mtxResult.setTable(MatrixMetricsType.Distance, distances);
		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Weight))
			mtxResult.setTable(MatrixMetricsType.Weight, weights);

		return mtxResult;
	}
}
