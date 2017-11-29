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
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class GpxRoutingResponseWriter {
    /* There will be no LineString check for now.
    All calculations that reach this point will anyway be only LineStrings */
    public static String toGPX(RoutingRequest request, RouteResult[] routeResults, JSONObject json) throws JAXBException, DatatypeConfigurationException {
        // TODO migrate own gpx solution
        // example: WayPoint.builder().extSpeed(20)

        GpxType gpx = new GpxType();
        BBox bbox = null;
        // Get current date to insert into Waypoint, Route and GPX
        Date date = new Date();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        for (RouteResult routeResult : routeResults) {
            bbox = routeResult.getSummary().getBBox();
            RteType route = new RteType();
            LineString routeGeom = GeomUtility.createLinestring(routeResult.getGeometry());
            //TODO get the Bounding "box" or the Bounds parameters here for the gpx
            for (RouteSegment segment : routeResult.getSegments()) {

                List<RouteStep> routeSteps = segment.getSteps();

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
                        BigDecimal longitude = BigDecimal.valueOf(point.getCoordinate().x);
                        BigDecimal latitude = BigDecimal.valueOf(point.getCoordinate().y);
                        double elevationCheck = point.getCoordinate().z;
                        // Create waypoint to start adding point geometries
                        WptType wayPoint = new WptType();
                        if (!Double.isNaN(elevationCheck)) {
                            BigDecimal elevation = BigDecimal.valueOf(point.getCoordinate().z);
                            wayPoint.setLat(latitude);
                            wayPoint.setLon(longitude);
                            wayPoint.setEle(elevation);
                        } else {
                            wayPoint.setLat(latitude);
                            wayPoint.setLon(longitude);
                        }
                        // add additional information to point;
                        wayPoint.setName(routestep.getName());
                        wayPoint.setDesc(routestep.getInstruction());
                        wayPoint.setTime(cal);
                        // Create set for Extensions and add them
                        ExtensionsType wayPointExt = new ExtensionsType();
                        HashMap<String, Object> extensionList = new HashMap<>();
                        extensionList.put("distance", routestep.getDistance());
                        extensionList.put("duration", routestep.getDuration());
                        extensionList.put("type", routestep.getType());
                        extensionList.put("step", j);
                        //Add WayPoint to list
                        wayPointExt.getAny().add(extensionList);
                        wayPoint.setExtensions(wayPointExt);
                        route.getRtept().add(wayPoint);


                    }
                }

            }
            ExtensionsType routeExtensions = new ExtensionsType();
            HashMap<String, Object> extensionsList = new HashMap<>();
            extensionsList.put("distance", routeResult.getSummary().getDistance());
            extensionsList.put("distanceActual", routeResult.getSummary().getDistanceActual());
            extensionsList.put("duration", routeResult.getSummary().getDuration());
            extensionsList.put("ascent", routeResult.getSummary().getAscent());
            extensionsList.put("descent", routeResult.getSummary().getDescent());
            extensionsList.put("avgSpeed", routeResult.getSummary().getAverageSpeed());
            routeExtensions.getAny().add(extensionsList);
            route.setExtensions(routeExtensions);
            gpx.getRte().add(route);
        }
//        route.setCmt();
//        route.setDesc();
//        route.setExtensions();
//        route.setName();

        BoundsType bounds = new BoundsType();
        bounds.setMinlat(BigDecimal.valueOf(bbox.minLat));
        bounds.setMinlon(BigDecimal.valueOf(bbox.minLon));
        bounds.setMaxlat(BigDecimal.valueOf(bbox.maxLat));
        bounds.setMaxlon(BigDecimal.valueOf(bbox.maxLon));
        MetadataType metadata = new MetadataType();
        metadata.setBounds(bounds);
        PersonType orsPerson = new PersonType();
        EmailType orsMail = new EmailType();
        orsMail.setDomain("@openrouteservice.org");
        orsMail.setId("support");
        orsPerson.setEmail(orsMail);
        LinkType orsLink = new LinkType();
        orsLink.setHref("https://www.openrouteservice.org/");
        orsLink.setText("https://www.openrouteservice.org/");
        orsLink.setType("text/html");
        orsPerson.setLink(orsLink);
        orsPerson.setName("OpenRouteService");
        metadata.setAuthor(orsPerson);
        CopyrightType copyright = new CopyrightType();
        copyright.setAuthor("OpenStreetMap contributor");
        copyright.setLicense("CC BY-SA");
        copyright.setYear(cal);
        metadata.setCopyright(copyright);
        metadata.setDesc("This is a GPX routing file from OpenRouteService");
        metadata.setName("OpenRouteService Routing");

        metadata.setTime(cal);
        gpx.setMetadata(metadata);

        //return gpx.toBuilder().creator("openrouteservice.org | OpenStreetMap contributors ".concat(String.valueOf(year))).build();
        // http://www.topografix.com/GPX/1/1/#SchemaProperties
        XMLBuilder builder = new XMLBuilder();
        return builder.Build(gpx);
    }


}
