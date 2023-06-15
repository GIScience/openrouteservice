package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.storages.CsvGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;

public class HeatStressWeighting extends FastestWeighting {

    private CsvGraphStorage heatStressStorage;
    private byte[] buffer;
    private double weightingFactor;
    private final String columnName;
    private final int columnIndex; // Caches index of columnName for performance reasons

    public HeatStressWeighting(FlagEncoder encoder, PMap map, GraphHopperStorage graphStorage) {
        super(encoder, map);
        heatStressStorage = GraphStorageUtils.getGraphExtension(graphStorage, CsvGraphStorage.class);
        buffer = new byte[heatStressStorage.numEntries()];

        weightingFactor = map.getDouble("factor", 1);
        this.columnName = map.getString("column", "");
        this.columnIndex = heatStressStorage.columnIndex(columnName);
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        if (heatStressStorage != null) {
            int stressLevel = heatStressStorage.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edgeState), columnIndex, buffer);
            return stressLevel * weightingFactor / 100;
        }

        return 1.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final HeatStressWeighting other = (HeatStressWeighting) obj;
        return toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return ("HeatStressWeighting" + toString()).hashCode();
    }
}