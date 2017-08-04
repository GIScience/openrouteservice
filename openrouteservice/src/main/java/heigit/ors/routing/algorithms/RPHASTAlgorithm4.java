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
package heigit.ors.routing.algorithms;

import java.util.Arrays;
import java.util.PriorityQueue;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.CHLevelEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;

public class RPHASTAlgorithm4 extends AbstractManyToManyRoutingAlgorithm {
	private IntObjectMap<MultiTreeSPEntry> _bestWeightMapFrom;
	private MultiTreeSPEntry _currFrom;
	private MultiTreeSPEntry _currTo;
	private PriorityQueue<MultiTreeSPEntry> _prioQueue;
	private CHLevelEdgeFilter _upwardEdgeFilter;
	private CHLevelEdgeFilter _downwardEdgeFilter;
	private SubGraph _sourceGraph;
	private SubGraph _targetGraph;
	private boolean _finishedFrom;
	private boolean _finishedTo;
	private int _visitedCountFrom;
	private int _visitedCountTo;
	private int numTrees;

	public RPHASTAlgorithm4(Graph graph, Weighting weighting, TraversalMode traversalMode) {
		super(graph, weighting, traversalMode);

		int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);

		initCollections(size);

		CHGraph chGraph = null;
		if (graph instanceof CHGraph)
			chGraph = (CHGraph) graph;
		else if (graph instanceof QueryGraph) {
			QueryGraph qGraph = (QueryGraph) graph;
			chGraph = (CHGraph) qGraph.getMainGraph();
		}

		setMaxVisitedNodes(Integer.MAX_VALUE);
		FlagEncoder encoder = weighting.getFlagEncoder();

		_upwardEdgeFilter = new CHLevelEdgeFilter(chGraph, encoder);
		_downwardEdgeFilter = new CHLevelEdgeFilter(chGraph, encoder);
		_downwardEdgeFilter.setBackwardSearch(true);

