package org.heigit.ors.fastisochrones.partitioning;

/**
 * Parameters for fast isochrone algorithm preprocessing and query processing. Some defaults can be changed via app.config
 *
 * @author Hendrik Leuschner
 */
public final class FastIsochroneParameters {
    //GLOBAL
    private static int maxThreadCount = 12;
    private static boolean log = true;
    //PARTITIONING
    private static int maxCellNodesNumber = 5000;
    private static int minCellNodesNumber = 4;
    private static double splitValue = 0.2525;
    //CONTOUR + ECCENTRICITY
    private static boolean enableSuperCells = true;

    private FastIsochroneParameters() {
    }

    public static int getMaxThreadCount() {
        return maxThreadCount;
    }

    public static void setMaxThreadCount(int threads) {
        maxThreadCount = threads;
    }

    public static boolean isLogEnabled() {
        return log;
    }

    public static void setLogEnabled(boolean log) {
        FastIsochroneParameters.log = log;
    }

    public static int getMaxCellNodesNumber() {
        return maxCellNodesNumber;
    }

    public static void setMaxCellNodesNumber(int maxCellNodesNumber) {
        FastIsochroneParameters.maxCellNodesNumber = maxCellNodesNumber;
    }

    public static int getMinCellNodesNumber() {
        return minCellNodesNumber;
    }

    public static void setMinCellNodesNumber(int minCellNodesNumber) {
        FastIsochroneParameters.minCellNodesNumber = minCellNodesNumber;
    }

    public static double getSplitValue() {
        return splitValue;
    }

    public static void setSplitValue(double splitValue) {
        FastIsochroneParameters.splitValue = splitValue;
    }

    public static boolean isSupercellsEnabled() {
        return enableSuperCells;
    }

    public static void setEnableSupercells(boolean enableSupercells) {
        enableSuperCells = enableSupercells;
    }
}
