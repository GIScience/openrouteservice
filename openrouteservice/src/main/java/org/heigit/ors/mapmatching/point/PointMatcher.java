package org.heigit.ors.mapmatching.point;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.QueryResult;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Similar to a classical map matching algorithm, but for points.</p>
 *
 * <p>It returns the closest edge for each point.</p>
 */
public class PointMatcher {
  private final GeometryFactory gf = new GeometryFactory();
  private GraphHopper graphHopper;
  private LocationIndexTree locationIndex;

  /**
   * Set {@link GraphHopper} instance to work on.
   * @param graphHopper existing {@link GraphHopper} instance
   */
  public void setGraphHopper(GraphHopper graphHopper) {
    this.graphHopper = graphHopper;
  }

  /**
   * Set {@link LocationIndexTree LocationIndex} given by {@link #graphHopper}.
   */
  public void setLocationIndex() {
    locationIndex = (LocationIndexTree) graphHopper.getLocationIndex();
  }

  /**
   * Find the closest edge for a given point.
   * @param point given {@link Point}
   * @return internal edge id
   */
  public Set<Integer> match(Point point) {
    Set<Integer> edges = new HashSet<>();
    QueryResult qResult = locationIndex.findClosest(point.getY(), point.getX(), EdgeFilter.ALL_EDGES);
    if (qResult.getClosestEdge() != null) {
      edges.add(qResult.getClosestEdge().getEdge());
    }
    return edges;
  }
}
