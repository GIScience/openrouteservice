package org.heigit.ors.fastisochrones;

import com.graphhopper.coll.GHTreeMapComposed;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.*;
import org.heigit.ors.routing.graphhopper.extensions.core.AbstractCoreRoutingAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreDijkstra;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreDijkstraFilter;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreNodeContractor;

import java.util.Random;

import static com.graphhopper.util.Parameters.Algorithms.ASTAR_BI;
import static com.graphhopper.util.Parameters.Algorithms.DIJKSTRA_BI;

/**
 * Prepare the core graph. The core graph is a contraction hierarchies graph in which specified parts are not contracted
 * but remain on the highest level. E.g. used to build the core from restrictions.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner, Andrzej Oles, Stefan Panig
 */
public class PrepareIsochroneCore extends AbstractAlgoPreparation implements RoutingAlgorithmFactory {
    private final CHProfile chProfile;
    private final PreparationWeighting prepareWeighting;
    private final TraversalMode traversalMode;
    private final CoreDijkstraFilter levelFilter;
    private final EdgeFilter restrictionFilter;
    private final GraphHopperStorage ghStorage;
    private final CHGraphImpl prepareGraph;
    private final Random rand = new Random(123);
    private final StopWatch allSW = new StopWatch();
    private final Weighting weighting;
    private final Directory dir;
    private CHEdgeExplorer restrictionExplorer;
    private CHEdgeExplorer vehicleAllExplorer;
    private CHEdgeExplorer vehicleAllTmpExplorer;
    private CHEdgeExplorer calcPrioAllExplorer;
    private int maxLevel;
    // the most important nodes comes last
    private GHTreeMapComposed sortedNodes;
    private int oldPriorities[];
    private int restrictedNodes = 0;

    private long counter;
    private double meanDegree;
    private int periodicUpdatesPercentage = 10;
    private int lastNodesLazyUpdatePercentage = 10;
    private int neighborUpdatePercentage = 90;
    private double nodesContractedPercentage = 100;
    private double logMessagesPercentage = 20;
    private double dijkstraTime;
    private double periodTime;
    private double lazyTime;
    private double neighborTime;

    private CoreNodeContractor nodeContractor;


    private static final int RESTRICTION_PRIORITY = Integer.MAX_VALUE;

    PrepareIsochroneCore(Directory dir, GraphHopperStorage ghStorage, CHGraph chGraph, EdgeFilter restrictionFilter) {
        this.dir = dir;
        this.ghStorage = ghStorage;
        this.chProfile = chGraph.getCHProfile();
        this.weighting = chProfile.getWeighting();
        this.traversalMode = chProfile.getTraversalMode();
        this.prepareGraph = (CHGraphImpl) chGraph;
        this.restrictionFilter = restrictionFilter;
        levelFilter = new CoreDijkstraFilter(prepareGraph);
        prepareWeighting = new PreparationWeighting(weighting);
    }


    @Override
    public void doSpecificWork() {
        allSW.start();
        initFromGraph();

        if (!prepareNodes())
            return;
        contractNodes();
    }


    private PrepareIsochroneCore initFromGraph() {
        ghStorage.freeze();

        FlagEncoder prepareFlagEncoder = prepareWeighting.getFlagEncoder();
        final EdgeFilter allFilter = DefaultEdgeFilter.allEdges(prepareFlagEncoder);
        // filter by vehicle and level number
        final EdgeFilter accessWithLevelFilter = new LevelEdgeFilter(prepareGraph) {
            @Override
            public final boolean accept(EdgeIteratorState edgeState) {
                if (!super.accept(edgeState))
                    return false;

                return allFilter.accept(edgeState);
            }
        };

        maxLevel = prepareGraph.getNodes() + 1;
        vehicleAllExplorer = prepareGraph.createEdgeExplorer(allFilter);
        restrictionExplorer = prepareGraph.createEdgeExplorer(allFilter);
        vehicleAllTmpExplorer = prepareGraph.createEdgeExplorer(allFilter);
        calcPrioAllExplorer = prepareGraph.createEdgeExplorer(accessWithLevelFilter);

        // Use an alternative to PriorityQueue as it has some advantages:
        //   1. Gets automatically smaller if less entries are stored => less total RAM used.
        //      Important because Graph is increasing until the end.
        //   2. is slightly faster
        //   but we need the additional oldPriorities array to keep the old value which is necessary for the update method
        sortedNodes = new GHTreeMapComposed();
        oldPriorities = new int[prepareGraph.getNodes()];
        nodeContractor = new CoreNodeContractor(dir, ghStorage, prepareGraph, prepareGraph.getCHProfile());
        nodeContractor.setRestrictionFilter(restrictionFilter);
        nodeContractor.initFromGraph();
        return this;
    }

