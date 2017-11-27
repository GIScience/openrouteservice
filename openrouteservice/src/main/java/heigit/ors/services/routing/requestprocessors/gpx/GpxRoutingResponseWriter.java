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

import javax.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GpxRoutingResponseWriter {
    /* There will be no LineString check for now.
    All calculations that reach this point will anyway be only LineStrings */
    public static String toGPX(RoutingRequest request, RouteResult[] routeResults, JSONObject json) throws JAXBException {
        // TODO migrate own gpx solution
        // example: WayPoint.builder().extSpeed(20)
        GPX gpx = new GPX();


        for (RouteResult routeResult : routeResults) {
            LineString routeGeom = GeomUtility.createLinestring(routeResult.getGeometry());
            //TODO get the Bounding "box" or the Bounds parameters here for the gpx
            for (RouteSegment segment : routeResult.getSegments()) {
                List<RouteStep> routeSteps = segment.getSteps();
                List<WayPoint> wayPointList = new ArrayList<>();
                for (int i = 0; i < routeGeom.getLength(); i++) {
                    RouteStep routeStep = null;
                    for (RouteStep element : routeSteps) {
                        int[] wayPoints = element.getWayPoints();
                        if (i < wayPoints[0] || i > wayPoints[1]) {
                            routeStep = element;
                            break;
                        }


                        // Get Point for Geometry
                        Point point = routeGeom.getPointN(i);
                        WayPoint wayPoint = null;
                        double longitude = point.getCoordinate().x;
                        double latitude = point.getCoordinate().y;
                        double elevation = point.getCoordinate().z;
                        // TODO check if the routeStep should be integrated
                        // String stepWayPoints = routeStep != null ? Arrays.toString(routeStep.getWayPoints()) : null;
                        // Normal values
                        if (!Double.valueOf(point.getCoordinate().z).isNaN()) {
                            wayPoint = new WayPoint(longitude, latitude, elevation);
                        }
                        else{
                            wayPoint = new WayPoint(longitude, latitude);
                        }

                        wayPoint.set_name(routeStep != null ? routeStep.getName() : "");
                        wayPoint.set_description(routeStep != null ? routeStep.getInstruction() : "");
                        // Create set for Extensions and add them
                        wayPoint.createExtSet();
                        wayPoint.add_extension("distance", routeStep != null ? routeStep.getDistance() : 0);
                        wayPoint.add_extension("duration", routeStep != null ? routeStep.getDuration() : 0);
                        wayPoint.add_extension("type", routeStep != null ? routeStep.getType() : 0);
                        //Add WayPoint to list
                        wayPointList.add(wayPoint);
                    }
                }

                Route route = new Route(wayPointList);

                // TODO add Bounds when everything runs!
                //Bounds bounds = new Bounds(route);
                //BigDecimal[] start = bounds.getStart();
                //BigDecimal[] stop = bounds.getStop();
                // BigDecimal wayPoint1 = finalRoute.getWayPoint(0).getLat();
                gpx.addRoute(route);

            }
            // int year = Calendar.getInstance().get(Calendar.YEAR);
            // gpx = GPX.builder().addRoute(gpxRoute).creator("openrouteservice.org | OpenStreetMap contributors ".concat(String.valueOf(year))).build();
        }
        return new Builder().build(gpx);
        //return gpx.toBuilder().creator("openrouteservice.org | OpenStreetMap contributors ".concat(String.valueOf(year))).build();
        // http://www.topografix.com/GPX/1/1/#SchemaProperties

    }


}
