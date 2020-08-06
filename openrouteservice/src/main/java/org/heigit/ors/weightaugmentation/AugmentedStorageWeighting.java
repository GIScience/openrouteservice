package org.heigit.ors.weightaugmentation;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.vividsolutions.jts.geom.Polygon;
import java.util.List;
import java.util.Set;
import org.heigit.ors.mapmatching.polygon.PolygonMatcher;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;

public class AugmentedStorageWeighting implements Weighting {
  private final Weighting superWeighting;
  private final AugmentationStorage augmentationStorage;
  private final PolygonMatcher polygonMatcher;

  public AugmentedStorageWeighting(PMap additionalHints, Weighting weighting, GraphHopper graphHopper) {
    ORSPMap params = (ORSPMap) additionalHints;
    this.superWeighting = weighting;
    this.augmentationStorage = new AugmentationStorage();
    this.polygonMatcher = new PolygonMatcher();
    polygonMatcher.setGraphHopper(graphHopper);
    polygonMatcher.setLocationIndex();
    //noinspection unchecked
    List<AugmentedWeight> augmentedWeights = (List<AugmentedWeight>) params.getObj("user_weights");
    fillAugmentationStorage(augmentedWeights);
  }

  private void fillAugmentationStorage(List<AugmentedWeight> augmentedWeights) {
    for (AugmentedWeight augmentedWeight: augmentedWeights) {
      Polygon polygon = (Polygon) augmentedWeight.getGeometry();
      Set<Integer> edges = polygonMatcher.match(polygon);
      for (int edge: edges) {
        augmentationStorage.applyAugmentation(edge, augmentedWeight.getWeight());
      }
    }
  }

  public double getAugmentations(final EdgeIteratorState edge) {
    return augmentationStorage.get(edge.getEdge());
  }

  @Override
  public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
    return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId) * getAugmentations(edge);
  }

  @Override
  public long calcMillis(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
    return superWeighting.calcMillis(edge, reverse, prevOrNextEdgeId) * (long) getAugmentations(edge);
  }

  @Override
  public double getMinWeight(double distance) {
    // TODO implement
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
