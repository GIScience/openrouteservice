package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntHashSet;

/**
 * Helper class for keeping track of a node partitioning based on IntHashSets.
 *
 * @author Hendrik Leuschner
 */
class BiPartition {
    private final IntHashSet partition0;
    private final IntHashSet partition1;

    public BiPartition() {
        this.partition0 = new IntHashSet(0);
        this.partition1 = new IntHashSet(0);
    }

    public BiPartition(IntHashSet partition0, IntHashSet partition1) {
        this.partition0 = partition0;
        this.partition1 = partition1;
    }

    public IntHashSet getPartition(int partitionNumber) {
        if (partitionNumber != 0 && partitionNumber != 1)
            throw new IllegalArgumentException("Only 2 partitions supported");
        return partitionNumber == 0 ? partition0 : partition1;
    }
}
