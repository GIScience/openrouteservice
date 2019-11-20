package org.heigit.ors.partitioning;



public class FastIsochroneParameters {

    public static final int FASTISO_MAXTHREADCOUNT = 8;
        //>> Partitioning
    public static final int PART__MIN_SPLITTING_ITERATION = 0;
    public static final int PART__MAX_SPLITTING_ITERATION = 16777216; //==2^22
    public static final int PART__MAX_CELL_NODES_NUMBER = 5000;
    public static final int PART__MIN_CELL_NODES_NUMBER = 20;
    public static final boolean PART__SEPARATECONNECTED = true;

    //>> Inertial Flow
    public static final byte INFL__GRAPH_EDGE_CAPACITY = 1;
    public static final byte INFL__LOW_GRAPH_EDGE_CAPACITY = 1;
    public static final short INFL__DUMMY_EDGE_CAPACITY = Short.MAX_VALUE;
    public static final double FLOW__SET_SPLIT_VALUE = 0.2525;


    /**
     * Default tolerance.
     */
    public static final double CONCAVEHULL_THRESHOLD = 0.008;

}
