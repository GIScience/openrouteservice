package heigit.ors.services.routing.requestprocessors.gpx;


import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.routing.requestprocessors.gpx.beans.*;
import heigit.ors.util.GeomUtility;


import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class GpxRoutingResponseWriter {
    /* There will be no LineString check for now.
    All calculations that reach this point will anyway be only LineStrings */
    public static GPX toGPX(RoutingRequest request, RouteResult[] routeResults, JSONObject json) {
        // TODO migrate own gpx solution
        // example: WayPoint.builder().extSpeed(20)
        WayPoint wayPoint = new WayPoint();
        List<WayPoint> wayPointList = new ArrayList<>();
        for (RouteResult route : routeResults) {
            LineString routeGeom = GeomUtility.createLinestring(route.getGeometry());
            //TODO get the Bounding "box" or the Bounds parameters here for the gpx
            for (RouteSegment segment : route.getSegments()) {
                List<RouteStep> routeSteps = segment.getSteps();
                for (int i = 0; i < routeGeom.getLength(); i++) {
                    RouteStep routeStep = null;
                    for (RouteStep element : routeSteps) {
                        int[] wayPoints = element.getWayPoints();
                        if (i < wayPoints[0] || i > wayPoints[1]) {
                            routeStep = element;
                            break;
                        }


                    }
                    // Get variables
                    Point point = routeGeom.getPointN(i);
                    Double longitude = point.getCoordinate().x;
                    Double latitude = point.getCoordinate().y;
                    Double elevation = point.getCoordinate().z;
                    double stepDistance = routeStep != null ? routeStep.getDistance() : 0;
                    double stepDuration = routeStep != null ? routeStep.getDuration() : 0;
                    String stepInstruction = routeStep != null ? routeStep.getInstruction() : "";
                    String stepName = routeStep != null ? routeStep.getName() : "";
                    int stepType = routeStep != null ? routeStep.getType() : 0;
                    String stepWayPoints = routeStep != null ? Arrays.toString(routeStep.getWayPoints()) : null;
                    // Normal values
                    wayPoint.setLon(BigDecimal.valueOf(longitude));
                    wayPoint.setLat(BigDecimal.valueOf(latitude));
                    wayPoint.setEle(elevation);
                    wayPoint.setName(stepName);
                    wayPoint.setComment(stepInstruction);
                    // Extension values
                    wayPoint.setDistance(stepDistance);
                    wayPoint.setDuration(stepDuration);
                    wayPoint.setType(stepType);
                    wayPoint.setWayPointIdentifier(stepWayPoints);
                    //Add WayPoint to list
                    wayPointList.add(wayPoint);


                }
                Route finalRoute = new Route();
                finalRoute.setWayPointList(wayPointList);
                Bounds bounds = new Bounds(route);
                BigDecimal[] start = bounds.getStart();
                BigDecimal[] stop = bounds.getStop();
                // BigDecimal wayPoint1 = finalRoute.getWayPoint(0).getLat();
                GPX gpx = new GPX();
                gpx.addRoute(finalRoute);
                gpx.write();
                Builder gpxBuilder = new Builder(gpx);
                String gpxString = gpxBuilder.write();

            }
            int year = Calendar.getInstance().get(Calendar.YEAR);
            gpx = GPX.builder().addRoute(gpxRoute).creator("openrouteservice.org | OpenStreetMap contributors ".concat(String.valueOf(year))).build();
        }
        return gpx.toBuilder().creator("openrouteservice.org | OpenStreetMap contributors ".concat(String.valueOf(year))).build();
        // TODO: Import Conversion Code here
        // TODO: Get metadata from the Result into the gpx: creator, length, time etc.
        // http://www.topografix.com/GPX/1/1/#SchemaProperties

    }


}
