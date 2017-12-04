package heigit.ors.services.routing.requestprocessors.gpx;


import com.graphhopper.util.shapes.BBox;

import com.openrouteservice.orsgpx.*;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;
import heigit.ors.util.GeomUtility;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * {@link GpxRoutingResponseWriter} provides function(s) to convert OpenRouteService {@link RouteResult} to GPX.
 */
public class GpxRoutingResponseWriter {
    /**
     * @param routeResults The function needs a {@link RouteResult} as input.
     * @return It returns a XML string representation of the generated GPX
     * @throws JAXBException
     * @throws DatatypeConfigurationException
     */
    public static String toGPX(RouteResult[] routeResults) throws JAXBException, DatatypeConfigurationException {
        // TODO migrate own gpx solution
        // example: WayPoint.builder().extSpeed(20)

        Gpx gpx = new Gpx();
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
                    // the start and end points always cross with the points from the routesteps before and after
                    // to avoid duplicity the startpoint is raised by one if not zero or just one point ine the routestep
                    if (startPoint != 0 || wayPointNumber.length == 1) {
                        startPoint += 1;
                    }
                    // get end coordinate to look for in routeGeom
                    int endPoint = wayPointNumber[1];
                    // create a counter to avoid double entries
                    // Get the coordinate pair from routeGeom according to wayPointNumber. But stop at one before the endPoint to prevent duplicity
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
                        //wayPoint.setTime(cal);
                        WptTypeExtensions wptExtensions = new WptTypeExtensions();
                        wptExtensions.setDistance(routestep.getDistance());
                        wptExtensions.setDuration(routestep.getDuration());
                        wptExtensions.setType(routestep.getType());
                        wptExtensions.setStep(j);
                        wayPoint.setExtensions(wptExtensions);
                        route.getRtept().add(wayPoint);

                    }
                }

            }
            RteTypeExtensions extensions = new RteTypeExtensions();
            extensions.setDistance(routeResult.getSummary().getDistance());
            extensions.setDistanceActual(routeResult.getSummary().getDistanceActual());
            extensions.setDuration(routeResult.getSummary().getDuration());
            extensions.setAscent(routeResult.getSummary().getAscent());
            extensions.setDescent(routeResult.getSummary().getDescent());
            extensions.setAvgSpeed(routeResult.getSummary().getAverageSpeed());
            route.setExtensions(extensions);
            gpx.getRte().add(route);
        }

        BoundsType bounds = new BoundsType();
        bounds.setMinlat(BigDecimal.valueOf(bbox != null ? bbox.minLat : 0));
        bounds.setMinlon(BigDecimal.valueOf(bbox != null ? bbox.minLon : 0));
        bounds.setMaxlat(BigDecimal.valueOf(bbox != null ? bbox.maxLat : 0));
        bounds.setMaxlon(BigDecimal.valueOf(bbox != null ? bbox.maxLon : 0));
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
        gpx.setCreator("OpenRouteService");
        gpx.setVersion("1.1");
        //TODO: Link in Metadata?
        //TODO: keywords?
        //TODO: Extensions?
        return gpx.build();
    }


}
