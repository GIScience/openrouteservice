package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;

import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.partitioning.CellStorage;
import org.heigit.ors.partitioning.IsochroneNodeStorage;
import org.heigit.ors.partitioning.EccentricityStorage;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.ArrayList;

import static org.heigit.ors.partitioning.FastIsochroneParameters.PART__MAX_CELL_NODES_NUMBER;


public class Eccentricity extends AbstractEccentricity {

    double acceptedFullyReachablePercentage = 1.0;
    int eccentricityDijkstraLimitFactor = 5;

    public Eccentricity(GraphHopperStorage graphHopperStorage){
        super(graphHopperStorage);
    }

    public void calcEccentricities(GraphHopperStorage ghStorage, Graph graph, Weighting weighting, FlagEncoder flagEncoder, TraversalMode traversalMode, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        if(eccentricityStorages == null) {
            eccentricityStorages = new ArrayList<>();
        }
        EccentricityStorage eccentricityStorage = new EccentricityStorage(ghStorage, ghStorage.getDirectory(), weighting);
        if(!eccentricityStorage.loadExisting())
            eccentricityStorage.init();


        FixedCellEdgeFilter fixedCellEdgeFilter = new FixedCellEdgeFilter(isochroneNodeStorage, 0, graph.getNodes());
        EdgeFilter defaultEdgeFilter = DefaultEdgeFilter.outEdges(flagEncoder);
        EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
        edgeFilterSequence.add(defaultEdgeFilter);
        //Calculate the eccentricity without fixed cell edge filter for now
//        edgeFilterSequence.add(fixedCellEdgeFilter);
        for (int borderNode = 0; borderNode < graph.getNodes(); borderNode++){
            if(!isochroneNodeStorage.getBorderness(borderNode))
                continue;
            fixedCellEdgeFilter.setCellId(isochroneNodeStorage.getCellId(borderNode));
            RangeDijkstra rangeDijkstra = new RangeDijkstra(graph, weighting, traversalMode);
            rangeDijkstra.setMaxVisitedNodes(PART__MAX_CELL_NODES_NUMBER * eccentricityDijkstraLimitFactor);
            rangeDijkstra.setEdgeFilter(edgeFilterSequence);
            rangeDijkstra.setCellNodes(cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(borderNode)));
            double eccentricity = rangeDijkstra.calcMaxWeight(borderNode);
            int visitedNodesInCell = rangeDijkstra.fromMap.size();
            int cellNodeCount = cellStorage.getNodesOfCell(isochroneNodeStorage.getCellId(borderNode)).size();

            //TODO This is really just a cheap workaround that should really really be something smart instead
            //If set to 1, it is okay though
            if (((double) visitedNodesInCell) / cellNodeCount >= acceptedFullyReachablePercentage) {
                eccentricityStorage.setFullyReachable(borderNode, true);
            }
            else {
                eccentricityStorage.setFullyReachable(borderNode, false);
            }

            eccentricityStorage.setEccentricity(borderNode, eccentricity);
        }
        eccentricityStorage.flush();
        eccentricityStorages.add(eccentricityStorage);
    }

    @Override
    public void calcEccentricities() {
        calcEccentricities(this.ghStorage, this.baseGraph, this.weighting, this.encoder, this.traversalMode, this.isochroneNodeStorage, this.cellStorage);
    }
}
