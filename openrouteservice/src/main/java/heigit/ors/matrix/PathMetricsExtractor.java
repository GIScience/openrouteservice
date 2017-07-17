package heigit.ors.matrix;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.common.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;

public class PathMetricsExtractor {
	private int _metrics;
	private Graph _graph;
	private CHGraph _chGraph;
	private Weighting _weighting;
	private Weighting _timeWeighting;
	private double _edgeDistance;
	private double _edgeWeight;
	private double _edgeTime;
	private DistanceUnit _distUnits;
	private boolean _reverseOrder = true;
	private boolean _unpackDistance = true;

	public PathMetricsExtractor(int metrics, Graph graph, FlagEncoder encoder, Weighting weighting, DistanceUnit units)
	{
		_metrics = metrics;
		_graph = graph;
		_weighting = weighting;
		_timeWeighting = new FastestWeighting(encoder);
		_distUnits = units;
		
		if (graph instanceof CHGraph)
			_chGraph = (CHGraph)graph;
		else if (graph instanceof QueryGraph)
		{
			QueryGraph qGraph = (QueryGraph)graph;
			Graph mainGraph = qGraph.getMainGraph();
			if (mainGraph instanceof CHGraph)
				_chGraph = (CHGraph)mainGraph;
		}
	}
	
	public void setEmptyValues(int sourceIndex, MatrixLocations srcData, MatrixLocations dstData, float[] times, float[] distances, float[] weights)
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
	
	public void calcValues(int sourceIndex, IntObjectMap<SPTEntry> targets, MatrixLocations srcData, MatrixLocations dstData, float[] times, float[] distances, float[] weights) throws Exception
	{
		if (targets == null)
			throw new IllegalStateException("Target destinations not set"); 

		int index = sourceIndex * dstData.size();
		double time = 0.0;
		double distance = 0.0;
		double weight = 0.0;
		boolean calcTime = MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Duration);
		boolean calcDistance = MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Distance);
		boolean calcWeight = MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Weight);
		
		int[] targetNodes = dstData.getNodeIds();
		
		for (int target : targetNodes) {
			SPTEntry goalEdge = targets.get(target);
			time = 0.0;
			distance = 0.0;
			weight = 0.0;
 
			if (goalEdge != null) {
				while (EdgeIterator.Edge.isValid(goalEdge.edge)) {
					if (_chGraph != null)
					{ 
						CHEdgeIteratorState iterState = (CHEdgeIteratorState)_graph.getEdgeIteratorState(goalEdge.edge, goalEdge.adjNode);
  
						if (!_unpackDistance && calcDistance)
							distance += (_distUnits == DistanceUnit.Meters) ? iterState.getDistance(): DistanceUnitUtil.convert(iterState.getDistance(), DistanceUnit.Meters, _distUnits);

						if (calcWeight || calcTime || _unpackDistance)
						{
							if (iterState.isShortcut())
							{
								if (_chGraph.getLevel(iterState.getBaseNode()) > _chGraph.getLevel(iterState.getAdjNode()))
								{
									_reverseOrder = true;
									extractEdgeValues(iterState, false);
								}
								else
								{
									_reverseOrder = false;
									extractEdgeValues(iterState, true);
								}
							}
							else
							{
								extractEdgeValues(iterState, false);
							}
							
							if (_unpackDistance)
								distance += (_distUnits == DistanceUnit.Meters) ? _edgeDistance : DistanceUnitUtil.convert(_edgeDistance, DistanceUnit.Meters, _distUnits);
							
							time += _edgeTime;
							weight += _edgeWeight;
						}
					}
					else
					{ 
						EdgeIteratorState iter = _graph.getEdgeIteratorState(goalEdge.edge, goalEdge.adjNode);
						 
						if (calcDistance)
							distance += (_distUnits == DistanceUnit.Meters) ? iter.getDistance(): DistanceUnitUtil.convert(iter.getDistance(), DistanceUnit.Meters, _distUnits);

						if (calcTime)
							time += _timeWeighting.calcMillis(iter, false, EdgeIterator.NO_EDGE) / 1000.0;

						if (calcWeight)
							weight += _weighting.calcWeight(iter, false, EdgeIterator.NO_EDGE);
					}

					goalEdge = goalEdge.parent;
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
		}
	}

	private void extractEdgeValues(CHEdgeIteratorState iterState, boolean reverse)
	{
		if (iterState.isShortcut())
		{
			_edgeDistance = 0.0;
			_edgeTime = 0.0;
			_edgeWeight = 0.0;
			
			if ((_chGraph.getLevel(iterState.getBaseNode()) < _chGraph.getLevel(iterState.getAdjNode())))
				reverse = !reverse;
			
			expandEdge(iterState, reverse);  
		}
		else
		{
			//System.out.println(iterState.getName() + " "+iterState.getDistance());
			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Distance))
				_edgeDistance = iterState.getDistance();
			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Duration))
				_edgeTime = _weighting.calcMillis(iterState, reverse, EdgeIterator.NO_EDGE) / 1000.0;
			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Weight))
				_edgeWeight = _weighting.calcWeight(iterState, reverse, EdgeIterator.NO_EDGE);
		}
	}

	private void expandEdge(CHEdgeIteratorState iterState, boolean reverse) {
		if (!iterState.isShortcut()) {
			//System.out.println(iterState.getName() + " "+iterState.getDistance());
			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Distance))
				_edgeDistance += iterState.getDistance();
			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Duration))
				_edgeTime += _weighting.calcMillis(iterState, reverse, EdgeIterator.NO_EDGE) / 1000.0;
			if (MatrixMetricsType.isSet(_metrics, MatrixMetricsType.Weight))
				_edgeWeight +=_weighting.calcWeight(iterState, reverse, EdgeIterator.NO_EDGE);
			return;
		}
	
		int skippedEdge1 = iterState.getSkippedEdge1();
		int skippedEdge2 = iterState.getSkippedEdge2();
		int from = iterState.getBaseNode(), to = iterState.getAdjNode();

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
