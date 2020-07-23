package org.heigit.ors.weightaugmentation;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Polygon;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.AvoidAreasEdgeFilter;

// TODO this is a hack to test the Tests. Need to be implemented!
public class PolygonEdgeFilter implements EdgeFilter {
  final EdgeFilter superEdgeFilter;

  public PolygonEdgeFilter(Polygon[] polygons) {
    superEdgeFilter = new AvoidAreasEdgeFilter(polygons);
  }

  @Override
  public boolean accept(EdgeIteratorState edgeIteratorState) {
    return !superEdgeFilter.accept(edgeIteratorState);
  }
}
