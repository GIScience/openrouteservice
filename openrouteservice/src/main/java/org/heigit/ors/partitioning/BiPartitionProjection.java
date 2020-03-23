package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;

import java.util.Map;

class BiPartitionProjection {
    private Map<PartitioningBase.Projection, IntArrayList> projection0;
    private Map<PartitioningBase.Projection, IntArrayList> projection1;

    public BiPartitionProjection(Map<PartitioningBase.Projection, IntArrayList> partition0, Map<PartitioningBase.Projection, IntArrayList> partition1){
        this.projection0 = partition0;
        this.projection1 = partition1;
    }

    public Map<PartitioningBase.Projection, IntArrayList> getProjection0() {
        return projection0;
    }

    public Map<PartitioningBase.Projection, IntArrayList> getProjection1() {
        return projection1;
    }

}
