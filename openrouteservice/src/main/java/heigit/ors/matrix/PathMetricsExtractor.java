package heigit.ors.matrix;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

public class PathMetricsExtractor {
	private int _metrics;
	private Graph _graph;
	private CHGraph _chGraph;
	private Weighting _weighting;
	private Weighting _timeWeighting;
	private double _edgeWeight;
	private double _edgeTime;
	private FlagEncoder _encoder;
	private boolean reverseOrder = true;

	public PathMetricsExtractor(int metrics, GraphHopperStorage graphStorage, FlagEncoder encoder, Weighting weighting)
	{
		_metrics = metrics;
		_graph = graphStorage.getBaseGraph();
		_encoder = encoder;
		_weighting = weighting;
		_timeWeighting = new FastestWeighting(encoder);
		
		if (graphStorage.isCHPossible())
			_chGraph = (CHGraph)graphStorage.getGraph(CHGraph.class /* "fastest"*/);
	}
	
	public void setEmptyValues(int sourceIndex, MatrixLocationData srcData, MatrixLocationData dstData, float[] times, float[] distances, float[] weights)
	{
		int i = sourceIndex;
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
	
	public void calcValues(int sourceIndex, IntObjectMap<SPTEntry> targets, MatrixLocationData srcData, MatrixLocationData dstData, float[] times, float[] distances, float[] weights)
	{
		if (targets == null)
			throw new IllegalStateException("Target destinations not set");

		int i = sourceIndex;
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
						CHEdgeIteratorState iter = _chGraph.getEdgeIteratorState(goalEdge.edge, goalEdge.adjNode);

						if (calcDistance)
							distance += iter.getDistance();

						if (calcWeight || calcTime)
						{
							//eturn !(graph.getLevel(base) <= graph.getLevel(adj)) ? false : true;
							if (iter.isShortcut() && _chGraph.getLevel(iter.getBaseNode()) > _chGraph.getLevel(iter.getAdjNode()))
							{
								reverseOrder = true;//_chGraph.getLevel(iter.getBaseNode())  > _chGraph.getLevel(iter.getAdjNode());
								//reverseOrder = iter.getBaseNode() > iter.getAdjNode();//iter.isBackward(_encoder);//iter.getBaseNode() > iter.getAdjNode();
								extractEdgeValues(iter, false);
							}
							else
							{
								_edgeTime = 0;
								_edgeWeight = 0; 
								//reverseOrder = true;
								//extractEdgeValues(iter, true);
							}
							
							//System.out.print(Integer.toString(iter.getEdge()) + ",");
							time += _edgeTime;
							weight += _edgeWeight;
						}
					}
					else
					{
						EdgeIteratorState iter = _graph.getEdgeIteratorState(goalEdge.edge, goalEdge.adjNode);
						
						if (calcTime)
							time += _timeWeighting.calcMillis(iter, false, EdgeIterator.NO_EDGE) / 1000.0;

						if (calcDistance)
							distance += iter.getDistance();

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
				times[i] = (float)time;

			if (calcDistance)
				distances[i] = (float)distance;

			if (calcWeight)
				weights[i] = (float)weight;

			i++;
		}
	}

	private void extractEdgeValues(CHEdgeIteratorState iterState, boolean reverse)
	{
		_edgeTime = 0.0;
		_edgeWeight = 0.0;
		
		if (iterState.isShortcut())
		{
			expandEdge(iterState, reverse);
			//_edgeWeight = iterState.getWeight();
			//_edgeTime = iterState.getWeight();
			//System.out.print(Double.toString(iterState.getDistance()) + ",");
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
			System.out.print(Integer.toString(mainEdgeState.getEdge()) + ",");

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
		if (reverseOrder) {
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
		}  else
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
