package org.heigit.ors.weightaugmentation;

import static org.heigit.ors.weightaugmentation.UserWeightParser.ALLOWED_MULTI_GEOMETRY_TYPES;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.PMap;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.AugmentationStorageException;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.mapmatching.hmm.HiddenMarkovMapMatcher;
import org.heigit.ors.mapmatching.point.PointMatcher;
import org.heigit.ors.mapmatching.polygon.PolygonMatcher;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;
import org.heigit.ors.util.DebugUtility;

public class UserWeightFactory {
  public static final String USED_PARAM = RouteRequest.PARAM_USER_WEIGHTS;
  private static final Logger LOGGER = Logger.getLogger(UserWeightFactory.class.getName());

  private final AugmentationStorage augmentationStorage;
  private final PolygonMatcher polygonMatcher;
  private final PointMatcher pointMatcher;
  private final HiddenMarkovMapMatcher lineStringMatcher;

  /**
   * Constructor of the class with custom stepSize and searchRadius.
   * @param additionalHints additionalHints (as {@link ORSPMap}) created in {@link org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper} including the augmentations by the user in the key {@code "user_weights"}
   * @param graphHopper {@link GraphHopper} instance
   * @param stepSize custom step size for generating the node search grid
   * @param searchRadius custom search radius for the node search
   */
  public UserWeightFactory(PMap additionalHints, GraphHopper graphHopper, double stepSize, double searchRadius) throws AugmentationStorageException, NoSuchFieldException {
    ORSPMap params = (ORSPMap) additionalHints;
    this.augmentationStorage = new AugmentationStorage();
    this.polygonMatcher = new PolygonMatcher();
    this.pointMatcher = new PointMatcher();
    this.lineStringMatcher = new HiddenMarkovMapMatcher();
    polygonMatcher.setGraphHopper(graphHopper);
    pointMatcher.setGraphHopper(graphHopper);
    lineStringMatcher.setGraphHopper(graphHopper);
    polygonMatcher.setLocationIndex();
    pointMatcher.setLocationIndex();
    lineStringMatcher.setEdgeFilter(null);
    if (stepSize > 0) {
      polygonMatcher.setNodeGridStepSize(stepSize);
    }
    if (searchRadius > 0) {
      polygonMatcher.setSearchRadius(searchRadius);
      lineStringMatcher.setSearchRadius(searchRadius);
    }
    if (params == null || !params.hasObj(USED_PARAM)) {
      throw new NoSuchFieldException(String.format("Field '%s' missing in params.", USED_PARAM));
    }
    //noinspection unchecked
    getEdgesAndFillStorage((List<AugmentedWeight>) params.getObj(USED_PARAM));
    if (DebugUtility.isDebug()) {
      LOGGER.info(String.format("Number of augmentations: %s", augmentationStorage.size()));
    }
  }

  /**
   * Overload of the {@link #UserWeightFactory(PMap, GraphHopper, double, double)} with a default {@code stepSize} and {@code searchRadius}.
   */
  public UserWeightFactory(PMap additionalHints, GraphHopper graphHopper) throws AugmentationStorageException, NoSuchFieldException {
    this(additionalHints, graphHopper, -1, -1);
  }

  private void getEdgesAndFillStorage(List<AugmentedWeight> augmentedWeights) throws AugmentationStorageException {
    for (AugmentedWeight augmentedWeight: augmentedWeights) {
      Set<Integer> edges = getMatchedEdges(augmentedWeight.getGeometry());
      augmentationStorage.applyAllAugmentation(edges, augmentedWeight.getWeight());
    }
  }

  private Set<Integer> getMatchedEdges(Geometry geometry) {
    Set<Integer> edges = new HashSet<>();
    if (geometry.getGeometryType().equals("Polygon")) {
      edges.addAll(polygonMatcher.match((Polygon) geometry));
    } else if (geometry.getGeometryType().equals("Point")) {
      edges.addAll(pointMatcher.match((Point) geometry));
    } else if (geometry.getGeometryType().equals("LineString")) {
      LineString lineString = (LineString) geometry;
      RouteSegmentInfo[] routeSegments = lineStringMatcher
          .match(lineString.getCoordinates(), true);
      for (RouteSegmentInfo routeSegment: routeSegments) {
        if (routeSegment != null)
          edges.addAll(routeSegment.getEdges());
      }
    } else if (Arrays.asList(ALLOWED_MULTI_GEOMETRY_TYPES).contains(geometry.getGeometryType())) {
      for (int g = 0; g < geometry.getNumGeometries(); g++) {
        edges.addAll(getMatchedEdges(geometry.getGeometryN(g)));
      }
    } else {
      throw new UnsupportedOperationException("AugmentationStorage is not implemented for " + geometry.getGeometryType());
    }
    return edges;
  }

  /**
   * Returns the created {@link AugmentationStorage}.
   * @return {@link AugmentationStorage}
   */
  public AugmentationStorage getStorage() {
    return augmentationStorage;
  }

  /**
   * A new {@link Weighting} using the {@link AugmentationStorage} ({@link AugmentedWeighting}).
   * @param weighting already existing {@link Weighting}
   * @return new {@link AugmentedWeighting}
   */
  public AugmentedWeighting getWeighting(Weighting weighting) {
    return new AugmentedWeighting(weighting, augmentationStorage);
  }
}
