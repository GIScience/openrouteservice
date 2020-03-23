package org.heigit.ors.partitioning;


/**
 * Parameters for fast isochrone algorithm preprocessing and query processing.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class FastIsochroneParameters {

    public static final int FASTISO_MAXTHREADCOUNT = 12;
        //>> Partitioning
    public static final int PART__MIN_SPLITTING_ITERATION = 0;
    public static final int PART__MAX_SPLITTING_ITERATION = 268435456; //==2^28
    public static final int PART__MAX_CELL_NODES_NUMBER = 5000;
    public static final int PART__MIN_CELL_NODES_NUMBER = 4;
    public static final int PART__MAX_SUBCELL_NUMBER = 10;
    public static final boolean PART__SEPARATEDISCONNECTED = true;
    public static final int PART_SUPERCELL_HIERARCHY_LEVEL = 3;

    //>> Inertial Flow
    public static final double FLOW__SET_SPLIT_VALUE = 0.2525;
    public static final int FLOW__CONSIDERED_PROJECTIONS = 3;

    //CONTOUR
    //TODO Currently buggy!
    public static final boolean CONTOUR__USE_SUPERCELLS = true;
    public static final boolean ECC__USERELEVANTONLY = true;


    /**
     * Default tolerance.
     */
    public static final double CONCAVEHULL_THRESHOLD = 0.010;

    public static final boolean PART__DEBUG = false;


}
