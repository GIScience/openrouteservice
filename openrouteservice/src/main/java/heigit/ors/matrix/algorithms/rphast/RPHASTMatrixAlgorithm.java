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
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;

import heigit.ors.matrix.MatrixLocations;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.PathMetricsExtractor;
import heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;

public class RPHASTMatrixAlgorithm extends AbstractMatrixAlgorithm {
	private PrepareContractionHierarchies _prepareCH;
	private PathMetricsExtractor _pathMetricsExtractor;
	
	public void init(MatrixRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting)
	{
		super.init(req, gh, graph, encoder, weighting);

		_prepareCH = _graphHopper.getCHFactoryDecorator().getPreparations().get(0);
		_pathMetricsExtractor = new PathMetricsExtractor(req.getMetrics(), graph, _encoder, weighting, req.getUnits());
	}

	@Override
	public MatrixResult compute(MatrixLocations srcData, MatrixLocations dstData, int metrics) throws Exception {
		MatrixResult mtxResult = new MatrixResult(srcData.getLocations(), dstData.getLocations());

		float[] times = null; 
		float[] distances = null; 
		float[] weights = null;
 
		int tableSize = srcData.size() * dstData.size();
		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Duration))
			times = new float[tableSize];
		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Distance)) 
			distances = new float[tableSize];
		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Weight))
			weights = new float[tableSize];

		DijkstraBidirectionCHRPHAST algorithm = _prepareCH.createRPHAST(_graph, _encoder); 
		// Compute target tree only once as it is the same for every source
		IntObjectMap<SPTEntry> tree = algorithm.createTargetTree(dstData.getNodeIds());
        int sourceId = -1; 
        
		for (int srcIndex = 0; srcIndex < srcData.size(); srcIndex++) {
			sourceId = srcData.getNodeId(srcIndex);
			if (sourceId == -1)
			{
				_pathMetricsExtractor.setEmptyValues(srcIndex, srcData, dstData, times, distances, weights);
			}
			else
			{  
				algorithm = _prepareCH.createRPHAST(_graph, _encoder);
				IntObjectMap<SPTEntry> destinationTree = algorithm.calcMatrix(sourceId, dstData.getNodeIds(), tree, srcIndex * dstData.size());
				_pathMetricsExtractor.calcValues(srcIndex , destinationTree, srcData, dstData, times, distances, weights); 
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
