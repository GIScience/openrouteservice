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
import com.graphhopper.util.StopWatch;

import heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;
import heigit.ors.util.DebugUtility;

public class RPHASTAlgorithm4 extends AbstractManyToManyRoutingAlgorithm {
	private IntObjectMap<MultiTreeSPEntry> _bestWeightMapFrom;
	private MultiTreeSPEntry _currFrom;
	private MultiTreeSPEntry _currTo;
	private PriorityQueue<MultiTreeSPEntry> _prioQueue;
	private CHLevelEdgeFilter _upwardEdgeFilter;
	private CHLevelEdgeFilter _downwardEdgeFilter;
	private SubGraph _targetGraph;
	private boolean _finishedFrom;
	private boolean _finishedTo;
	private int _visitedCountFrom;
	private int _visitedCountTo;
	private int _treeEntrySize;

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
		PriorityQueue<Integer> prioQueue = new PriorityQueue<>(100);
		this._treeEntrySize = sources.length;

		// Phase I: build shortest path tree from all target nodes to the
		// highest node
		_targetGraph = new SubGraph(graph);

		addNodes(_targetGraph, prioQueue, targets);

		while (!prioQueue.isEmpty()) {
			int adjNode = prioQueue.poll();
			EdgeIterator iter = outEdgeExplorer.setBaseNode(adjNode);

			while (iter.next()) {
				if (!_downwardEdgeFilter.accept(iter))
					continue;

				_downwardEdgeFilter.updateHighestNode(iter);

				if (_targetGraph.addEdge(adjNode, iter, true))
					prioQueue.add(iter.getAdjNode());
			}
		}

		if (DebugUtility.isDebug())
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
		for (int i = 0; i < from.length; i++) {
			_currFrom = new MultiTreeSPEntry(from[i], from.length);
			_currFrom.weights[i] = 0.0001;
			_currFrom.visited = true;
			_prioQueue.add(_currFrom);

			if (!traversalMode.isEdgeBased()) {
				_bestWeightMapFrom.put(from[i], _currFrom);
			} else
				throw new IllegalStateException("Edge-based behavior not supported");
		}

		outEdgeExplorer = graph.createEdgeExplorer();
		StopWatch sw = new StopWatch();
		sw.start();

		runUpwardSearch();

		sw.stop();
		System.out.println(sw.getTime());
		_currFrom = _bestWeightMapFrom.get(_upwardEdgeFilter.getHighestNode());
		_currFrom.visited = true;
		_prioQueue.clear();
		_prioQueue.add(_currFrom);

		outEdgeExplorer = _targetGraph.createExplorer();
		sw = new StopWatch();
		sw.start();
		runDownwardSearch();
		sw.stop();
		System.out.println(sw.getTime());

		System.out.print(upwardIters + " " + downwardIters);

		MultiTreeSPEntry[] targets = new MultiTreeSPEntry[to.length];

		for (int i = 0; i < to.length; i++)
			targets[i] = _bestWeightMapFrom.get(to[i]);

		return targets;
	}

	private int downwardIters = 0;
	private int upwardIters = 0;

	private void fillEdgesUpward(MultiTreeSPEntry currEdge, PriorityQueue<MultiTreeSPEntry> prioQueue,
			IntObjectMap<MultiTreeSPEntry> shortestWeightMap, EdgeExplorer explorer) {
		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null) // we reach one of the target nodes
			return;

		boolean canMergeTrees = false, addToQ = false;
		int nonEmptyValues = 0;
		double edgeWeight, entryWeight, tmpWeight;

		while (iter.next()) {
			if (!_upwardEdgeFilter.accept(iter))
				continue;

			_upwardEdgeFilter.updateHighestNode(iter);

			edgeWeight = weighting.calcWeight(iter, false, 0);

			if (!Double.isInfinite(edgeWeight))
			{
				MultiTreeSPEntry ee = shortestWeightMap.get(iter.getAdjNode());

				if (ee == null) {
					ee = new MultiTreeSPEntry(iter.getAdjNode(), _treeEntrySize);
					
					for (int i = 0; i < _treeEntrySize; i++) {
						entryWeight = currEdge.weights[i];
						if (entryWeight == 0.0)
							continue;

						ee.weights[i] = edgeWeight + entryWeight;
						ee.parent[i] = currEdge;
						ee.edge[i] = iter.getEdge();
					}
					
					shortestWeightMap.put(iter.getAdjNode(), ee);
					prioQueue.add(ee);
				} else {
					addToQ = false;
					nonEmptyValues = 0;

					for (int i = 0; i < _treeEntrySize; i++) {
						entryWeight = currEdge.weights[i];
						if (entryWeight == 0.0)
							continue;

						tmpWeight = edgeWeight + entryWeight;

						if (ee.weights[i] > tmpWeight || ee.weights[i] == 0.0) {
							ee.weights[i] = tmpWeight;
							ee.edge[i] = iter.getEdge();
							ee.parent[i] = currEdge;
							addToQ = true;
						}

						nonEmptyValues++;
					}

					if (addToQ) 
					{
						prioQueue.remove(ee);
						prioQueue.add(ee);
					}
					
					if (nonEmptyValues == _treeEntrySize && _targetGraph.containsNode(iter.getAdjNode()))
						canMergeTrees = true;
				}
			}

			upwardIters++;
		}

		if (canMergeTrees)
			prioQueue.clear();
	}

	private void fillEdgesDownward(MultiTreeSPEntry currEdge, PriorityQueue<MultiTreeSPEntry> prioQueue,
			IntObjectMap<MultiTreeSPEntry> shortestWeightMap, EdgeExplorer explorer) {

		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null)
			return;

		double edgeWeight, entryWeight, tmpWeight;
		boolean addToQ = false;

		while (iter.next()) {
			edgeWeight = weighting.calcWeight(iter, false, 0);

			if (!Double.isInfinite(edgeWeight))
			{
				MultiTreeSPEntry ee = shortestWeightMap.get(iter.getAdjNode());

				if (ee == null) {
					ee = new MultiTreeSPEntry(iter.getAdjNode(), _treeEntrySize);
					ee.visited = true;

					for (int i = 0; i < _treeEntrySize; ++i) {
						entryWeight = currEdge.weights[i];
						if (entryWeight == 0.0)
							continue;

						ee.weights[i] = edgeWeight + entryWeight;
						ee.parent[i] = currEdge;
						ee.edge[i] = iter.getEdge();
					}

					shortestWeightMap.put(iter.getAdjNode(), ee);
					prioQueue.add(ee);
				}
				else {
					addToQ = false;

					for (int i = 0; i < _treeEntrySize; ++i) {
						entryWeight = currEdge.weights[i];
						if (entryWeight == 0.0) 
							continue;

						tmpWeight = edgeWeight + entryWeight;

						if (ee.weights[i] > tmpWeight || ee.weights[i] == 0.0) {
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
						// // does not have to be reset necessarily //
						ee.visited = true;

						prioQueue.add(ee);
					}
					else if (addToQ) {

						ee.visited = true;
						prioQueue.remove(ee);
						prioQueue.add(ee);
					}
				}
			}

			downwardIters++;
		}
	}
}