    private boolean prepareNodes() {
        int nodes = prepareGraph.getNodes();
        for (int node = 0; node < nodes; node++) {
            prepareGraph.setLevel(node, maxLevel);
        }

        for (int node = 0; node < nodes; node++) {
            int priority = oldPriorities[node] = calculatePriority(node);
            sortedNodes.insert(node, priority);
            if (priority == RESTRICTION_PRIORITY)
                restrictedNodes++;
        }

        return !sortedNodes.isEmpty();
    }

    private void contractNodes() {
        meanDegree = prepareGraph.getAllEdges().length() / prepareGraph.getNodes();
        int level = 1;
        counter = 0;

        // preparation takes longer but queries are slightly faster with preparation
        // => enable it but call not so often
        boolean periodicUpdate = true;
        StopWatch periodSW = new StopWatch();
        long periodicUpdatesCount = Math.round(Math.max(10, sortedNodes.getSize() / 100d * periodicUpdatesPercentage));
        if (periodicUpdatesPercentage == 0)
            periodicUpdate = false;

        // disable lazy updates for last x percentage of nodes as preparation is then a lot slower
        // and query time does not really benefit
        long lastNodesLazyUpdates = Math.round(sortedNodes.getSize() / 100d * lastNodesLazyUpdatePercentage);

        // according to paper "Polynomial-time Construction of Contraction Hierarchies for Multi-criteria Objectives" by Funke and Storandt
        // we don't need to wait for all nodes to be contracted

        //Avoid contrtacting core nodes  +  the additional percentage
        long nodesToAvoidContract = Math.round((100 - nodesContractedPercentage) / 100 * (sortedNodes.getSize() - restrictedNodes)) + restrictedNodes;
        StopWatch lazySW = new StopWatch();

        // Recompute priority of uncontracted neighbors.
        // Without neighbor updates preparation is faster but we need them
        // to slightly improve query time. Also if not applied too often it decreases the shortcut number.
        boolean neighborUpdate = true;
        if (neighborUpdatePercentage == 0)
            neighborUpdate = false;

        StopWatch neighborSW = new StopWatch();
        while (!sortedNodes.isEmpty()) {
            // periodically update priorities of ALL nodes
            if (periodicUpdate && counter > 0 && counter % periodicUpdatesCount == 0) {
                periodSW.start();
                sortedNodes.clear();
                int len = prepareGraph.getNodes();
                for (int node = 0; node < len; node++) {
                    if (prepareGraph.getLevel(node) != maxLevel)
                        continue;
                    int priority = oldPriorities[node];
                    if (!(priority == RESTRICTION_PRIORITY)) {
                        priority = oldPriorities[node] = calculatePriority(node);
                    }
                    sortedNodes.insert(node, priority);
                }
                periodSW.stop();
                if (sortedNodes.isEmpty())
                    throw new IllegalStateException(
                            "Cannot prepare as no unprepared nodes where found. Called preparation twice?");
            }


            counter++;
            int polledNode = sortedNodes.pollKey();

            //We have worked through all nodes that are not associated with restrictions. Now we can stop the contraction.
            if (oldPriorities[polledNode] == RESTRICTION_PRIORITY) {
                //Set the number of core nodes in the storage for use in other places
                prepareGraph.setCoreNodes(sortedNodes.getSize() + 1);
                while (!sortedNodes.isEmpty()) {
                    CHEdgeIterator iter = vehicleAllExplorer.setBaseNode(polledNode);
                    while (iter.next()) {
                        if (prepareGraph.getLevel(iter.getAdjNode()) == maxLevel) continue;
                        prepareGraph.disconnect(vehicleAllTmpExplorer, iter);
                    }
                    polledNode = sortedNodes.pollKey();
                }
                break;
            }

            if (sortedNodes.getSize() < nodesToAvoidContract) {
                // skipped nodes are already set to maxLevel
                prepareGraph.setCoreNodes(sortedNodes.getSize() + 1);
                //Disconnect all shortcuts that lead out of the core
                while (!sortedNodes.isEmpty()) {
                    CHEdgeIterator iter = vehicleAllExplorer.setBaseNode(polledNode);
                    while (iter.next()) {
//                        if (oldPriorities[iter.getAdjNode()] == RESTRICTION_PRIORITY) continue;
                        if (prepareGraph.getLevel(iter.getAdjNode()) == maxLevel) continue;
                        prepareGraph.disconnect(vehicleAllTmpExplorer, iter);
                    }
                    polledNode = sortedNodes.pollKey();
                }
                break;
            }

            if (!sortedNodes.isEmpty() && sortedNodes.getSize() < lastNodesLazyUpdates) {
                lazySW.start();
                int priority = oldPriorities[polledNode] = calculatePriority(polledNode);
                if (priority > sortedNodes.peekValue()) {
                    // current nodeId got more important => insert as new value and contract it later
                    sortedNodes.insert(polledNode, priority);
                    lazySW.stop();
                    continue;
                }
                lazySW.stop();
            }

            // contract nodeId v!
            nodeContractor.setMaxVisitedNodes(getMaxVisitedNodesEstimate());
            long degree = nodeContractor.contractNode(polledNode);
            // put weight factor on meanDegree instead of taking the average => meanDegree is more stable
            meanDegree = (meanDegree * 2 + degree) / 3;
            prepareGraph.setLevel(polledNode, level);
            level++;

            CHEdgeIterator iter = vehicleAllExplorer.setBaseNode(polledNode);
            while (iter.next()) {

                if (Thread.currentThread().isInterrupted()) {
                    throw new RuntimeException("Thread was interrupted");
                }

                int nn = iter.getAdjNode();
                if (prepareGraph.getLevel(nn) != maxLevel)
                    continue;

                if (neighborUpdate && rand.nextInt(100) < neighborUpdatePercentage) {
                    neighborSW.start();
                    int oldPrio = oldPriorities[nn];
                    int priority = oldPriorities[nn] = calculatePriority(nn);
                    if (priority != oldPrio)
                        sortedNodes.update(nn, oldPrio, priority);

                    neighborSW.stop();
                }

                // Hendrik: PHAST algorithm does not work properly with removed shortcuts
                prepareGraph.disconnect(vehicleAllTmpExplorer, iter);
            }
        }
        // Preparation works only once so we can release temporary data.
        // The preparation object itself has to be intact to create the algorithm.
        close();

        dijkstraTime += nodeContractor.getDijkstraSeconds();
        periodTime += periodSW.getSeconds();
        lazyTime += lazySW.getSeconds();
        neighborTime += neighborSW.getSeconds();
    }

