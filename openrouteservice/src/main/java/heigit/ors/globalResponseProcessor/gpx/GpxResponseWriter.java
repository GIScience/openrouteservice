/*
 *
 *  *
 *  *  *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *  *  *
 *  *  *   http://www.giscience.uni-hd.de
 *  *  *   http://www.heigit.org
 *  *  *
 *  *  *  under one or more contributor license agreements. See the NOTICE file
 *  *  *  distributed with this work for additional information regarding copyright
 *  *  *  ownership. The GIScience licenses this file to you under the Apache License,
 *  *  *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  *  *  with the License. You may obtain a copy of the License at
 *  *  *
 *  *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  *  Unless required by applicable law or agreed to in writing, software
 *  *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  See the License for the specific language governing permissions and
 *  *  *  limitations under the License.
 *  *
 *
 */

package heigit.ors.globalResponseProcessor.gpx;


import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import heigit.ors.config.AppConfig;
import heigit.ors.exceptions.MissingConfigParameterException;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;
import heigit.ors.routing.RouteSummary;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.AppInfo;
import heigit.ors.util.GeomUtility;
import heigit.ors.globalResponseProcessor.gpx.beans.BoundsType;
import heigit.ors.globalResponseProcessor.gpx.beans.CopyrightType;
import heigit.ors.globalResponseProcessor.gpx.beans.EmailType;
import heigit.ors.globalResponseProcessor.gpx.beans.Gpx;
import heigit.ors.globalResponseProcessor.gpx.beans.GpxExtensions;
import heigit.ors.globalResponseProcessor.gpx.beans.LinkType;
import heigit.ors.globalResponseProcessor.gpx.beans.MetadataType;
import heigit.ors.globalResponseProcessor.gpx.beans.PersonType;
import heigit.ors.globalResponseProcessor.gpx.beans.RteType;
import heigit.ors.globalResponseProcessor.gpx.beans.RteTypeExtensions;
import heigit.ors.globalResponseProcessor.gpx.beans.WptType;
import heigit.ors.globalResponseProcessor.gpx.beans.WptTypeExtensions;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * {@link GpxResponseWriter} converts OpenRouteService {@link RouteResult} to GPX in a well formatted xml string representation.
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class GpxResponseWriter {

    /**
     * toGPX can be used to convert a  {@link RoutingRequest} and {@link RouteResult} to a gpx.
     * Specific values should be set in the App.config. If not, the process continues with empty values and a log4j error raised through {@link MissingConfigParameterException}.
     *
     * @param rreq         The {@link RoutingRequest} object holds route specific information like language...
     * @param routeResults The function needs a {@link RouteResult} as input.
     * @return It returns a XML {@link String} representation of the generated GPX
     * @throws Exception The class throws Exception cases
     */
    public static String toGPX(RoutingRequest rreq, RouteResult[] routeResults) throws Exception {
        boolean includeElevation = rreq.getIncludeElevation();
        Gpx gpx = new Gpx();
        BBox bbox = null;
        // In case of multiple routes there is no general BBox. So the first route will always deliver the general BBox.
        // When multiple routes are integrated, a method should be integrated to calculate a BBox of multiple BBoxes... For now it's enough!
        bbox = routeResults[0].getSummary().getBBox();
        // Access routeresults
        for (RouteResult route : routeResults) {
            RteType routeType = new RteType();
            LineString routeGeom;
            List<RouteSegment> route_segments = null;
            if (route.getGeometry() != null) {
                routeGeom = GeomUtility.createLinestring(route.getGeometry());
                int number_points = routeGeom.getNumPoints();

                for (int i = 0; i < number_points; i++) {
                    Point point = routeGeom.getPointN(i);
                    BigDecimal longitude = BigDecimal.valueOf(point.getCoordinate().x);
                    BigDecimal latitude = BigDecimal.valueOf(point.getCoordinate().y);
                    WptType wayPoint = new WptType();
                    if (includeElevation) {
                        BigDecimal elevation = BigDecimal.valueOf(point.getCoordinate().z);
                        wayPoint.setLat(latitude);
                        wayPoint.setLon(longitude);
                        wayPoint.setEle(elevation);
                    } else {
                        wayPoint.setLat(latitude);
                        wayPoint.setLon(longitude);
                    }
                    routeType.getRtept().add(wayPoint);
                }
                if (rreq.getIncludeInstructions() && route.getSegments().size() > 0) {
                    int route_step_iterator = route.getSegments().get(0).getSteps().size();
                    route_segments = route.getSegments();
                    for (int i = 0; i < route_step_iterator; i++) {
                        RouteStep routeStep = route_segments.get(0).getSteps().get(i);
                        WptType wayPoint = null;
                        int[] wayPointNumber = routeStep.getWayPoints();
                        int startPoint = wayPointNumber[0];
                        // the start and end points always cross with the points from the routesteps before and after
                        // to avoid duplicity the startpoint is raised by one if not zero or just one point ine the routestep
                        if (startPoint != 0 || wayPointNumber.length == 1) {
                            startPoint += 1;
                        }
                        int endPoint = wayPointNumber[1];

                        if (route.getGeometry().length > 0) {
                            int geometry_iterator = route.getGeometry().length;
                            for (int j = 0; j < geometry_iterator; j++) {
                                if (j >= startPoint && j <= endPoint) {
                                    wayPoint = routeType.getRtept().get(j);
                                    wayPoint.setName(routeStep.getName());
                                    wayPoint.setDesc(routeStep.getInstruction());
                                    // add extensions to waypoint
                                    wayPoint.setName(routeStep.getName());
                                    wayPoint.setDesc(routeStep.getInstruction());
                                    // add extensions to waypoint
                                    WptTypeExtensions wptExtensions = new WptTypeExtensions();
                                    wptExtensions.setDistance(routeStep.getDistance());
                                    wptExtensions.setDuration(routeStep.getDuration());
                                    wptExtensions.setType(routeStep.getType());
                                    wptExtensions.setStep(j);
                                    wayPoint.setExtensions(wptExtensions);
                                }
                            }
                        } else {
                            int false_geometry_iterator = route.getSegments().get(0).getSteps().get(route_step_iterator).getWayPoints()[1];
                            for (int j = 0; j <= false_geometry_iterator; j++) {
                                wayPoint = new WptType();
                                if (j >= startPoint && j <= endPoint) {
                                    wayPoint.setName(routeStep.getName());
                                    wayPoint.setDesc(routeStep.getInstruction());
                                    // add extensions to waypoint
                                    WptTypeExtensions wptExtensions = new WptTypeExtensions();
                                    wptExtensions.setDistance(routeStep.getDistance());
                                    wptExtensions.setDuration(routeStep.getDuration());
                                    wptExtensions.setType(routeStep.getType());
                                    wptExtensions.setStep(j);
                                    wayPoint.setExtensions(wptExtensions);
                                    routeType.getRtept().set(j, wayPoint);
                                }
                            }
                        }
                    }
                }
            }
            if (route.getSummary() != null) {
                RteTypeExtensions rteTypeExtensions = new RteTypeExtensions();
                RouteSummary route_summary = route.getSummary();
                rteTypeExtensions.setAscent(route_summary.getAscent());
                rteTypeExtensions.setAvgSpeed(route_summary.getAverageSpeed());
                rteTypeExtensions.setDescent(route_summary.getDescent());
                rteTypeExtensions.setDistance(route_summary.getDistance());
                rteTypeExtensions.setDuration(route_summary.getDuration());
                BoundsType bounds = new BoundsType();
                BBox routeBBox = route.getSummary().getBBox();
                bounds.setMinlat(BigDecimal.valueOf(routeBBox != null ? routeBBox.minLat : 0));
                bounds.setMinlon(BigDecimal.valueOf(routeBBox != null ? routeBBox.minLon : 0));
                bounds.setMaxlat(BigDecimal.valueOf(routeBBox != null ? routeBBox.maxLat : 0));
                bounds.setMaxlon(BigDecimal.valueOf(routeBBox != null ? routeBBox.maxLon : 0));
                rteTypeExtensions.setBounds(bounds);
                routeType.setExtensions(rteTypeExtensions);
                gpx.getRte().add(routeType);
            }
        }

        // Create and set boundaries
        BoundsType bounds = new BoundsType();
        bounds.setMinlat(BigDecimal.valueOf(bbox != null ? bbox.minLat : 0));
        bounds.setMinlon(BigDecimal.valueOf(bbox != null ? bbox.minLon : 0));
        bounds.setMaxlat(BigDecimal.valueOf(bbox != null ? bbox.maxLat : 0));
        bounds.setMaxlon(BigDecimal.valueOf(bbox != null ? bbox.maxLon : 0));
        // create and set gpx metadata in a if and else check process to avoid interruption
        MetadataType metadata = new MetadataType();
        metadata.setBounds(bounds);
        PersonType orsPerson = new PersonType();
        EmailType orsMail = new EmailType();
        if (AppConfig.Global().getParameter("info", "support_mail") != null) {
            try {
                String[] mail = AppConfig.Global().getParameter("info", "support_mail").split("@");
                orsMail.setDomain("@" + mail[1]);
                orsMail.setId(mail[0]);
                orsPerson.setEmail(orsMail);
            } catch (Exception ex) {
                orsMail.setDomain("");
                orsMail.setId("");
                orsPerson.setEmail(orsMail);
                new MissingConfigParameterException(GpxResponseWriter.class, "support_mail", "The parameter seems to be malformed");
            }
        } else {
            orsMail.setDomain("");
            orsMail.setId("");
            orsPerson.setEmail(orsMail);
            new MissingConfigParameterException(GpxResponseWriter.class, "support_mail");
        }

        LinkType orsLink = new LinkType();
        // set base_url
        if (AppConfig.Global().getParameter("info", "base_url") != null) {
            orsLink.setHref(AppConfig.Global().getParameter("info", "base_url"));
            orsLink.setText(AppConfig.Global().getParameter("info", "base_url"));
            orsLink.setType("text/html");
            orsPerson.setLink(orsLink);
        } else {
            orsLink.setHref("");
            orsLink.setText("");
            orsLink.setType("text/html");
            orsPerson.setLink(orsLink);
            new MissingConfigParameterException(GpxResponseWriter.class, "base_url");
        }

        // set author_tag
        if (AppConfig.Global().getParameter("info", "author_tag") != null) {
            orsPerson.setName(AppConfig.Global().getParameter("info", "author_tag"));
        } else {
            orsPerson.setName("");
            new MissingConfigParameterException(GpxResponseWriter.class, "author_tag");
        }
        metadata.setAuthor(orsPerson);

        // set copyright
        CopyrightType copyright = new CopyrightType();
        if (RoutingServiceSettings.getAttribution() != null) {
            copyright.setAuthor(RoutingServiceSettings.getAttribution());

        } else {
            copyright.setAuthor("");
            new MissingConfigParameterException(GpxResponseWriter.class, "attribution");
        }
        // set content_licence
        if (AppConfig.Global().getParameter("info", "content_licence") != null) {
            copyright.setLicense(AppConfig.Global().getParameter("info", "content_licence"));
        } else {
            copyright.setLicense("");
            new MissingConfigParameterException(GpxResponseWriter.class, "content_licence");
        }
        // create and set current date as XMLGregorianCalendar element
        Date date = new Date();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        copyright.setYear(cal);
        // Set the metadata information
        metadata.setCopyright(copyright);
        if (RoutingServiceSettings.getParameter("routing_description") != null) {

            metadata.setDesc(RoutingServiceSettings.getParameter("routing_description"));
        } else {
            metadata.setDesc("");
            new MissingConfigParameterException(GpxResponseWriter.class, "routing_description");
        }
        // set routing_name
        metadata.setName(RoutingServiceSettings.getRoutingName());
        metadata.setTime(cal);
        gpx.setMetadata(metadata);
        // set author_tag
        if (AppConfig.Global().getParameter("info", "author_tag") != null) {
            gpx.setCreator(AppConfig.Global().getParameter("info", "author_tag"));
        } else {
            gpx.setCreator("");
            new MissingConfigParameterException(GpxResponseWriter.class, "author_tag");
        }

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
        return gpx.build();
    }
}
