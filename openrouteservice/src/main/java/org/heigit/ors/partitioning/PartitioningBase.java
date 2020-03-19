package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.heigit.ors.partitioning.PartitioningBase.Projection.*;

/**
 *
 * <p>
 *
 * @author Hendrik Leuschner
 */
public abstract class PartitioningBase implements Runnable{
    int cellId;
    Graph ghGraph;
    EdgeFilter edgeFilter;
    PartitioningData pData;
    protected static Map<Projection, Projection> correspondingProjMap = new HashMap<>();
    protected Map<Projection, IntArrayList> projections;

    int[] nodeToCellArr;
    GraphHopperStorage ghStorage;
    ExecutorService executorService;

    enum Projection {  // Sortier-Projektionen der Koordinaten
        Line_p90
                {
                    public double sortValue(double lat, double lon) {
                        return lat;
                    }
                },
        Line_p675
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(67.5)) * lon;
                    }
                },
        Line_p45
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(45)) * lon;
                    }
                },
        Line_p225
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(22.5)) * lon;
                    }
                },
        Line_m00
                {
                    public double sortValue(double lat, double lon) {
                        return lon;
                    }
                },
        Line_m225
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(22.5)) * lon;
                    }
                },
        Line_m45
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(45)) * lon;
                    }
                },
        Line_m675
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(67.5)) * lon;
                    }
                };

        abstract double sortValue(double lat, double lon);
    }

    PartitioningBase() {
    }

    PartitioningBase(GraphHopperStorage _ghStorage, PartitioningData pData, EdgeFilterSequence edgeFilters, ExecutorService executorService) {
        ghStorage = _ghStorage;
        this.pData = pData;
        nodeToCellArr = new int[ghStorage.getNodes()];
        this.edgeFilter = edgeFilters;
        setExecutorService(executorService);
        this.ghGraph = ghStorage.getBaseGraph();
    }

    protected IntHashSet initNodes() {
        IntHashSet nodeIdSet = new IntHashSet();
        EdgeIterator edgeIter = ghGraph.getAllEdges();
        while (edgeIter.next()) {
            nodeIdSet.add(edgeIter.getBaseNode());
            nodeIdSet.add(edgeIter.getAdjNode());
        }
        return nodeIdSet;
    }

    public MaxFlowMinCut initAlgo() {
        return new EdmondsKarpAStar(ghStorage, pData, this.edgeFilter, true);
    }

    public MaxFlowMinCut setAlgo() {
        return new EdmondsKarpAStar(ghStorage, pData, this.edgeFilter, false);
    }

    MaxFlowMinCut getAlgo() {
        return new EdmondsKarpAStar();
    }

    void setExecutorService(ExecutorService executorService){
        this.executorService = executorService;
    }

    protected void prepareProjectionMaps(){
        this.correspondingProjMap = new HashMap<>();
        this.correspondingProjMap.put(Line_p90, Line_m00);
        this.correspondingProjMap.put(Line_p675, Line_m225);
        this.correspondingProjMap.put(Line_p45, Line_m45);
        this.correspondingProjMap.put(Line_p225, Line_m675);
        this.correspondingProjMap.put(Line_m00, Line_p90);
        this.correspondingProjMap.put(Line_m225, Line_p675);
        this.correspondingProjMap.put(Line_m45, Line_p45);
        this.correspondingProjMap.put(Line_m675, Line_p225);
    }

    protected class BiPartition {
        private IntHashSet partition0;
        private IntHashSet partition1;

        public BiPartition(IntHashSet partition0, IntHashSet partition1){
            this.partition0 = partition0;
            this.partition1 = partition1;
        }

        public IntHashSet getPartition0() {
            return partition0;
        }

        public IntHashSet getPartition1() {
            return partition1;
        }

    }

    protected class BiPartitionProjection {
        private Map<Projection, IntArrayList> projection0;
        private Map<Projection, IntArrayList> projection1;

        public BiPartitionProjection(Map<Projection, IntArrayList> partition0, Map<Projection, IntArrayList> partition1){
            this.projection0 = partition0;
            this.projection1 = partition1;
        }

        public Map<Projection, IntArrayList> getProjection0() {
            return projection0;
        }

        public Map<Projection, IntArrayList> getProjection1() {
            return projection1;
        }

    }
}
