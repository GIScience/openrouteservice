package org.heigit.ors.partitioning;

import com.graphhopper.storage.GraphHopperStorage;
import java.util.*;

import static org.heigit.ors.partitioning.FastIsochroneParameters.*;
import static org.heigit.ors.partitioning.Sort.sortByValueReturnList;

public class InertialFlow_Ser extends PartitioningBase {

    private enum Projection {  // Sortier-Projektionen der Koordinaten
        Line_p90
                // Projektion auf 90°
                {
                    public double sortValue(double lat, double lon) {
                        return lat;
                    }
                },
        Line_p60
                // Projektion auf 60°: v.lat+tan(60°)*v.lon
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(60)) * lon;
                    }
                },
        Line_p30
                // Projektion auf 30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat + Math.tan(Math.toRadians(30)) * lon;
                    }
                },
        Line_m00
                // Projektion auf 0°
                {
                    public double sortValue(double lat, double lon) {
                        return lon;
                    }
                },
        Line_m30
                // Projektion auf -30°
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(30)) * lon;
                    }
                },
        Line_m60
                // Projektion auf -60°
                {
                    public double sortValue(double lat, double lon) {
                        return lat - Math.tan(Math.toRadians(60)) * lon;
                    }
                };

        abstract double sortValue(double lat, double lon);
    }

    private Map<Integer, Set<Integer>> splitNodeSet;

    private static double[] bArray = new double[]{0.4};//, 0.27, 0.3, 0.33, 0.36, 0.39, 0.42, 0.45}; // somewhat between 0.25 and 0.45


    public InertialFlow_Ser(GraphHopperStorage ghStorage) {
        super(ghStorage);
        this.cellId = 1;
        this.splitNodeSet = new HashMap<>();

        initNodes();
        initAlgo();
    }

    private InertialFlow_Ser(int cellId, Set<Integer> nodeSet) {
        this.cellId = cellId;
        this.nodeIdSet = nodeSet;
        this.splitNodeSet = new HashMap<>();

        setAlgo();

        graphBiSplit();
        saveResults();
        recursion();
    }

    public void run() {
        graphBiSplit();
        saveResults();
        recursion();
    }


    private void graphBiSplit() {
        int mincutScore = Integer.MAX_VALUE;
        Set<Integer> mincutSrcSet = new HashSet<>();
        Set<Integer> mincutSnkSet = new HashSet<>();
        Map<Integer, Double> tmpNodeProjMap = new HashMap<>();

        //>> Loop through Projections and project each Node
        for (Projection proj : Projection.values()) {
            //>> sort projected Nodes
            tmpNodeProjMap.clear();
            for (int nodeId : nodeIdSet) {
                tmpNodeProjMap.put(nodeId, proj.sortValue(ghStorage.getNodeAccess().getLatitude(nodeId), ghStorage.getNodeAccess().getLongitude(nodeId)));
            }
            List<Integer> tmpNodeList = sortByValueReturnList(tmpNodeProjMap, true);

            //>> loop through b-percentage Values to fetch Source and Sink Nodes
            double aTmp = 0.0;
            for (double bTmp : bArray) {
                mincutAlgo.setMaxFlowLimit(mincutScore).initSubNetwork(aTmp, bTmp, tmpNodeList);
                int cutScore = mincutAlgo.getMaxFlow();

//                if ((0 < cutScore) && (cutScore < mincutScore)) {
                if (cutScore < mincutScore) {
                    //>> store Results
                    mincutScore = cutScore;
                    mincutSrcSet = mincutAlgo.getSrcPartition();
                    mincutSnkSet = mincutAlgo.getSnkPartition();
//                    mincutEdgBaseSet = mincutAlgo.getMinCut();

                    //>> get Data for next Recursion-Step
                    splitNodeSet.put(0, mincutSrcSet);
                    splitNodeSet.put(1, mincutSnkSet);
                }

                aTmp = bTmp;
            }
        }
        this.mincutAlgo = null;   //>> free Memory
    }

    private void saveResults() {
        //>> saving iteration results
        for (Map.Entry<Integer, Set<Integer>> entry : splitNodeSet.entrySet()) {
            for (Integer node : entry.getValue())
                nodeToCellArr[node] = cellId << 1 | entry.getKey();
        }
    }

    private void recursion() {
        for (Map.Entry<Integer, Set<Integer>> entry : splitNodeSet.entrySet()) {
            boolean nextRecursionLevel = false;

            //>> Condition
            if ((cellId < PART__MAX_SPLITTING_ITERATION) && (entry.getValue().size() > PART__MAX_CELL_NODES_NUMBER))
                nextRecursionLevel = true;
            if ((cellId < PART__MIN_SPLITTING_ITERATION))
                nextRecursionLevel = true;

            //>> Execution
            if (nextRecursionLevel) {
                new InertialFlow_Ser(cellId << 1 | entry.getKey(), entry.getValue());
            }
        }
    }
}
