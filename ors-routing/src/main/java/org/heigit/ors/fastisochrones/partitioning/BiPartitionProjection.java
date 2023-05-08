package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;

import java.util.Map;

/**
 * Helper class for keeping track of a node partitioning projections based on IntHashSets.
 *
 * @author Hendrik Leuschner
 */
class BiPartitionProjection {
    private final Map<Projector.Projection, IntArrayList> projection0;
    private final Map<Projector.Projection, IntArrayList> projection1;

    public BiPartitionProjection(Map<Projector.Projection, IntArrayList> partition0, Map<Projector.Projection, IntArrayList> partition1) {
        this.projection0 = partition0;
        this.projection1 = partition1;
    }

    public Map<Projector.Projection, IntArrayList> getProjection(int projectionNumber) {
        if (projectionNumber != 0 && projectionNumber != 1)
            throw new IllegalArgumentException("Only 2 projections supported.");
        return projectionNumber == 0 ? projection0 : projection1;
    }
}
