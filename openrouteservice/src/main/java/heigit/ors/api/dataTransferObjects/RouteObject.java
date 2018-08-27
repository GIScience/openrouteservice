package heigit.ors.api.dataTransferObjects;

import com.vividsolutions.jts.geom.Coordinate;

public class RouteObject {
    private String geometryFormat;
    private RouteSummaryObject summary;

    private Coordinate[] routeCoordinates;

    private RouteSegment[] routeSegments;

    private RouteExtraInfo[] routeExtraInfos;

    private int[] waypointIndices;

}
