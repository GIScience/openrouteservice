package org.heigit.ors.partitioning;

public class BorderNodeDistanceSet{
    int[] adjBorderNodeIds;
    double[] adjBorderNodeDistances;

    public BorderNodeDistanceSet(int[] adjBorderNodeIds, double[] adjBorderNodeDistances){
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
