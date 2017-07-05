package heigit.ors.matrix;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.util.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;

public class PathMetricsExtractor {
	private int _metrics;
	private Graph _graph;
	private CHGraph _chGraph;
	private Weighting _weighting;
	private Weighting _timeWeighting;
	private double _edgeWeight;
	private double _edgeTime;
	private DistanceUnit _distUnits;
	private boolean _reverseOrder = true;

	public PathMetricsExtractor(int metrics, Graph graph, FlagEncoder encoder, Weighting weighting, DistanceUnit units)
	{
		_metrics = metrics;
		_graph = graph;
		_weighting = weighting;
		_timeWeighting = new FastestWeighting(encoder);
		_distUnits = units;
		
		if (graph instanceof CHGraph)
			_chGraph = (CHGraph)_graph;
	}
	
	public void setEmptyValues(int sourceIndex, MatrixSearchData srcData, MatrixSearchData dstData, float[] times, float[] distances, float[] weights)
	{
		int i = sourceIndex * dstData.size();
		int[] targetNodes = dstData.getNodeIds();
		
		for (@SuppressWarnings("unused") int target : targetNodes) {
			if (times != null)
				times[i] = -1;
			if (distances != null)
				distances[i] = -1;
			if (weights != null)
				weights[i] = -1;
			i++;
		}
	}
	