		inEdgeExplorer = graph.createEdgeExplorer();
		outEdgeExplorer = graph.createEdgeExplorer();
	}

	protected void initCollections(int size) {
		_prioQueue = new PriorityQueue<MultiTreeSPEntry>(size);
		_bestWeightMapFrom = new GHIntObjectHashMap<MultiTreeSPEntry>(size);
	}

	@Override
	public void reset() {
		_finishedFrom = false;
		_finishedTo = false;
		_prioQueue.clear();
		_bestWeightMapFrom.clear();
	}

	@Override
	public void prepare(int[] sources, int[] targets) {
		_targetGraph = new SubGraph(graph);
		this.numTrees = sources.length;
		PriorityQueue<Integer> prioQueue = new PriorityQueue<>(200);

		addNodes(_targetGraph, prioQueue, targets);

		while (!prioQueue.isEmpty()) {
			int adjNode = prioQueue.poll();
			EdgeIterator iter = outEdgeExplorer.setBaseNode(adjNode);

			while (iter.next()) {
				if (!_downwardEdgeFilter.accept(iter))
					continue;

				if (_targetGraph.addEdge(adjNode, iter, true))
					prioQueue.add(iter.getAdjNode());
			}
		}

		addNodes(null, prioQueue, sources);

		_targetGraph.print();

	}

	private void addNodes(SubGraph graph, PriorityQueue<Integer> prioQueue, int[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			int nodeId = nodes[i];
			if (nodeId >= 0) {
				if (graph != null)
					graph.addEdge(nodeId, null, true);
				prioQueue.add(nodeId);
			}
		}
	}

	protected void runUpwardSearch() {
		while (!isMaxVisitedNodesExceeded() && !_finishedFrom) {
			_finishedFrom = !upwardSearch();
		}
	}

	protected void runDownwardSearch() {
		while (!_finishedTo) {
			_finishedTo = !downwardSearch();
		}
	}

	@Override
	public int getVisitedNodes() {
		return _visitedCountFrom + _visitedCountTo;
	}

	private boolean upwardSearch() {
		if (_prioQueue.isEmpty())
			return false;

		_currFrom = _prioQueue.poll();
		fillEdgesUpward(_currFrom, _prioQueue, _bestWeightMapFrom, outEdgeExplorer);
		_visitedCountFrom++;

		return true;
	}

	private boolean downwardSearch() {
		if (_prioQueue.isEmpty())
			return false;

		_currTo = _prioQueue.poll();
		fillEdgesDownward(_currTo, _prioQueue, _bestWeightMapFrom, outEdgeExplorer);
		_visitedCountTo++;

		return true;
	}

	@Override
	public MultiTreeSPEntry[] calcPaths(int[] from, int[] to) {
		init(from, new double[to.length]);

		// outEdgeExplorer = _sourceGraph.createExplorer();

		runUpwardSearch();
		initDownwardSearch(_upwardEdgeFilter.getHighestNode());
		// MultiTreeSPEntry entry =
		// _bestWeightMapFrom.get(_upwardEdgeFilter.getHighestNode());
		// entry.visited = true;
		// _prioQueue.add(entry);
		outEdgeExplorer = _targetGraph.createExplorer();

		runDownwardSearch();

		MultiTreeSPEntry[] targets = new MultiTreeSPEntry[to.length];

		for (int i = 0; i < to.length; i++)
			targets[i] = _bestWeightMapFrom.get(to[i]);

		return targets;
	}

	public IntObjectMap<MultiTreeSPEntry> init(int[] from, double[] weights) {
		int[] edgeIds = new int[from.length];
		Arrays.fill(edgeIds, EdgeIterator.NO_EDGE);

		for (int i = 0; i < from.length; i++) {
			double[] newWeights = new double[from.length];
			Arrays.fill(newWeights, -1);
			newWeights[i] = 0;
			_currFrom = new MultiTreeSPEntry(edgeIds, from[i], newWeights);
			_currFrom.visited = true;
			_prioQueue.add(_currFrom);

			if (!traversalMode.isEdgeBased()) {
				_bestWeightMapFrom.put(from[i], _currFrom);
			} else
				throw new IllegalStateException("Edge-based behavior not supported");

		}
		return _bestWeightMapFrom;

	}

	private void initDownwardSearch(int to) {
		// Set the highest node from the upwards pass as the start node for the
		// downwards pass. Keep parent refs for future use in distance
		// extraction.
		_currTo = _bestWeightMapFrom.get(to);
		_currTo.visited = true;
		_prioQueue.clear();
		_prioQueue.add(_currTo);
		if (!traversalMode.isEdgeBased()) {
			_bestWeightMapFrom.put(to, _currTo);
		} else if (_currFrom != null && _currFrom.adjNode == to) {
			// special case of identical start and end
			// bestPath.sptEntry = currFrom;
			// bestPath.edgeTo = currTo;
			_finishedFrom = true;
			_finishedTo = true;
		}
	}

	private void fillEdgesUpward(MultiTreeSPEntry currEdge, PriorityQueue<MultiTreeSPEntry> prioQueue,
			IntObjectMap<MultiTreeSPEntry> shortestWeightMap, EdgeExplorer explorer) {
		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null) // we reach one of the target nodes
			return;

		while (iter.next()) {
			if (!_upwardEdgeFilter.accept(iter))
				continue;
			// if (!additionalEdgeFilter.accept(iter))
			// continue;
			// Keep track of the currently found highest CH level node
			// additionalEdgeFilter.updateHighestNode(iter);
			_upwardEdgeFilter.updateHighestNode(iter);

			MultiTreeSPEntry ee = shortestWeightMap.get(iter.getAdjNode());
			if (ee == null) {
				int[] edgeIds = new int[numTrees];
				Arrays.fill(edgeIds, EdgeIterator.NO_EDGE);
				ee = new MultiTreeSPEntry(edgeIds, iter.getAdjNode(), numTrees);
				// double edgeWeight = weighting.calcWeight(iter, reverse,
				// currEdge.edge);
				// double edgeWeight = weighting.calcWeight(iter, false, -1);
				double tmpWeight;
				for (int i = 0; i < numTrees; i++) {
					if (currEdge.weights[i] == -1)
						continue;

					tmpWeight = weighting.calcWeight(iter, false, currEdge.edge[i]) + currEdge.weights[i];
					if (Double.isInfinite(tmpWeight))
						continue;
					ee.weights[i] = tmpWeight;
					ee.parent[i] = currEdge;
					ee.edge[i] = iter.getEdge();
				}
				shortestWeightMap.put(iter.getAdjNode(), ee);
				prioQueue.add(ee);

			} else {
				// double edgeWeight = weighting.calcWeight(iter, reverse,
				// currEdge.edge);
				double edgeWeight = weighting.calcWeight(iter, false, -1);
				double tmpWeight;

				boolean addToQ = false;
				for (int i = 0; i < numTrees; i++) {
					if (currEdge.weights[i] == -1)
						continue;

					tmpWeight = weighting.calcWeight(iter, false, currEdge.edge[i]) + currEdge.weights[i];
					if (Double.isInfinite(tmpWeight))
						continue;
					if (ee.weights[i] > tmpWeight || ee.weights[i] == -1) {
						ee.weights[i] = tmpWeight;
						ee.edge[i] = iter.getEdge();
						ee.parent[i] = currEdge;
						addToQ = true;
					}

				}
				if (addToQ) {
					prioQueue.remove(ee);
					// ee.edge = iter.getEdge();
					// TODO: One parent pointer for each start point?
					// ee.parent = currEdge;
					prioQueue.add(ee);
				}

			}

		}
	}

	private void fillEdgesDownward(MultiTreeSPEntry currEdge, PriorityQueue<MultiTreeSPEntry> prioQueue,
			IntObjectMap<MultiTreeSPEntry> shortestWeightMap, EdgeExplorer explorer) {

		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null)
			return;

		while (iter.next()) {
			// no need in filter, since all edges in targetGraph are valid and
			// acceptable
			// if (!additionalEdgeFilter.accept(iter))
			// continue;

			MultiTreeSPEntry ee = shortestWeightMap.get(iter.getAdjNode());

			if (ee == null) {
				int[] edgeIds = new int[numTrees];
				Arrays.fill(edgeIds, EdgeIterator.NO_EDGE);
				ee = new MultiTreeSPEntry(edgeIds, iter.getAdjNode(), numTrees);
				// ee.parent = currEdge;
				ee.visited = true;
				double edgeWeight = weighting.calcWeight(iter, false, -1);
				double tmpWeight;
				for (int i = 0; i < numTrees; i++) {
					if (currEdge.weights[i] == -1)
						continue;

					tmpWeight = weighting.calcWeight(iter, false, currEdge.edge[i]) + currEdge.weights[i];
					if (Double.isInfinite(tmpWeight))
						continue;
					ee.weights[i] = tmpWeight;
					ee.parent[i] = currEdge;
					ee.edge[i] = iter.getEdge();

				}
				shortestWeightMap.put(iter.getAdjNode(), ee);

				prioQueue.add(ee);
			} else {
				// double edgeWeight = weighting.calcWeight(iter, false, -1);
				double tmpWeight;

				boolean addToQ = false;

				for (int i = 0; i < numTrees; i++) {
					if (currEdge.weights[i] == -1)
						continue;

					tmpWeight = weighting.calcWeight(iter, false, currEdge.edge[i]) + currEdge.weights[i];
					if (Double.isInfinite(tmpWeight))
						continue;
					if (ee.weights[i] > tmpWeight || ee.weights[i] == -1) {
						ee.weights[i] = tmpWeight;
						ee.edge[i] = iter.getEdge();
						ee.parent[i] = currEdge;
						addToQ = true;
					}

				}
				if (ee.visited == false) {
					// // This is the case if the node has been assigned a
					// weight in
					// // the upwards pass (fillEdges). We need to use it in
					// the
					// // downwards pass to access lower level nodes, though
					// the
					// weight
					// // does not have to be reset necessarily
					// prioQueue.remove(ee);
					//
					ee.visited = true;
					prioQueue.add(ee);
				}
				if (addToQ) {
					prioQueue.remove(ee);
					// ee.edge = iter.getEdge();
					// TODO: One parent pointer for each start point?
					// ee.parent = currEdge;
					ee.visited = true;
					prioQueue.add(ee);

				}

			}
		}
	}
}