    private int calculatePriority(int v) {
        // set the priority of a nodeId that is next to a restricted edge to a HIGH value
        CHEdgeIterator restrictionIterator = restrictionExplorer.setBaseNode(v);
        while (restrictionIterator.next()) {
            if (restrictionIterator.isShortcut()) continue;
            if (!restrictionFilter.accept(restrictionIterator))
                return RESTRICTION_PRIORITY;
        }
        nodeContractor.setMaxVisitedNodes(getMaxVisitedNodesEstimate());
        CoreNodeContractor.CalcShortcutsResult calcShortcutsResult = nodeContractor.calcShortcutCount(v);

        // set of shortcuts that would be added if adjNode v would be contracted next.
        // findShortcuts(calcScHandler.setNode(v));

        // System.out.println(v + "\t " + tmpShortcuts);
        // # huge influence: the bigger the less shortcuts gets created and the faster is the preparation
        //
        // every adjNode has an 'original edge' number associated. initially it is r=1
        // when a new shortcut is introduced then r of the associated edges is summed up:
        // r(u,w)=r(u,v)+r(v,w) now we can define
        // originalEdgesCount = σ(v) := sum_{ (u,w) ∈ shortcuts(v) } of r(u, w)
        int originalEdgesCount = calcShortcutsResult.originalEdgesCount;
        //  for (Shortcut sc : tmpShortcuts) {
        //      originalEdgesCount += sc.originalEdges;
        //  }

        // # lowest influence on preparation speed or shortcut creation count
        // (but according to paper should speed up queries)
        //
        // number of already contracted neighbors of v
        int contractedNeighbors = 0;
        int degree = 0;
        CHEdgeIterator iter = calcPrioAllExplorer.setBaseNode(v);
        while (iter.next()) {
            degree++;
            if (iter.isShortcut())
                contractedNeighbors++;
        }

        // from shortcuts we can compute the edgeDifference
        // # low influence: with it the shortcut creation is slightly faster
        //
        // |shortcuts(v)| − |{(u, v) | v uncontracted}| − |{(v, w) | v uncontracted}|
        // meanDegree is used instead of outDegree+inDegree as if one adjNode is in both directions
        // only one bucket memory is used. Additionally one shortcut could also stand for two directions.
        int edgeDifference = calcShortcutsResult.shortcutsCount - degree;

        // according to the paper do a simple linear combination of the properties to get the priority.
        // this is the current optimum for unterfranken:
        return 10 * edgeDifference + originalEdgesCount + contractedNeighbors;
    }

