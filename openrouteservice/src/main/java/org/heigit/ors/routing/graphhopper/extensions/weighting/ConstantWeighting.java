package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

public class ConstantWeighting implements Weighting {
    private final double weight;
    private final long millis;

    public ConstantWeighting(double weight, long millis) {
        this.weight = weight;
        this. millis = millis;
    }

    @Override
    public double getMinWeight(double distance) {
        return weight;
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeIteratorState, boolean reverse, int prevOrNextEdgeId) {
        return weight;
    }

    @Override
    public long calcMillis(EdgeIteratorState edgeIteratorState, boolean reverse, int prevOrNextEdgeId) {
        return millis;
    }

    @Override
    public FlagEncoder getFlagEncoder() {
        return null;
    }

    @Override
    public String getName() {
        return "constant(" + weight + ")";
    }

    @Override
    public boolean matches(HintsMap hintsMap) {
        return false;
    }
}
