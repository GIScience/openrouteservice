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
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import heigit.ors.routing.graphhopper.extensions.edgefilters.DownwardSearchEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.edgefilters.UpwardSearchEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntry;
import heigit.ors.routing.graphhopper.extensions.storages.MultiTreeSPEntryItem;

public class RPHASTAlgorithm extends AbstractManyToManyRoutingAlgorithm {
	//private final Logger logger = LoggerFactory.getLogger(getClass());

	private IntObjectMap<MultiTreeSPEntry> _bestWeightMapFrom;
	private MultiTreeSPEntry _currFrom;
	private MultiTreeSPEntry _currTo;
	private PriorityQueue<MultiTreeSPEntry> _prioQueue;
	private UpwardSearchEdgeFilter _upwardEdgeFilter;
	private DownwardSearchEdgeFilter _downwardEdgeFilter;
	private SubGraph _targetGraph;
	private boolean _finishedFrom;
	private boolean _finishedTo;
	private int _visitedCountFrom;
	private int _visitedCountTo;
	private int _treeEntrySize;
	
	private MultiTreeSPEntryItem _msptItem;
	private MultiTreeSPEntryItem _msptSubItem;
	private boolean _addToQueue = false;
	private double _edgeWeight, _entryWeight, _tmpWeight;
	
	public RPHASTAlgorithm(Graph graph, Weighting weighting, TraversalMode traversalMode) {
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

		_upwardEdgeFilter = new UpwardSearchEdgeFilter(chGraph, encoder);
		_downwardEdgeFilter = new DownwardSearchEdgeFilter(chGraph, encoder);

		_inEdgeExplorer = graph.createEdgeExplorer();
		_outEdgeExplorer = graph.createEdgeExplorer();
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
		_treeEntrySize = sources.length;

		// Phase I: build shortest path tree from all target nodes to the
		// highest node
		_targetGraph = new SubGraph(_graph);

		addNodes(_targetGraph, prioQueue, targets);

		while (!prioQueue.isEmpty()) {
			int adjNode = prioQueue.poll();
			EdgeIterator iter = _outEdgeExplorer.setBaseNode(adjNode);
			_downwardEdgeFilter.setBaseNode(adjNode);

			while (iter.next()) {
				if (!_downwardEdgeFilter.accept(iter))
					continue;

				if (_targetGraph.addEdge(adjNode, iter, true))
					prioQueue.add(iter.getAdjNode());
			}
		}

		//if (logger.isInfoEnabled())
		//	_targetGraph.print();
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
		fillEdgesUpward(_currFrom, _prioQueue, _bestWeightMapFrom, _outEdgeExplorer);
		_visitedCountFrom++;

		return true;
	}

	private boolean downwardSearch() {
		if (_prioQueue.isEmpty())
			return false;

		_currTo = _prioQueue.poll();
		fillEdgesDownward(_currTo, _prioQueue, _bestWeightMapFrom, _outEdgeExplorer);
		_visitedCountTo++;

		return true;
	}

	@Override
	public MultiTreeSPEntry[] calcPaths(int[] from, int[] to) {
		for (int i = 0; i < from.length; i++) {
			if (from[i] == -1)
			  continue;

			//If two queried points are on the same node, this case can occur
			MultiTreeSPEntry existing = _bestWeightMapFrom.get(from[i]);
			if (existing != null) {
				existing.getItem(i).weight = 0.0;
				continue;
			}
			
			_currFrom = new MultiTreeSPEntry(from[i], EdgeIterator.NO_EDGE, 0.0, true, null, from.length);
			_currFrom.getItem(i).weight = 0.0;
			_currFrom.visited = true;
				_prioQueue.add(_currFrom);

			if (!_traversalMode.isEdgeBased()) 
				_bestWeightMapFrom.put(from[i], _currFrom);
			else
				throw new IllegalStateException("Edge-based behavior not supported");
		}
 
		_outEdgeExplorer = _graph.createEdgeExplorer();
	//	StopWatch sw = new StopWatch();
	//	sw.start();

		runUpwardSearch();

	//	sw.stop();
		
	//	if (logger.isInfoEnabled())
	//	{
	//		logger.info(Long.toString(sw.getTime()));
	//		logger.info("HN: " + Integer.toString(_upwardEdgeFilter.getHighestNode()));
	//	}


		_currFrom = _bestWeightMapFrom.get(_upwardEdgeFilter.getHighestNode());
		_currFrom.visited = true;
		_currFrom.resetUpdate(true);
		_prioQueue.clear();
		_prioQueue.add(_currFrom);

		for (int i = 0; i < from.length; i++) {
			int sourceNode = from[i];
			MultiTreeSPEntry mspTree = _bestWeightMapFrom.get(sourceNode);
			mspTree.getItem(i).update = true;
			_prioQueue.add(mspTree);
		}

		_outEdgeExplorer = _targetGraph.createExplorer();
	//	sw = new StopWatch();
	//	sw.start();
		runDownwardSearch();
	//	sw.stop();
		
	//	if (logger.isInfoEnabled())
	//	{
	//		logger.info(Long.toString(sw.getTime()));
			
	//		logger.info("Iters: " + upwardIters + " " + downwardIters);
	//		logger.info("Total: " + upwardItersTotal + " " + downwardItersTotal);
	//		logger.info("Skipped: " + upwardItersSkipped + " " + downwardItersSkipped);
	//	}
		
		MultiTreeSPEntry[] targets = new MultiTreeSPEntry[to.length];

		for (int i = 0; i < to.length; ++i)
			targets[i] = _bestWeightMapFrom.get(to[i]);

		return targets;
	}

/*	private int downwardIters = 0;
	private int upwardIters = 0;
	private int downwardItersTotal = 0;
	private int upwardItersTotal = 0;
	private int downwardItersSkipped = 0;
	private int upwardItersSkipped = 0;*/

