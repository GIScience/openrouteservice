package org.heigit.ors.fastisochrones.partitioning;

/**
 * Parameters for fast isochrone algorithm preprocessing and query processing.
 * Some defaults can be changed via ors-config.json
 *
 * @author Hendrik Leuschner
 */
public final class FastIsochroneParameters {
    //NAMES
    public static final String ACTIVECELLDIJKSTRA = "ActiveCellDijkstra";
    public static final String CORERANGEDIJKSTRA = "CoreRangeDijkstra";
    public static final String RANGEDIJKSTRA = "RangeDijkstra";

    //GLOBAL
    private static int maxThreadCount = 12;
    private static boolean log = true;
    //PARTITIONING
    //Based on Implementierung eines Algorithmus zur schnellen Berechnung metrik-affiner Isochronen in einem Straßennetzwerk by Stefan Panig, 2019
    private static int maxCellNodesNumber = 5000;
    private static int minCellNodesNumber = 1;
    //Factor based on Aaron Schild & Christian Sommer. On Balanced Seperators in Road Networks, Springer
    //International Publishing Switzerland, 2015 and
    //Implementierung eines Algorithmus zur schnellen Berechnung metrik-affiner Isochronen in einem Straßennetzwerk by Stefan Panig, 2019
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
