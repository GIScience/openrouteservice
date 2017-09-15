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
package heigit.ors.matrix.algorithms.dijkstra;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;

import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.MatrixLocations;
import heigit.ors.matrix.PathMetricsExtractor;
import heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;
import heigit.ors.routing.algorithms.DijkstraOneToManyAlgorithm;
import heigit.ors.services.matrix.MatrixServiceSettings;

public class DijkstraMatrixAlgorithm extends AbstractMatrixAlgorithm {
	private PathMetricsExtractor _pathMetricsExtractor;

	public void init(MatrixRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting)
	{
		super.init(req, gh, graph, encoder, weighting);

		_pathMetricsExtractor = new PathMetricsExtractor(req.getMetrics(), _graph, _encoder, _weighting, req.getUnits());
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
			DijkstraOneToManyAlgorithm algorithm = new DijkstraOneToManyAlgorithm(_graph, _weighting, TraversalMode.NODE_BASED);
			algorithm.prepare(srcData.getNodeIds(),  dstData.getNodeIds());
			algorithm.setMaxVisitedNodes(MatrixServiceSettings.getMaximumVisitedNodes());
			
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
					SPTEntry[] targets = algorithm.calcPaths(sourceId, dstData.getNodeIds());

					if (algorithm.getFoundTargets() != algorithm.getTargetsCount())
						throw new Exception("Search exceeds the limit of visited nodes.");

					if (targets != null)
					{
						_pathMetricsExtractor.calcValues(srcIndex, targets, srcData, dstData, times, distances, weights);
					}
				}
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
