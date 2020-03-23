package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntHashSet;

class BiPartition {
    private IntHashSet partition0;
    private IntHashSet partition1;

    public BiPartition(){
        this.partition0 = new IntHashSet(0);
        this.partition1 = new IntHashSet(0);
    }

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
