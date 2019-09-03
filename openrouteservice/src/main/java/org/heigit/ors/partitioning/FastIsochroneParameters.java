package org.heigit.ors.partitioning;



public class FastIsochroneParameters {
        //>> Partitioning
    public static final int PART__MIN_SPLITTING_ITERATION = 0;
    public static final int PART__MAX_SPLITTING_ITERATION = 4194304; //==2^22
    public static final int PART__MAX_CELL_NODES_NUMBER = 5000;

    //>> Inertial Flow
    public static final int INFL__GRAPH_EDGE_CAPACITY = 1;
    public static final int INFL__DUMMY_EDGE_CAPACITY = Integer.MAX_VALUE;


    /**
     * Default tolerance.
     */
    public static final double CONCAVEHULL_THRESHOLD = 0.008;

}
