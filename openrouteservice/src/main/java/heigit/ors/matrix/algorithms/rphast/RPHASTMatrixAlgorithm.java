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
package heigit.ors.matrix.algorithms.rphast;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.ch.PrepareContractionHierarchies.DijkstraBidirectionCHRPHAST;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.Helper;

import heigit.ors.matrix.MatrixLocationData;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.PathMetricsExtractor;
import heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;
import heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import heigit.ors.routing.traffic.RealTrafficDataProvider;

public class RPHASTMatrixAlgorithm extends AbstractMatrixAlgorithm {
	private CHGraph _chGraph;
	private PrepareContractionHierarchies _prepareCH;
	private PathMetricsExtractor _pathMetricsExtractor;
	
	public void init(MatrixRequest req, GraphHopper gh, FlagEncoder encoder)
	{
		super.init(req, gh, encoder);

		_chGraph = (CHGraph)gh.getGraphHopperStorage().getGraph(CHGraph.class /* "fastest"*/);
		_prepareCH = _graphHopper.getCHFactoryDecorator().getPreparations().get(0);
		HintsMap hintsMap = new HintsMap();
		hintsMap.setWeighting(Helper.isEmpty(req.getWeightingMethod()) ? "fastest" : req.getWeightingMethod());
		Weighting _weighting = new ORSWeightingFactory(RealTrafficDataProvider.getInstance()).createWeighting(hintsMap, encoder, _chGraph, null, null);
		_pathMetricsExtractor = new PathMetricsExtractor(req.getMetrics(), _graphHopper.getGraphHopperStorage(), _encoder, _weighting);
	}

	@Override
	public MatrixResult compute(MatrixLocationData srcData, MatrixLocationData dstData, int metrics) {
		MatrixResult mtxResult = new MatrixResult();

		mtxResult.setSources(srcData.getCoordinates());
		mtxResult.setSourceNames(srcData.getNames());
		mtxResult.setDestinations(dstData.getCoordinates());
		mtxResult.setDestinationNames(dstData.getNames());

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

		DijkstraBidirectionCHRPHAST algorithm = _prepareCH.createRPHAST(_chGraph, _encoder);
		// Compute target tree only once as it is the same for every source
		IntObjectMap<SPTEntry> tree = algorithm.createTargetTree(dstData.getNodeIds());

		for (int source = 0; source < srcData.size(); source++) {
			if (srcData.getNodeId(source) == -1)
			{
				_pathMetricsExtractor.setEmptyValues(source * dstData.size(), srcData, dstData, times, distances, weights);
			}
			else
			{
				algorithm = _prepareCH.createRPHAST(_chGraph, _encoder);
				IntObjectMap<SPTEntry> destinationTree = algorithm.calcMatrix(srcData.getNodeId(source), dstData.getNodeIds(), tree, source * dstData.size());
				_pathMetricsExtractor.calcValues(source * dstData.size(), destinationTree, srcData, dstData, times, distances, weights);
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
