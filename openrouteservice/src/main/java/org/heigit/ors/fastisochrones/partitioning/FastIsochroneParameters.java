package org.heigit.ors.fastisochrones.partitioning;


/**
 * Parameters for fast isochrone algorithm preprocessing and query processing. Some defaults can be changed via app.config
 *
 * @author Hendrik Leuschner
 */
public class FastIsochroneParameters {
    //GLOBAL
    private static int FIA__MAXTHREADCOUNT = 12;
    private static boolean FIA__LOG = true;
    //PARTITIONING
    private static int FIA__MAX_CELL_NODES_NUMBER = 5000;
    private static int FIA__MIN_CELL_NODES_NUMBER = 4;
    private static double FIA__SPLIT_VALUE = 0.2525;
    //CONTOUR + ECCENTRICITY
    private static boolean FIA__ENABLE_SUPERCELLS = true;

    public static void setMaxThreadCount(int threads) {
        FIA__MAXTHREADCOUNT = threads;
    }

    public static int getMaxThreadCount() {
        return FIA__MAXTHREADCOUNT;
    }

    public static boolean isLogEnabled() {
        return FIA__LOG;
    }

    public static void setLogEnabled(boolean fia_log) {
        FIA__LOG = fia_log;
    }

    public static int getMaxCellNodesNumber() {
        return FIA__MAX_CELL_NODES_NUMBER;
    }

    public static void setMaxCellNodesNumber(int fia_maxCellNodesNumber) {
        FIA__MAX_CELL_NODES_NUMBER = fia_maxCellNodesNumber;
    }

    public static int getMinCellNodesNumber() {
        return FIA__MIN_CELL_NODES_NUMBER;
    }

    public static void setMinCellNodesNumber(int fia_minCellNodesNumber) {
        FIA__MIN_CELL_NODES_NUMBER = fia_minCellNodesNumber;
    }

    public static double getSplitValue() {
        return FIA__SPLIT_VALUE;
    }

    public static void setSplitValue(double fia_splitValue) {
        FIA__SPLIT_VALUE = fia_splitValue;
    }

    public static boolean isSupercellsEnabled() {
        return FIA__ENABLE_SUPERCELLS;
    }

    public static void setEnableSupercells(boolean fia_enableSupercells) {
        FIA__ENABLE_SUPERCELLS = fia_enableSupercells;
    }
}
