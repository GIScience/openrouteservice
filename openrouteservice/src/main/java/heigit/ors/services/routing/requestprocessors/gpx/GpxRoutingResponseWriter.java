package heigit.ors.services.routing.requestprocessors.gpx;


import com.graphhopper.util.shapes.BBox;

import com.openrouteservice.orsgpx.*;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import heigit.ors.config.AppConfig;
import heigit.ors.routing.*;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.AppInfo;
import heigit.ors.util.GeomUtility;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * {@link GpxRoutingResponseWriter} provides function(s) to convert OpenRouteService {@link RouteResult} to GPX.
 */
public class GpxRoutingResponseWriter {
    /**
     * @param rreq         The {@link RoutingRequest} object holds route specific information like language...
     * @param routeResults The function needs a {@link RouteResult} as input.
     * @return It returns a XML {@link String} representation of the generated GPX
     * @throws JAXBException
     * @throws DatatypeConfigurationException
     */
    public static String toGPX(RoutingRequest rreq, RouteResult[] routeResults) throws JAXBException, DatatypeConfigurationException {
        boolean includeElevation = rreq.getIncludeElevation();
        Gpx gpx = new Gpx();
        BBox bbox = null;
        // Access routeresults
        if (routeResults != null) {
            for (RouteResult routeResult : routeResults) {
                bbox = routeResult.getSummary().getBBox();
                RteType route = new RteType();
                // Access segments
                if (routeResult.getSegments().size() > 0) {
                    LineString routeGeom = GeomUtility.createLinestring(routeResult.getGeometry());

                    for (RouteSegment segment : routeResult.getSegments()) {

                        for (RouteStep routestep : segment.getSteps()) {
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
                                // Create waypoint
                                WptType wayPoint = new WptType();
                                // look for elevation
                                if (includeElevation) {
                                    // add coordinates to waypoint
                                    BigDecimal elevation = BigDecimal.valueOf(point.getCoordinate().z);
                                    wayPoint.setLat(latitude);
                                    wayPoint.setLon(longitude);
                                    wayPoint.setEle(elevation);
                                } else {
                                    // add coordinates to waypoint
                                    wayPoint.setLat(latitude);
                                    wayPoint.setLon(longitude);
                                }

                                // add additional information to waypoint;
                                wayPoint.setName(routestep.getName());
                                wayPoint.setDesc(routestep.getInstruction());
                                // add extensions to waypoint
                                WptTypeExtensions wptExtensions = new WptTypeExtensions();
                                wptExtensions.setDistance(routestep.getDistance());
                                wptExtensions.setDuration(routestep.getDuration());
                                wptExtensions.setType(routestep.getType());
                                wptExtensions.setStep(j);
                                wayPoint.setExtensions(wptExtensions);
                                // add waypoint the the routepoint list
                                route.getRtept().add(wayPoint);

                            }
                        }

                    }
                    // create and add extensions to the route
                    RteTypeExtensions extensions = new RteTypeExtensions();
                    extensions.setDistance(routeResult.getSummary().getDistance());
                    extensions.setDistanceActual(routeResult.getSummary().getDistanceActual());
                    extensions.setDuration(routeResult.getSummary().getDuration());
                    extensions.setAscent(routeResult.getSummary().getAscent());
                    extensions.setDescent(routeResult.getSummary().getDescent());
                    extensions.setAvgSpeed(routeResult.getSummary().getAverageSpeed());
                    route.setExtensions(extensions);
                    // add the finished route to the gpx
                    gpx.getRte().add(route);
                }
            }
        }
        // Create and set boundaries
        BoundsType bounds = new BoundsType();
        bounds.setMinlat(BigDecimal.valueOf(bbox != null ? bbox.minLat : 0));
        bounds.setMinlon(BigDecimal.valueOf(bbox != null ? bbox.minLon : 0));
        bounds.setMaxlat(BigDecimal.valueOf(bbox != null ? bbox.maxLat : 0));
        bounds.setMaxlon(BigDecimal.valueOf(bbox != null ? bbox.maxLon : 0));
        // create and set gpx metadata
        MetadataType metadata = new MetadataType();
        metadata.setBounds(bounds);
        PersonType orsPerson = new PersonType();
        EmailType orsMail = new EmailType();
        //TODO in appconfig integrieren
        String[] mail = AppConfig.Global().getParameter("info", "mail_domain").split("@");
        orsMail.setDomain("@" + mail[1]);
        orsMail.setId(mail[0]);
        orsPerson.setEmail(orsMail);
        LinkType orsLink = new LinkType();
        orsLink.setHref(AppConfig.Global().getParameter("info", "base_url"));
        orsLink.setText(AppConfig.Global().getParameter("info", "base_url"));
        orsLink.setType("text/html");
        orsPerson.setLink(orsLink);
        orsPerson.setName(AppConfig.Global().getParameter("info", "author_tag"));
        metadata.setAuthor(orsPerson);
        // create and set copyright
        CopyrightType copyright = new CopyrightType();
        copyright.setAuthor(RoutingServiceSettings.getAttribution());
        copyright.setLicense(AppConfig.Global().getParameter("info", "content_licence"));
        // create and set current date as XMLGregorianCalendar element
        Date date = new Date();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        copyright.setYear(cal);
        metadata.setCopyright(copyright);
        metadata.setDesc(RoutingServiceSettings.getParameter("routing_description"));
        metadata.setName(RoutingServiceSettings.getParameter("routing_name"));
        metadata.setTime(cal);
        gpx.setMetadata(metadata);
        gpx.setCreator(AppConfig.Global().getParameter("info", "author_tag"));
        // set gpx extensions
        GpxExtensions gpxExtensions = new GpxExtensions();
        gpxExtensions.setAttribution(RoutingServiceSettings.getAttribution());
        gpxExtensions.setElevation(String.valueOf(includeElevation));
        gpxExtensions.setEngine(AppInfo.VERSION);
        gpxExtensions.setBuild_date(AppInfo.BUILD_DATE);
        gpxExtensions.setInstructions(String.valueOf(rreq.getIncludeInstructions()));
        gpxExtensions.setLanguage(rreq.getLanguage());
        gpxExtensions.setPreference(RoutingProfileType.getName(rreq.getSearchParameters().getWeightingMethod()));
        gpxExtensions.setProfile(WeightingMethod.getName(rreq.getSearchParameters().getProfileType()));
        gpxExtensions.setDistance_units(rreq.getUnits().name());
        gpx.setExtensions(gpxExtensions);
        //TODO: Link in Metadata?
        //TODO: keywords?
        //TODO: Extensions?
        // return the gpx alement as a finished XML element in string representation
        return gpx.build();
    }


}
