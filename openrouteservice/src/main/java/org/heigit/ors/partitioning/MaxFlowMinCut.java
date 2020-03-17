package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntHashMap;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
/**
 * Abstract MaxFlowMinCut implementation.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class MaxFlowMinCut {

    PartitioningData pData;

    protected boolean flooded;
    protected int nodes, visitedToken, maxFlow, maxFlowLimit;
    protected int snkNodeId;

    protected static int _dummyNodeId = -2;
    protected static int _dummyEdgeId = -2;

    protected Graph _graph;
    protected EdgeExplorer _edgeExpl;
    protected EdgeIterator _edgeIter;

    protected GraphHopperStorage _ghStorage;

    protected double limit;

    protected IntIntHashMap nodeOrder;
    protected IntArrayList orderedNodes;

    protected EdgeFilter edgeFilter;

    MaxFlowMinCut(GraphHopperStorage ghStorage, PartitioningData pData, EdgeFilter edgeFilter, boolean init) {
        this._ghStorage = ghStorage;
        this._graph = ghStorage.getBaseGraph();
        this._edgeExpl = _graph.createEdgeExplorer();
        this.pData = pData;

        setAdditionalEdgeFilter(edgeFilter);
        if(init) {
            MaxFlowMinCutImpl maxFlowMinCut = new MaxFlowMinCutImpl(ghStorage, pData);
            maxFlowMinCut.setAdditionalEdgeFilter(edgeFilter);
            maxFlowMinCut.setGHStorage(ghStorage);
            maxFlowMinCut.run();
        }
    }

    MaxFlowMinCut() {
    }

    protected void setGHStorage(GraphHopperStorage ghStorage){
        this._ghStorage = ghStorage;
    }


    protected void initSubNetwork() {
        reset();
        buildSrcSnkNodes();
        identifySrcSnkEdges();
    }

    protected MaxFlowMinCut setMaxFlowLimit(int prevMaxFlow) {
        this.maxFlowLimit = prevMaxFlow;
        return this;
    }

    /*###############################################################################################################*/
    //>> query call to build individual network

    protected void reset() {
        this.nodes = 0;
        this.flooded = false;
        this.maxFlow = 0;
        this.visitedToken = 1;
    }


    private void buildSrcSnkNodes() {
        this.snkNodeId = getDummyNodeId();
    }

    private void identifySrcSnkEdges() {
        this.nodes = orderedNodes.size();
        for (int i = 0; i < nodes; i++) {
            IntHashSet targSet = new IntHashSet();
            int nodeId = orderedNodes.get(i);

            pData.setVisited(nodeId, 0);

            _edgeIter = _edgeExpl.setBaseNode(nodeId);
            while (_edgeIter.next()) {
                if(targSet.contains(_edgeIter.getAdjNode())
                        || _edgeIter.getAdjNode() == _edgeIter.getBaseNode())
                    continue;
                if(!acceptForPartitioning(_edgeIter))
                    continue;
                targSet.add(_edgeIter.getAdjNode());
                //reset
                FlowEdgeData flowEdgeData = pData.getFlowEdgeData(_edgeIter.getEdge(), _edgeIter.getBaseNode());
                flowEdgeData.flow = false;
                pData.setFlowEdgeData(_edgeIter.getEdge(), _edgeIter.getBaseNode(), flowEdgeData);

            }
        }
    }

    public void setNodeOrder(){
        this.nodeOrder = new IntIntHashMap();
        for(int i = 0; i < orderedNodes.size(); i++)
            nodeOrder.put(orderedNodes.get(i), i);
    }

    public void setOrderedNodes(IntArrayList orderedNodes){
        this.orderedNodes = orderedNodes;
    }

    protected boolean acceptForPartitioning(EdgeIterator edgeIterator){
        return edgeFilter.accept(edgeIterator);
    }

    public void setAdditionalEdgeFilter(EdgeFilter edgeFilter){
        this.edgeFilter = edgeFilter;
    }

    private synchronized int getDummyNodeId() {
        return ++_dummyNodeId;
    }

    protected synchronized int getDummyEdgeId() {
        return ++_dummyEdgeId;
    }

}