    @Override
    public RoutingAlgorithm createAlgo(Graph graph, AlgorithmOptions opts) {
        AbstractCoreRoutingAlgorithm algo;

        // TODO: Proper way of switching between DijkstraBase and AStar in core
        String algoStr = ASTAR_BI; //opts.getAlgorithm();

/*        if (ASTAR_BI.equals(algoStr)) {
            CoreALT tmpAlgo = new CoreALT(graph, prepareWeighting, traversalMode, this.pns);
            tmpAlgo.setApproximation(RoutingAlgorithmFactorySimple.getApproximation(ASTAR_BI, opts, graph.getNodeAccess()));
            algo = tmpAlgo;
        } else */
        if (DIJKSTRA_BI.equals(algoStr)) {
            algo = new CoreDijkstra(graph, prepareWeighting, traversalMode);
        } else {
            throw new IllegalArgumentException("Algorithm " + opts.getAlgorithm()
                    + " not supported for Contraction Hierarchies. Try with ch.disable=true");
        }

        algo.setMaxVisitedNodes(opts.getMaxVisitedNodes());

        // append any restriction filters after nodeId level filter
        EdgeFilter ef = opts.getEdgeFilter();
        if (ef != null)
            levelFilter.addRestrictionFilter(ef);

        algo.setEdgeFilter(levelFilter);

        return algo;
    }

    public void close() {
        nodeContractor.close();
        sortedNodes = null;
        oldPriorities = null;
    }

    @Override
    public String toString() {
        return "prepare|dijkstrabi|ch";
    }


    /**
     * S-E-T
     **/
    public PrepareIsochroneCore setPeriodicUpdates(int periodicUpdates) {
        if (periodicUpdates < 0)
            return this;
        if (periodicUpdates > 100)
            throw new IllegalArgumentException("periodicUpdates has to be in [0, 100], to disable it use 0");

        this.periodicUpdatesPercentage = periodicUpdates;
        return this;
    }

    public PrepareIsochroneCore setLazyUpdates(int lazyUpdates) {
        if (lazyUpdates < 0)
            return this;

        if (lazyUpdates > 100)
            throw new IllegalArgumentException("lazyUpdates has to be in [0, 100], to disable it use 0");

        this.lastNodesLazyUpdatePercentage = lazyUpdates;
        return this;
    }

    public PrepareIsochroneCore setNeighborUpdates(int neighborUpdates) {
        if (neighborUpdates < 0)
            return this;

        if (neighborUpdates > 100)
            throw new IllegalArgumentException("neighborUpdates has to be in [0, 100], to disable it use 0");

        this.neighborUpdatePercentage = neighborUpdates;
        return this;
    }

    public PrepareIsochroneCore setLogMessages(double logMessages) {
        if (logMessages >= 0)
            this.logMessagesPercentage = logMessages;
        return this;
    }

    public PrepareIsochroneCore setContractedNodes(double nodesContracted) {
        if (nodesContracted < 0)
            return this;

        if (nodesContracted > 100)
            throw new IllegalArgumentException("setNodesContracted can be 100% maximum");

        this.nodesContractedPercentage = nodesContracted;
        return this;
    }

    /**
     * G-E-T
     **/
    public double getLazyTime() {
        return lazyTime;
    }

    public double getPeriodTime() {
        return periodTime;
    }

    public double getDijkstraTime() {
        return dijkstraTime;
    }

    public double getNeighborTime() {
        return neighborTime;
    }

    public Weighting getWeighting() {
        return chProfile.getWeighting();
    }

    public CHProfile getCHProfile() {
        return chProfile;
    }

    private String getTimesAsString() {
        return "t(dijk):" + Helper.round2(dijkstraTime) + ", t(period):" + Helper.round2(periodTime) + ", t(lazy):"
                + Helper.round2(lazyTime) + ", t(neighbor):" + Helper.round2(neighborTime);
    }

    private int getMaxVisitedNodesEstimate() {
        // todo: we return 0 here if meanDegree is < 1, which is not really what we want, but changing this changes
        // the nodeId contraction order and requires re-optimizing the parameters of the graph contraction
        return (int) meanDegree * 100;
    }

}