	public void calcValues(int sourceIndex, IntObjectMap<SPTEntry> targets, MatrixSearchData srcData, MatrixSearchData dstData, float[] times, float[] distances, float[] weights) throws Exception
	{
		if (targets == null)
			throw new IllegalStateException("Target destinations not set"); 

		int index = sourceIndex* dstData.size();
		double time = 0.0;
		double distance = 0.0;
		double weight = 0.0;
		boolean calcTime = MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Duration);
		boolean calcDistance = MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Distance);
		boolean calcWeight = MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Weight);
		
		int targetIndex = 0;
		int[] targetNodes = dstData.getNodeIds();
		
		for (int target : targetNodes) {
			SPTEntry goalEdge = targets.get(target);
			SPTEntry prevEdge = null;
			time = 0.0;
			distance = 0.0;
			weight = 0.0;

			if (goalEdge != null) {
				// correct values for the last edge
				if (EdgeIterator.Edge.isValid(goalEdge.edge))
				{
					if (goalEdge.parent != null && goalEdge.parent.adjNode != EdgeIterator.NO_EDGE)
					{
						ClosestEdgeData closestEdge = dstData.getClosestEdge(targetIndex);
						EdgeIteratorState iter = closestEdge.getEdge(goalEdge.adjNode, goalEdge.parent.adjNode);
						if (iter != null)
						{
							if (calcDistance)
								distance += (_distUnits == DistanceUnit.Meters) ? iter.getDistance() : DistanceUnitUtil.convert(iter.getDistance(), DistanceUnit.Meters, _distUnits);
							if (calcTime)
								time += _timeWeighting.calcMillis(iter, false, EdgeIterator.NO_EDGE) / 1000.0;
							if (calcWeight)
								weight += _weighting.calcWeight(iter, false, EdgeIterator.NO_EDGE);
						}
					}
				}
				
				while (EdgeIterator.Edge.isValid(goalEdge.edge)) {
					if (_chGraph != null)
					{
						CHEdgeIteratorState iter = _chGraph.getEdgeIteratorState(goalEdge.edge, goalEdge.adjNode);

						if (calcDistance)
							distance += (_distUnits == DistanceUnit.Meters) ? iter.getDistance(): DistanceUnitUtil.convert(iter.getDistance(), DistanceUnit.Meters, _distUnits);

						if (calcWeight || calcTime)
						{
							if (iter.isShortcut())
							{
								if (_chGraph.getLevel(iter.getBaseNode()) > _chGraph.getLevel(iter.getAdjNode()))
								{
									_reverseOrder = true;
									extractEdgeValues(iter, false);
								}
								else
								{
									_reverseOrder = false;
									extractEdgeValues(iter, true);
								}
							}
							else
							{
								extractEdgeValues(iter, false);
							}
							
							time += _edgeTime;
							weight += _edgeWeight;
						}
					}
					else
					{
						EdgeIteratorState iter = _graph.getEdgeIteratorState(goalEdge.edge, goalEdge.adjNode);
						//System.out.print(Integer.toString(iter.getEdge()) + ",");
						
						if (calcDistance)
							distance += (_distUnits == DistanceUnit.Meters) ? iter.getDistance(): DistanceUnitUtil.convert(iter.getDistance(), DistanceUnit.Meters, _distUnits);

						if (calcTime)
							time += _timeWeighting.calcMillis(iter, false, EdgeIterator.NO_EDGE) / 1000.0;

						if (calcWeight)
							weight += _weighting.calcWeight(iter, false, EdgeIterator.NO_EDGE);
					}

					prevEdge = goalEdge;
					goalEdge = goalEdge.parent;
				}
				
				// correct values for the first edge
				if (prevEdge != null && EdgeIterator.Edge.isValid(prevEdge.edge))
				{
					if (goalEdge.adjNode != EdgeIterator.NO_EDGE)
					{
						ClosestEdgeData closestEdge = srcData.getClosestEdge(sourceIndex);
						EdgeIteratorState iter = closestEdge.getEdge(prevEdge.adjNode, goalEdge.adjNode);
						if (iter != null)
						{
							if (calcDistance)
								distance += (_distUnits == DistanceUnit.Meters) ? iter.getDistance() : DistanceUnitUtil.convert(iter.getDistance(), DistanceUnit.Meters, _distUnits);
							if (calcTime)
								time += _timeWeighting.calcMillis(iter, false, EdgeIterator.NO_EDGE) / 1000.0;

							if (calcWeight)
								weight += _weighting.calcWeight(iter, false, EdgeIterator.NO_EDGE);
						}
					}
				}
			}
			else
			{
				time = -1;
				distance= -1;
				weight = -1;
			}
			
			if (calcTime)
				times[index] = (float)time;

			if (calcDistance)
				distances[index] = (float)distance;

			if (calcWeight)
				weights[index] = (float)weight;

			index++;
			targetIndex++;
		}
	}

	private void extractEdgeValues(CHEdgeIteratorState iterState, boolean reverse)
	{
		if (iterState.isShortcut())
		{
			_edgeTime = 0.0;
			_edgeWeight = 0.0;

			if ((_chGraph.getLevel(iterState.getBaseNode()) <= _chGraph.getLevel(iterState.getAdjNode())))
				reverse = !reverse; 
			
			expandEdge(iterState, reverse);  
		}
		else
		{
			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Duration))
				_edgeTime = _weighting.calcMillis(iterState, reverse, EdgeIterator.NO_EDGE) / 1000.0;
			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Weight))
				_edgeWeight = _weighting.calcWeight(iterState, reverse, EdgeIterator.NO_EDGE);
		}
	}

	private void expandEdge(CHEdgeIteratorState mainEdgeState, boolean reverse) {
		if (!mainEdgeState.isShortcut()) {
			//System.out.print(Integer.toString(mainEdgeState.getEdge()) + ",");

			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Duration))
				_edgeTime += _weighting.calcMillis(mainEdgeState, reverse, EdgeIterator.NO_EDGE) / 1000.0;
			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Weight))
				_edgeWeight +=_weighting.calcWeight(mainEdgeState, reverse, EdgeIterator.NO_EDGE);
			return;
		}
	
		int skippedEdge1 = mainEdgeState.getSkippedEdge1();
		int skippedEdge2 = mainEdgeState.getSkippedEdge2();
		int from = mainEdgeState.getBaseNode(), to = mainEdgeState.getAdjNode();

		// get properties like speed of the edge in the correct direction
		if (reverse) {
			int tmp = from;
			from = to;
			to = tmp;
		}

		// getEdgeProps could possibly return an empty edge if the shortcut is available for both directions
		if (_reverseOrder) {
			CHEdgeIteratorState edgeState = (CHEdgeIteratorState) _chGraph.getEdgeIteratorState(skippedEdge1, to);
			boolean empty = edgeState == null;
			if (empty)
				edgeState = (CHEdgeIteratorState) _chGraph.getEdgeIteratorState(skippedEdge2, to);

			expandEdge(edgeState, false);

			if (empty)
				edgeState = (CHEdgeIteratorState) _chGraph.getEdgeIteratorState(skippedEdge1, from);
			else
				edgeState = (CHEdgeIteratorState) _chGraph.getEdgeIteratorState(skippedEdge2, from);

			expandEdge(edgeState, true);
		} 
		else
		{
			CHEdgeIteratorState iter = (CHEdgeIteratorState) _chGraph.getEdgeIteratorState(skippedEdge1, from);
			boolean empty = iter == null;
			if (empty)
				iter = (CHEdgeIteratorState) _chGraph.getEdgeIteratorState(skippedEdge2, from);

			expandEdge(iter, true);

			if (empty)
				iter = (CHEdgeIteratorState) _chGraph.getEdgeIteratorState(skippedEdge1, to);
			else
				iter = (CHEdgeIteratorState) _chGraph.getEdgeIteratorState(skippedEdge2, to);

			expandEdge(iter, false);
		} 
	}
}
