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
public abstract class MaxFlowMinCut {

    PartitioningData pData;
    private IntHashSet srcPartition, snkPartition;

    protected boolean flooded;
    protected int nodes, visitedToken, maxFlow, maxFlowLimit;
    protected int snkNodeId;
    protected double limit;
    protected static int _dummyNodeId = -2;
    protected static int _dummyEdgeId = -2;

    protected Graph _graph;
    protected EdgeExplorer _edgeExpl;
    protected EdgeIterator _edgeIter;
    protected GraphHopperStorage _ghStorage;

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
            PartitioningDataBuilder maxFlowMinCut = new PartitioningDataBuilder(ghStorage, pData);
            maxFlowMinCut.setAdditionalEdgeFilter(edgeFilter);
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

    public void setVisited(int node) {
        pData.setVisited(node, visitedToken);
    }

    public boolean isVisited(int visited) {
        return (visited == visitedToken);
    }

    public void setUnvisitedAll() {
        ++this.visitedToken;
    }

    public int getMaxFlow() {
        execute();
        return maxFlow;
    }

    private void calcNodePartition() {
        srcPartition = new IntHashSet();
        snkPartition = new IntHashSet();

        execute();
        for (int nodeId : nodeOrder.keys) {
            if (isVisited(pData.getVisited(nodeId)))
                this.srcPartition.add(nodeId);
            else
                this.snkPartition.add(nodeId);
        }
    }

    public IntHashSet getSrcPartition() {
        calcNodePartition();
        return srcPartition;
    }

    public IntHashSet getSnkPartition() {
        return snkPartition;
    }

    private void execute() {
        if (flooded)
            return;

        this.flooded = true;
        flood();
    }

    public abstract void flood();


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
