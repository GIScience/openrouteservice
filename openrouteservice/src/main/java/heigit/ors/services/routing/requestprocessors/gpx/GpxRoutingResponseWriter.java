package heigit.ors.services.routing.requestprocessors.gpx;

import com.graphhopper.util.GPXEntry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import heigit.ors.geomconverter.GeojsonToGpx;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.util.GeomUtility;


import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static java.awt.SystemColor.info;

public class GpxRoutingResponseWriter {
    /* There will be no LineString check for now.
    All calculations that reach this point will anyway be only LineStrings */
    public static GPX toGPX(RoutingRequest request, RouteResult[] routeResults, JSONObject json) {
        // TODO migrate back to io.jenetics.jpx.GPX
        // TODO extend io.jenetics.jpx.GPX for certain extensions
        // example: WayPoint.builder().extSpeed(20)
        GPX gpx;

        List<WptType> wayPointList = new ArrayList<>();
        int nRoutes = routeResults.length;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        GPX gpx = null;
        ObjectFactory objectFactory = null;
        RteType gpxRoute = objectFactory.createRteType();


        for (RouteResult route : routeResults) {
            LineString routeGeom = GeomUtility.createLinestring(route.getGeometry());
            for (RouteSegment segment : route.getSegments()) {
                List<RouteStep> routeSteps = segment.getSteps();
                for (int i = 0; i < routeGeom.getLength(); i++) {
                    WptType wayPoint = objectFactory.createWptType();
                    RouteStep routeStep = null;
                    for (RouteStep element : routeSteps) {
                        int[] wayPoints = element.getWayPoints();
                        if (i < wayPoints[0] || i > wayPoints[1]) {
                            routeStep = element;
                            break;
                        }


                    }
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

                    wayPoint.setLon(BigDecimal.valueOf(longitude));
                    wayPoint.setLat(BigDecimal.valueOf(latitude));

                    if (!Double.isNaN(elevation)) {
                        wayPoint.setEle(BigDecimal.valueOf(elevation));
                    }
                    if (stepDistance != 0){
                        // TODO extension Distance
                    }
                    if (stepDuration != 0){
                        // TODO extension Duration
                    }
                    if (stepInstruction != null){
                        wayPoint.setDesc(stepInstruction);
                    }
                    if (stepName != null){
                        wayPoint.setName(stepName);
                    }
                    if (stepType != 0){
                        // TODO extension stepType
                    }
                    /* stepWayPoints returns an int[] as a string that represents all associated WayPoints.
                    * Could be helpful to users who would like to get all connected waypoints in an efficient way.
                    * */
                    if (stepWayPoints != null){
                        // TODO extension stepWayPoints
                    }
                    wayPointList.add(wayPoint);
                    RteType rte = objectFactory.createRteType().set;
                }

            }
            gpx = GPX.builder().addRoute(gpxRoute).creator("openrouteservice.org | OpenStreetMap contributors ".concat(String.valueOf(year))).build();
        }
        return gpx.toBuilder().creator("openrouteservice.org | OpenStreetMap contributors ".concat(String.valueOf(year))).build();
        // TODO: Import Conversion Code here
        // TODO: Get metadata from the Result into the gpx: creator, length, time etc.
        // http://www.topografix.com/GPX/1/1/#SchemaProperties

    }


}
