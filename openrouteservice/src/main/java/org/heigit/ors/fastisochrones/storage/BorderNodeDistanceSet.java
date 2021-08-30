package org.heigit.ors.fastisochrones.storage;

/**
 * Object to store a set of nodeids and respective distances.
 *
 * @author Hendrik Leuschner
 */
public class BorderNodeDistanceSet {
    int[] adjBorderNodeIds;
    double[] adjBorderNodeDistances;

    public BorderNodeDistanceSet(int[] adjBorderNodeIds, double[] adjBorderNodeDistances) {
        this.adjBorderNodeIds = adjBorderNodeIds;
        this.adjBorderNodeDistances = adjBorderNodeDistances;
    }

    public int[] getAdjBorderNodeIds() {
        return adjBorderNodeIds;
    }

    public double[] getAdjBorderNodeDistances() {
        return adjBorderNodeDistances;
    }
}
