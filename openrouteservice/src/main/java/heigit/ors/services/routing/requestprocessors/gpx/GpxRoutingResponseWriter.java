package heigit.ors.services.routing.requestprocessors.gpx;


import com.graphhopper.util.shapes.BBox;
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
import java.util.ArrayList;
import java.util.List;

public class GpxRoutingResponseWriter {
    /* There will be no LineString check for now.
    All calculations that reach this point will anyway be only LineStrings */
    public static String toGPX(RoutingRequest request, RouteResult[] routeResults, JSONObject json) throws JAXBException {
        // TODO migrate own gpx solution
        // example: WayPoint.builder().extSpeed(20)
        GPX gpx = new GPX();
        BBox bbox = null;

        for (RouteResult routeResult : routeResults) {
            LineString routeGeom = GeomUtility.createLinestring(routeResult.getGeometry());
            //TODO get the Bounding "box" or the Bounds parameters here for the gpx
            for (RouteSegment segment : routeResult.getSegments()) {
                bbox = segment.getBBox();
                List<RouteStep> routeSteps = segment.getSteps();
                List<WayPoint> wayPointList = new ArrayList<>();
                for (RouteStep routestep : routeSteps) {
                    // Get the id of the coordinates to look for them inside routeGeom and assign them to the WayPoint
                    int[] wayPointNumber = routestep.getWayPoints();
                    // get start coordinate to look for in routeGeom
                    int startPoint = wayPointNumber[0];
                    // get end coordinate to look for in routeGeom
                    int endPoint = wayPointNumber[1];
                    // Get the x coordinate pair from routeGeom according to wayPointNumber
                    for (int j = startPoint; j <= endPoint; j++) {

                        // Get geometry of the actual Point
                        Point point = routeGeom.getPointN(j);
                        double longitude = point.getCoordinate().x;
                        double latitude = point.getCoordinate().y;
                        double elevation = point.getCoordinate().z;
                        // Create waypoint to start adding point geometries
                        WayPoint wayPoint;
                        if (!Double.isNaN(elevation)) {
                            wayPoint = new WayPoint(latitude, longitude, elevation);
                        } else {
                            wayPoint = new WayPoint(latitude, longitude);
                        }
                        // add additional information to point
                        wayPoint.set_name(routestep.getName());
                        wayPoint.set_description(routestep.getInstruction());
                        // Create set for Extensions and add them
                        wayPoint.add_extension("distance", routestep.getDistance());
                        wayPoint.add_extension("duration", routestep.getDuration());
                        wayPoint.add_extension("type", routestep.getType());
                        wayPoint.add_extension("step", j);
                        //Add WayPoint to list
                        wayPointList.add(wayPoint);


                    }
                }

                Route route = new Route(wayPointList);
                route.addExtension("distance", segment.getDistance());
                route.addExtension("duration", segment.getDuration());
                route.addExtension("ascent", segment.getAscent());
                route.addExtension("descent", segment.getDescent());
                route.addExtension("detourFactor", segment.getDetourFactor());
                // TODO add Bounds when everything runs!
                gpx.addRoute(route);

            }
            // int year = Calendar.getInstance().get(Calendar.YEAR);
            // gpx = GPX.builder().addRoute(gpxRoute).creator("openrouteservice.org | OpenStreetMap contributors ".concat(String.valueOf(year))).build();
        }
        Bounds bounds = new Bounds(bbox.minLat, bbox.minLon, bbox.maxLat, bbox.maxLon);
        Metadata metadata = new Metadata();
        metadata.setBounds(bounds);
        gpx.setMetadata(metadata);
        return new Builder().build(gpx);
        //return gpx.toBuilder().creator("openrouteservice.org | OpenStreetMap contributors ".concat(String.valueOf(year))).build();
        // http://www.topografix.com/GPX/1/1/#SchemaProperties

    }


}