	private void fillEdgesUpward(MultiTreeSPEntry currEdge, PriorityQueue<MultiTreeSPEntry> prioQueue,
			IntObjectMap<MultiTreeSPEntry> shortestWeightMap, EdgeExplorer explorer) {
		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null) // we reach one of the target nodes
			return;

		_upwardEdgeFilter.setBaseNode(currEdge.adjNode);

		while (iter.next()) {
			if (!_upwardEdgeFilter.accept(iter))
				continue;
			
			_upwardEdgeFilter.updateHighestNode(iter);
			
			_edgeWeight = _weighting.calcWeight(iter, false, 0);

			if (!Double.isInfinite(_edgeWeight)) {
				MultiTreeSPEntry ee = shortestWeightMap.get(iter.getAdjNode());

				if (ee == null) {
					ee = new MultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), _edgeWeight, true, currEdge, currEdge.getSize());

					shortestWeightMap.put(iter.getAdjNode(), ee);
					prioQueue.add(ee);
				} else {
					_addToQueue = false;
					
					for (int i = 0; i < _treeEntrySize; ++i) {
						_msptItem = currEdge.getItem(i);
						_entryWeight = _msptItem.weight;
						
						if (_entryWeight == Double.POSITIVE_INFINITY)
							continue;

						if (_msptItem.update == false) {
							//					upwardItersSkipped++;
							continue;
						}
						_msptSubItem = ee.getItem(i);
						
						_tmpWeight = _edgeWeight + _entryWeight;

						if (_msptSubItem.weight > _tmpWeight) {
							_msptSubItem.weight = _tmpWeight;
							_msptSubItem.edge = iter.getEdge();
							_msptSubItem.parent = currEdge;
							_msptSubItem.update = true;
							
							_addToQueue = true;
		//					upwardIters++;

						} //else
		//					upwardItersTotal++;
					}

					if (_addToQueue) {
						ee.updateWeights();
						prioQueue.remove(ee);
						prioQueue.add(ee);
					}
				}
			}
		}
		
		if(!_targetGraph.containsNode(currEdge.adjNode)) currEdge.resetUpdate(false);
	}

	private void fillEdgesDownward(MultiTreeSPEntry currEdge, PriorityQueue<MultiTreeSPEntry> prioQueue,
			IntObjectMap<MultiTreeSPEntry> shortestWeightMap, EdgeExplorer explorer) {

		EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);

		if (iter == null)
			return;

		while (iter.next()) {
			_edgeWeight = _weighting.calcWeight(iter, false, 0);

			if (!Double.isInfinite(_edgeWeight)) {
				MultiTreeSPEntry ee = shortestWeightMap.get(iter.getAdjNode());

				if (ee == null) {
					ee = new MultiTreeSPEntry(iter.getAdjNode(), iter.getEdge(), _edgeWeight, true, currEdge, currEdge.getSize());
					ee.visited = true;

					shortestWeightMap.put(iter.getAdjNode(), ee);
					prioQueue.add(ee);
				} else {
					_addToQueue = false;
					
					for (int i = 0; i < _treeEntrySize; ++i) {
						_msptItem = currEdge.getItem(i);
						_entryWeight = _msptItem.weight;
						
						if (_entryWeight == Double.POSITIVE_INFINITY)
							continue;

//						if (_msptItem.update == false) {
//		//					downwardItersSkipped++;
//							continue;
//						}

						_tmpWeight = _edgeWeight + _entryWeight;

						MultiTreeSPEntryItem eeItem = ee.getItem(i);

						if (eeItem.weight > _tmpWeight) {
							eeItem.weight = _tmpWeight;
							eeItem.edge = iter.getEdge();
							eeItem.parent = currEdge;
							eeItem.update = true;
							
							_addToQueue = true;
		//					downwardIters++;
						} //else
						//	downwardItersTotal++;
					}
					
					ee.updateWeights();
					
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
//						ee.resetUpdate(true);
						prioQueue.add(ee);
					} else if (_addToQueue) {
						ee.visited = true;
						prioQueue.remove(ee);
						prioQueue.add(ee);
					}
				}
			}
		}
		
//		currEdge.resetUpdate(false);
	}
}
