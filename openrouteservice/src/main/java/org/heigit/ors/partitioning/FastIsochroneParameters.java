package org.heigit.ors.partitioning;



public class FastIsochroneParameters {

    public static final int FASTISO_MAXTHREADCOUNT = 12;
        //>> Partitioning
    public static final int PART__MIN_SPLITTING_ITERATION = 0;
    public static final int PART__MAX_SPLITTING_ITERATION = 268435456; //==2^28
    public static final int PART__MAX_CELL_NODES_NUMBER = 5000;
    public static final int PART__MIN_CELL_NODES_NUMBER = 4;
    public static final boolean PART__SEPARATECONNECTED = true;
    public static final int PART_SUPERCELL_HIERARCHY_LEVEL = 3;

    //>> Inertial Flow
    public static final byte INFL__GRAPH_EDGE_CAPACITY = 1;
    public static final byte INFL__LOW_GRAPH_EDGE_CAPACITY = 1;
    public static final short INFL__DUMMY_EDGE_CAPACITY = Short.MAX_VALUE;
    public static final double FLOW__SET_SPLIT_VALUE = 0.2525;

    //CONTOUR
    public static final boolean CONTOUR__USE_SUPERCELLS = false;


    /**
     * Default tolerance.
     */
    public static final double CONCAVEHULL_THRESHOLD = 0.010;

}
