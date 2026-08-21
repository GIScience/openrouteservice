package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.storages.CsvGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;

public class HeatStressWeightingDegreeC extends FastestWeighting {

    private final CsvGraphStorage heatStressStorage;
    private final byte[] buffer;
    private final double degree_c;
    private final String columnName;
    private final int columnIndex; // Caches index of columnName for performance reasons

    public HeatStressWeightingDegreeC(FlagEncoder encoder, PMap map, GraphHopperStorage graphStorage) {
        super(encoder, map);
        heatStressStorage = GraphStorageUtils.getGraphExtension(graphStorage, CsvGraphStorage.class);
        buffer = new byte[heatStressStorage.numEntries()];

        degree_c = map.getDouble("degree_c", 25);
        this.columnName = map.getString("column", "");
        this.columnIndex = heatStressStorage.columnIndex(columnName);
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        if (heatStressStorage != null) {
            int sunPercentage = heatStressStorage.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edgeState), columnIndex, buffer);

            double utciCelsius = degree_c + 3.1 * sunPercentage / 100;

            return heatFactor(utciCelsius);
        }

        return 1.0;
    }

    private static final double MEAN_ROUTE_LENGTH_M = 624.9;
    private static final double LOW_STRESS_RATE = 21.7 / MEAN_ROUTE_LENGTH_M;
    private static final double MIDDLE_STRESS_RATE = 44.0 / MEAN_ROUTE_LENGTH_M;
    private static final double HIGH_STRESS_RATE = 64.3 / MEAN_ROUTE_LENGTH_M;
    public static double heatFactor(double utciCelsius) {
        if (utciCelsius <= 26f) return 1.0;
        return 1.0 + LOW_STRESS_RATE * (Math.min(utciCelsius, 29.0) - 26.0)
                + MIDDLE_STRESS_RATE * Math.max(0.0, Math.min(utciCelsius, 32.0) - 29.0)
                + HIGH_STRESS_RATE * Math.max(0.0, utciCelsius - 32.0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final HeatStressWeightingDegreeC other = (HeatStressWeightingDegreeC) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("HeatStressWeighting" + this).hashCode();
    }
}
