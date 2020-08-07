package org.heigit.ors.weightaugmentation;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * {@link EdgeFilter} that checks if an edge is in a polygon.
 */
public class PolygonEdgeFilter implements EdgeFilter {
  final Polygon[] polygons;

  /**
   * Create edge filter from a polygon array.
   * @param polygons given polygons
   */
  public PolygonEdgeFilter(Polygon[] polygons) {
    this.polygons = polygons;
  }

  /**
   * Check if the geometry of the given edge is in the polygon.
   *
   * This is achieved by checking if the polygon {@link Polygon#contains(Geometry) contains}, {@link Polygon#crosses(Geometry) crosses}, or {@link Polygon#touches(Geometry) touches} the given edge.
   * @param edgeIteratorState given edge to check
   * @return if edge is in the polygon
   */
  @Override
  public boolean accept(EdgeIteratorState edgeIteratorState) {
    LineString edgeGeometry = toLineString(edgeIteratorState.fetchWayGeometry(3));
    for (Polygon polygon: polygons) {
      if (polygon.contains(edgeGeometry) || polygon.crosses(edgeGeometry) || polygon.touches(edgeGeometry)) {
        return true;
      }
    }
    return false;
  }

  private static LineString toLineString(PointList pointList) {
    GeometryFactory gf = new GeometryFactory();
    Coordinate[] coordinates = new Coordinate[pointList.getSize() == 1 ? 2 : pointList.getSize()];

    for(int i = 0; i < pointList.getSize(); ++i) {
      coordinates[i] = new Coordinate(PointList.round6(pointList.getLongitude(i)), PointList.round6(pointList.getLatitude(i)));
    }

    if (pointList.getSize() == 1) {
      coordinates[1] = coordinates[0];
    }

    return gf.createLineString(coordinates);
  }
}
