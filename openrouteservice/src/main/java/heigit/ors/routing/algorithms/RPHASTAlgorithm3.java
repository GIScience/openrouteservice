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
import com.graphhopper.coll.GHIntHashSet;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.CHLevelEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import heigit.ors.util.DebugUtility;

public class RPHASTAlgorithm3 extends AbstractOneToManyRoutingAlgorithm {
	private IntObjectMap<SPTEntry> _bestWeightMapFrom;
	private SPTEntry _currFrom;
	private SPTEntry _currTo;
	private PriorityQueue<SPTEntry> _prioQueue;
	private CHLevelEdgeFilter _upwardEdgeFilter;
	private CHLevelEdgeFilter _downwardEdgeFilter;
	private SubGraph _sourceGraph;
	private SubGraph _targetGraph;
	private boolean _finishedFrom;
	private boolean _finishedTo;
	private int _visitedCountFrom;
	private int _visitedCountTo;

	public RPHASTAlgorithm3(Graph graph, Weighting weighting, TraversalMode traversalMode) {
		super(graph, weighting, traversalMode);

		int size = Math.min(Math.max(200, graph.getNodes() / 10), 2000);

		initCollections(size);

		CHGraph chGraph = null;
		if (graph instanceof CHGraph)
			chGraph = (CHGraph)graph;
		else if (graph instanceof QueryGraph)
		{
			QueryGraph qGraph = (QueryGraph)graph;
			chGraph = (CHGraph)qGraph.getMainGraph();
		}

		setMaxVisitedNodes(Integer.MAX_VALUE);
		FlagEncoder encoder = weighting.getFlagEncoder();

		_upwardEdgeFilter = new CHLevelEdgeFilter(chGraph, encoder);
		_downwardEdgeFilter = new CHLevelEdgeFilter(chGraph, encoder);
		_downwardEdgeFilter.setBackwardSearch(true);

		inEdgeExplorer = graph.createEdgeExplorer();
		outEdgeExplorer =  graph.createEdgeExplorer();
	}

	protected void initCollections(int size) {
		_prioQueue = new PriorityQueue<SPTEntry>(size);
		_bestWeightMapFrom = new GHIntObjectHashMap<SPTEntry>(size);
	}

	@Override
	public void reset()
	{
		_finishedFrom = false;
		_finishedTo = false;
		_prioQueue.clear();
		_bestWeightMapFrom.clear();
	}

	@Override
	public void prepare(int[] sources, int[] targets) {
		PriorityQueue<Integer> prioQueue = new PriorityQueue<>(100);

		// Phase I: build shortest path tree from all target nodes to the highest node
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

		// Phase II: build a graph from all source nodes to the highest node
		_sourceGraph = new SubGraph(graph);
		GHIntHashSet nodesSkip = new GHIntHashSet();
		/*nodesSkip.add(sources[0]);
		nodesSkip.add(sources[1]);
		nodesSkip.add(sources[2]);
		nodesSkip.add(sources[3]);*/

		addNodes(_sourceGraph, prioQueue, sources);

		while (!prioQueue.isEmpty()) {
			int adjNode = prioQueue.poll();
			EdgeIterator iter = outEdgeExplorer.setBaseNode(adjNode);

			while (iter.next()) {
				if (!_upwardEdgeFilter.accept(iter))
					continue;

				_upwardEdgeFilter.updateHighestNode(iter);

				_sourceGraph.addEdge(adjNode, iter, false);

				if (!nodesSkip.contains(iter.getAdjNode()))
				{
					nodesSkip.add(iter.getAdjNode());
					prioQueue.add(iter.getAdjNode());
				}
			}
		} 

		if (DebugUtility.isDebug())
			_sourceGraph.print();
	}

	private void addNodes(SubGraph graph, PriorityQueue<Integer> prioQueue, int[] nodes)
	{
		for (int i = 0; i < nodes.length; i++) 
		{
			int nodeId = nodes[i];
			if (nodeId >= 0)
			{
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
	public SPTEntry[] calcPaths(int from, int[] to) {
		SPTEntry entry = createSPTEntry(from, 0.0);
		entry.visited = true;
		_prioQueue.add(entry);
		_bestWeightMapFrom.put(from, entry);

		outEdgeExplorer = _sourceGraph.createExplorer();
		
		runUpwardSearch();
		
		entry = _bestWeightMapFrom.get(_upwardEdgeFilter.getHighestNode());
		entry.visited = true;
		_prioQueue.add(entry);
		
		outEdgeExplorer = _targetGraph.createExplorer();
		
		runDownwardSearch();

		SPTEntry[] targets = new SPTEntry[to.length];

		for (int i = 0; i < to.length; i++)
			targets[i] = _bestWeightMapFrom.get(to[i]);

		return targets; 
	}

	private void fillEdgesUpward(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue, IntObjectMap<SPTEntry> shortestWeightMap,
			EdgeExplorer explorer) {
		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null) // we reach one of the target nodes
			return;

		while (iter.next()) {
			double tmpWeight = weighting.calcWeight(iter, false, currEdge.edge) + currEdge.weight;

			if (Double.isInfinite(tmpWeight)) 
				continue;

			SPTEntry ee = shortestWeightMap.get(iter.getAdjNode());

			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(iter.getAdjNode(), ee);
				prioQueue.add(ee);
			} else if (ee.weight > tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge;
				prioQueue.add(ee);
			} 
		}
	}

	private void fillEdgesDownward(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue,
			IntObjectMap<SPTEntry> shortestWeightMap, EdgeExplorer explorer) {

		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null)
			return;

		while (iter.next())
		{
			double tmpWeight = weighting.calcWeight(iter, false, currEdge.edge) + currEdge.weight;
			if (Double.isInfinite(tmpWeight))
				continue;

			SPTEntry ee = shortestWeightMap.get(iter.getAdjNode());

			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(iter.getAdjNode(), ee);
				prioQueue.add(ee);
				ee.visited = true; 
			} else if (ee.weight > tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge; 
				prioQueue.add(ee);
			}
			else if (ee.visited == false) {
				// // This is the case if the node has been assigned a weight in
				// // the upwards pass (fillEdges). We need to use it in the
				// // downwards pass to access lower level nodes, though the
				// weight
				// // does not have to be reset necessarily
				// prioQueue.remove(ee);
				//
				prioQueue.add(ee);
				ee.visited = true;
			} 
		}
	}
}
