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
package heigit.ors.matrix.algorithms.rphast;

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;

import heigit.ors.matrix.MatrixLocations;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.MultiTreeMetricsExtractor;
import heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;
import heigit.ors.routing.algorithms.RPHASTAlgorithm;
import heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;

public class RPHASTMatrixAlgorithm extends AbstractMatrixAlgorithm {
	private PrepareContractionHierarchies _prepareCH;
	private MultiTreeMetricsExtractor _pathMetricsExtractor;

	public void init(MatrixRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting) {
		super.init(req, gh, graph, encoder, weighting);

		_prepareCH = _graphHopper.getCHFactoryDecorator().getPreparations().get(0);
		_pathMetricsExtractor = new MultiTreeMetricsExtractor(req.getMetrics(), graph, _encoder, weighting,
				req.getUnits());
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

		if (!srcData.hasValidNodes() || !dstData.hasValidNodes())
		{
			for (int srcIndex = 0; srcIndex < srcData.size(); srcIndex++) 
				_pathMetricsExtractor.setEmptyValues(srcIndex, srcData, dstData, times, distances, weights);
		}
		else
		{
			RPHASTAlgorithm algorithm = new RPHASTAlgorithm(_graph, _prepareCH.getPrepareWeighting(),
					TraversalMode.NODE_BASED);
			
			int[] srcIds = getValidNodeIds(srcData.getNodeIds());
			int[] destIds = getValidNodeIds(dstData.getNodeIds());
			
			algorithm.prepare(srcIds, destIds);

			MultiTreeSPEntry[] destTrees = algorithm.calcPaths(srcIds, destIds);

			MultiTreeSPEntry[] originalDestTrees = new MultiTreeSPEntry[dstData.size()];
			
			int j = 0;
			for (int i = 0; i < dstData.size(); i++) {
				if (dstData.getNodeIds()[i] != -1) {
					originalDestTrees[i] = destTrees[j];
					++j;
				} else {
					originalDestTrees[i] = null;
				}
			}

			_pathMetricsExtractor.calcValues(originalDestTrees, srcData, dstData, times, distances, weights);
		}

		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Duration))
			mtxResult.setTable(MatrixMetricsType.Duration, times);
		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Distance))
			mtxResult.setTable(MatrixMetricsType.Distance, distances);
		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Weight))
			mtxResult.setTable(MatrixMetricsType.Weight, weights);

		return mtxResult;
	}
	
	private int[] getValidNodeIds(int[] nodeIds)
	{
		List<Integer> nodeList = new ArrayList<Integer>();
		for (int dst : nodeIds) {
			if (dst != -1)
				nodeList.add(dst);

		}
		
		int[] res = new int[nodeList.size()];
		for (int i = 0; i < nodeList.size(); i++) 
			res[i] = nodeList.get(i);
		
		return res;
	}
}
