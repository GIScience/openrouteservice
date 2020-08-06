package org.heigit.ors.weightaugmentation;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonEdgeFilter implements EdgeFilter {
  final Polygon[] polygons;

  public PolygonEdgeFilter(Polygon[] polygons) {
    this.polygons = polygons;
  }

  @Override
  public boolean accept(EdgeIteratorState edgeIteratorState) {
    LineString edgeGeometry = toLineString(edgeIteratorState.fetchWayGeometry(3));
    for (Polygon polygon: polygons) {
      if (polygon.contains(edgeGeometry) || polygon.crosses(edgeGeometry)) {
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
