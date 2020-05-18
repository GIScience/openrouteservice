package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.Contour;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.getSplitValue;
import static org.heigit.ors.fastisochrones.partitioning.Projector.Projection.*;
import static org.heigit.ors.fastisochrones.partitioning.Projector.Projection.Line_p225;

public class Projector {
    protected static Map<Projection, Projection> correspondingProjMap = new HashMap<>();
    private GraphHopperStorage ghStorage;

    public Projector(GraphHopperStorage graphHopperStorage) {
        this.ghStorage = graphHopperStorage;
    }

    protected void prepareProjectionMaps() {
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
        return nodeListProjMap;
    }

    protected BiPartitionProjection reorderProjections(Map<Projection, IntArrayList> originalProjections, BiPartition biPartition) {
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

    public enum Projection {
        Line_p90 {
            public double sortValue(double lat, double lon) {
                return lat;
            }
        },
        Line_p675 {
            public double sortValue(double lat, double lon) {
                return lat + Math.tan(Math.toRadians(67.5)) * lon;
            }
        },
        Line_p45 {
            public double sortValue(double lat, double lon) {
                return lat + Math.tan(Math.toRadians(45)) * lon;
            }
        },
        Line_p225 {
            public double sortValue(double lat, double lon) {
                return lat + Math.tan(Math.toRadians(22.5)) * lon;
            }
        },
        Line_m00 {
            public double sortValue(double lat, double lon) {
                return lon;
            }
        },
        Line_m225 {
            public double sortValue(double lat, double lon) {
                return lat - Math.tan(Math.toRadians(22.5)) * lon;
            }
        },
        Line_m45 {
            public double sortValue(double lat, double lon) {
                return lat - Math.tan(Math.toRadians(45)) * lon;
            }
        },
        Line_m675 {
            public double sortValue(double lat, double lon) {
                return lat - Math.tan(Math.toRadians(67.5)) * lon;
            }
        };

        abstract double sortValue(double lat, double lon);
    }
}
