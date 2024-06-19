package org.heigit.ors.routing.graphhopper.extensions;

import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

public class WeightedPolygon extends Polygon {
    private double weightingFactor;
    // Constructor to initialize the WeightedPolygon from an existing Polygon and a WeightingFactor
    public WeightedPolygon(Polygon polygon, double weightingFactor) {
        //TODO properly set the holes
        super(polygon.getExteriorRing(), null, polygon.getFactory());
        this.weightingFactor = weightingFactor;
    }

    // Getter for the WeightingFactor
    public double getWeightingFactor() {
        return weightingFactor;
    }

    // Setter for the WeightingFactor
    public void setWeightingFactor(double weightingFactor) {
        this.weightingFactor = weightingFactor;
    }

    // Other methods can be added as needed to extend functionality
}
