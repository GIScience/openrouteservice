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
