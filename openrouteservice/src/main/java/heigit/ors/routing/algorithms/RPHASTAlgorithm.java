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
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import heigit.ors.routing.graphhopper.extensions.edgefilters.DownwardSearchEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.edgefilters.UpwardSearchEdgeFilter;

public class RPHASTAlgorithm extends AbstractOneToManyRoutingAlgorithm {
	private IntObjectMap<SPTEntry> _bestWeightMapFrom;
	private SPTEntry _currFrom;
	private SPTEntry _currTo;
	private PriorityQueue<SPTEntry> _prioQueue;
	private UpwardSearchEdgeFilter _upwardEdgeFilter;
	private DownwardSearchEdgeFilter _downwardEdgeFilter;
	private SubGraph _targetGraph;
	private boolean _finishedFrom;
	private boolean _finishedTo;
	private int _visitedCountFrom;
	private int _visitedCountTo;
	
	public RPHASTAlgorithm(Graph graph, Weighting weighting, TraversalMode traversalMode) {
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
		
		_upwardEdgeFilter = new UpwardSearchEdgeFilter(chGraph, encoder);
		_downwardEdgeFilter = new DownwardSearchEdgeFilter(chGraph, encoder);
		
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
		_targetGraph = new SubGraph(graph);
		
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
		if (_prioQueue.isEmpty()) {

			return false;
		}
		_currFrom = _prioQueue.poll();
		fillEdgesUpward(_currFrom, _prioQueue, _bestWeightMapFrom, outEdgeExplorer, false);
		_visitedCountFrom++;
		return true;
	}

	private boolean downwardSearch() {
		if (_prioQueue.isEmpty()) {

			return false;
		}
		_currTo = _prioQueue.poll();
		fillEdgesDownward(_currTo, _prioQueue, _bestWeightMapFrom, inEdgeExplorer, false);
		_visitedCountTo++;

		return true;
	}
	
	@Override
	public SPTEntry[] calcPaths(int from, int[] to) {
		initUpwardSearch(from, 0);
		
		runUpwardSearch(); 
		
		initDownwardSearch(_upwardEdgeFilter.getHighestNode());
  
		EdgeExplorer tmpExplorer = inEdgeExplorer;
		inEdgeExplorer = _targetGraph.createExplorer();
		
		runDownwardSearch();
		
		inEdgeExplorer = tmpExplorer;

		SPTEntry[] targets = new SPTEntry[to.length];

		for (int i = 0; i < to.length; i++)
			targets[i] = _bestWeightMapFrom.get(to[i]);
		
		return targets;
	}
	
	public void initUpwardSearch(int from, double weight) {
		_currFrom = createSPTEntry(from, weight);
		_currFrom.visited = true;
		_prioQueue.add(_currFrom);

		if (!traversalMode.isEdgeBased()) {
			_bestWeightMapFrom.put(from, _currFrom);
		} else if (_currTo != null && _currTo.adjNode == from) {
			// special case of identical start and end
			// bestPath.sptEntry = currFrom;
			// bestPath.edgeTo = currTo;
			_finishedFrom = true;
			_finishedTo = true;
		} 
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

	private void fillEdgesUpward(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue, IntObjectMap<SPTEntry> shortestWeightMap,
			EdgeExplorer explorer, boolean reverse) {
		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null) // we reach one of the target nodes
			return;
		 
		while (iter.next()) {
			if (!_upwardEdgeFilter.accept(iter)) 
				continue;

			double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.edge) + currEdge.weight;

			if (Double.isInfinite(tmpWeight)) 
				continue;

			// Keep track of the currently found highest CH level node
			_upwardEdgeFilter.updateHighestNode(iter);

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
			IntObjectMap<SPTEntry> shortestWeightMap, EdgeExplorer explorer, boolean reverse) {

		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);
		
		if (iter == null) // we reach one of the target nodes
			return;

		while (iter.next())
		{
			// no need in filter, since all edges in targetGraph are valid and acceptable
			//if (!additionalEdgeFilter.accept(iter)) 
			//	continue;
			
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
			} else if (ee.weight >= tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge; 
				prioQueue.add(ee);
			} else if (ee.visited == false) {
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
