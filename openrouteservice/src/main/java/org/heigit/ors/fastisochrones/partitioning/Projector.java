package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.Contour;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getSplitValue;
import static org.heigit.ors.fastisochrones.partitioning.Projector.Projection.*;
import static org.heigit.ors.fastisochrones.partitioning.Projector.Projection.LINE_P225;

public class Projector {
    protected static Map<Projection, Projection> correspondingProjMap;
    private GraphHopperStorage ghStorage;

    public Projector() {
        prepareProjectionMaps();
    }

    private static void prepareProjectionMaps() {
        correspondingProjMap = new EnumMap<>(Projection.class);
        correspondingProjMap.put(LINE_P90, LINE_M00);
        correspondingProjMap.put(LINE_P675, LINE_M225);
        correspondingProjMap.put(LINE_P45, LINE_M45);
        correspondingProjMap.put(LINE_P225, LINE_M675);
        correspondingProjMap.put(LINE_M00, LINE_P90);
        correspondingProjMap.put(LINE_M225, LINE_P675);
        correspondingProjMap.put(LINE_M45, LINE_P45);
        correspondingProjMap.put(LINE_M675, LINE_P225);
    }

    protected Map<Projection, IntArrayList> calculateProjections() {
        //>> Loop through linear combinations and project each Node
        Map<Projection, IntArrayList> nodeListProjMap = new HashMap<>(Projection.values().length);
        Integer[] ids = IntStream.rangeClosed(0, ghStorage.getNodes() - 1).boxed().toArray(Integer[]::new);

        Double[] values = new Double[ids.length];
        Sort sort = new Sort();
        for (Projection proj : Projection.values()) {
            //>> sort projected Nodes
            for (int i = 0; i < ids.length; i++) {
                values[i] = proj.sortValue(ghStorage.getNodeAccess().getLatitude(ids[i]), ghStorage.getNodeAccess().getLongitude(ids[i]));
            }
            nodeListProjMap.put(proj, sort.sortByValueReturnList(ids, values));
        }
        //Check if there are at least two differing projections. If no lat lon data is set, all projections are the same.
        boolean valid = false;
        for (Projection proj : Projection.values()) {
            if (!nodeListProjMap.get(proj).equals(nodeListProjMap.get(LINE_M00)))
                valid = true;
        }
        if (!valid)
            throw new IllegalStateException("All projections of the graph are the same. Maybe NodeAccess is faulty or not initialized?");
        return nodeListProjMap;
    }

    protected BiPartitionProjection partitionProjections(Map<Projection, IntArrayList> originalProjections, BiPartition biPartition) {
        IntHashSet part0 = biPartition.getPartition(0);
        Map<Projection, IntArrayList> projections0 = new HashMap<>(Projection.values().length);
        Map<Projection, IntArrayList> projections1 = new HashMap<>(Projection.values().length);
        int origNodeCount = originalProjections.get(Projection.values()[0]).size();
        //Add initial lists
        for (Projection proj : Projection.values()) {
            projections0.put(proj, new IntArrayList(origNodeCount / 3));
            projections1.put(proj, new IntArrayList(origNodeCount / 3));
        }

        //Go through the original projections and separate each into two projections for the subsets, maintaining order
        for (int i = 0; i < origNodeCount; i++) {
            for (Projection proj : originalProjections.keySet()) {
                int node = originalProjections.get(proj).get(i);
                if (part0.contains(node))
                    projections0.get(proj).add(node);
                else
                    projections1.get(proj).add(node);
            }
        }

        return new BiPartitionProjection(projections0, projections1);
    }

    protected List<Projection> calculateProjectionOrder(Map<Projection, IntArrayList> projections) {
        List<Projection> order;
        Map<Projection, Double> squareRangeProjMap = new HashMap<>();
        Map<Projection, Double> orthogonalDiffProjMap = new HashMap<>();
        //>> calculate Projection-Distances
        for (Projection proj : projections.keySet()) {
            int idx = (int) (projections.get(proj).size() * getSplitValue());
            squareRangeProjMap.put(proj, projIndividualValue(projections, proj, idx));
        }

        //>> combine inverse Projection-Distances
        for (Projection proj : projections.keySet()) {
            orthogonalDiffProjMap.put(proj, projCombinedValue(squareRangeProjMap, proj));
        }

        //>> order Projections by Projection-Value
        Sort sort = new Sort();
        order = sort.sortByValueReturnList(orthogonalDiffProjMap, false);
        return order;
    }

    private double projIndividualValue(Map<Projection, IntArrayList> projMap, Projection proj, int idx) {
        IntArrayList tmpNodeList;
        double fromLat, fromLon, toLat, toLon;

        tmpNodeList = projMap.get(proj);
        toLat = ghStorage.getNodeAccess().getLatitude(tmpNodeList.get(idx));
        toLon = ghStorage.getNodeAccess().getLongitude(tmpNodeList.get(idx));
        fromLat = ghStorage.getNodeAccess().getLatitude(tmpNodeList.get(tmpNodeList.size() - idx - 1));
        fromLon = ghStorage.getNodeAccess().getLongitude(tmpNodeList.get(tmpNodeList.size() - idx - 1));

        return Contour.distance(fromLat, toLat, fromLon, toLon);
    }

    private double projCombinedValue(Map<Projection, Double> squareRangeProjMap, Projection proj) {
        return squareRangeProjMap.get(proj) * squareRangeProjMap.get(proj) / squareRangeProjMap.get(correspondingProjMap.get(proj));
    }

    public void setGHStorage(GraphHopperStorage ghStorage) {
        this.ghStorage = ghStorage;
    }

    public enum Projection {
        LINE_P90 {
            public double sortValue(double lat, double lon) {
                return lat;
            }
        },
        LINE_P675 {
            //2.414213 = Math.tan(Math.toRadians(67.5))
            public double sortValue(double lat, double lon) {
                return lat + 2.414213 * lon;
            }
        },
        LINE_P45 {
            //1 = Math.tan(Math.toRadians(45))
            public double sortValue(double lat, double lon) {
                return lat + 1 * lon;
            }
        },
        LINE_P225 {
            //0.414213 = Math.tan(Math.toRadians(22.5))
            public double sortValue(double lat, double lon) {
                return lat + 0.414213 * lon;
            }
        },
        LINE_M00 {
            public double sortValue(double lat, double lon) {
                return lon;
            }
        },
        LINE_M225 {
            //0.414213 = Math.tan(Math.toRadians(22.5))
            public double sortValue(double lat, double lon) {
                return lat - 0.414213 * lon;
            }
        },
        LINE_M45 {
            //1 = Math.tan(Math.toRadians(45))
            public double sortValue(double lat, double lon) {
                return lat - 1 * lon;
            }
        },
        LINE_M675 {
            //2.414213 = Math.tan(Math.toRadians(67.5))
            public double sortValue(double lat, double lon) {
                return lat - 2.414213 * lon;
            }
        };

        abstract double sortValue(double lat, double lon);
    }
}
