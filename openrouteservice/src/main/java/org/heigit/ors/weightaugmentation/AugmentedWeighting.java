package org.heigit.ors.weightaugmentation;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;

public class AugmentedWeighting implements Weighting {
  private final Weighting superWeighting;
  private final UserWeightParser weightParser;

  public AugmentedWeighting(FlagEncoder encoder, PMap additionalHints, Weighting weighting) {
    ORSPMap params = (ORSPMap) additionalHints;
    this.superWeighting = weighting;
    this.weightParser = (UserWeightParser) params.getObj("user_weights");
  }

  @Override
  public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
    return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId) * weightParser.getAugmentations(edge);
  }

  @Override
  public long calcMillis(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
    return superWeighting.calcMillis(edge, reverse, prevOrNextEdgeId) * (long) weightParser.getAugmentations(edge);
  }

  @Override
  public double getMinWeight(double distance) {
    return superWeighting.getMinWeight(distance);
  }

  @Override
  public FlagEncoder getFlagEncoder() {
    return superWeighting.getFlagEncoder();
  }

  @Override
  public boolean matches(HintsMap weightingMap) {
    return superWeighting.matches(weightingMap);
  }

  @Override
  public String toString() {
    return "augmented|" + superWeighting.toString();
  }

  @Override
  public String getName() {
    return "augmented|" + superWeighting.getName();
  }
}
