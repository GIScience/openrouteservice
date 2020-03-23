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

package org.heigit.ors.globalresponseprocessor.gpx;


import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import org.heigit.ors.api.util.SystemMessage;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.globalresponseprocessor.gpx.beans.*;
import org.heigit.ors.routing.*;
import org.heigit.ors.services.routing.RoutingServiceSettings;
import org.heigit.ors.util.AppInfo;
import org.heigit.ors.util.ErrorLoggingUtility;
import org.heigit.ors.util.GeomUtility;

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

    private static final String PARAM_NAME_SUPPORT_MAIL = "support_mail";
    private static final String PARAM_NAME_BASE_URL = "base_url";
    private static final String PARAM_NAME_AUTHOR_TAG = "author_tag";
    private static final String PARAM_NAME_CONTENT_LICENCE = "content_licence";
    private static final String PARAM_NAME_ROUTING_DESCRIPTION = "routing_description";

    private GpxResponseWriter () {}

    /**
     * toGPX can be used to convert a  {@link RoutingRequest} and {@link RouteResult} to a gpx.
     * Specific values should be set in the App.config. If not, the process continues with empty values and a log4j error message.
     *
     * @param rreq         The {@link RoutingRequest} object holds route specific information like language...
     * @param routeResults The function needs a {@link RouteResult} as input.
     * @return It returns a XML {@link String} representation of the generated GPX
     * @throws Exception The class throws Exception cases
     */
    public static String toGPX(RoutingRequest rreq, RouteResult[] routeResults) throws Exception {
        boolean includeElevation = rreq.getIncludeElevation();
        Gpx gpx = new Gpx();
        // In case of multiple routes there is no general BBox. So the first route will always deliver the general BBox.
        // When multiple routes are integrated, a method should be integrated to calculate a BBox of multiple BBoxes... For now it's enough!
        BBox bbox = routeResults[0].getSummary().getBBox();
        // Access routeresults
        for (RouteResult route : routeResults) {
            RteType routeType = new RteType();
            LineString routeGeom;
            if (route.getGeometry() != null) {
                routeGeom = GeomUtility.createLinestring(route.getGeometry());
                int numPoints = routeGeom.getNumPoints();

                for (int i = 0; i < numPoints; i++) {
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
                if (rreq.getIncludeInstructions() && !route.getSegments().isEmpty()) {
                    int routeStepIterator = route.getSegments().get(0).getSteps().size();
                    List<RouteSegment> routeSegments = route.getSegments();
                    for (int i = 0; i < routeStepIterator; i++) {
                        RouteStep routeStep = routeSegments.get(0).getSteps().get(i);
                        int[] wayPointNumber = routeStep.getWayPoints();
                        int startPoint = wayPointNumber[0];
                        // the start and end points always cross with the points from the routesteps before and after
                        // to avoid duplicity the startpoint is raised by one if not zero or just one point ine the routestep
                        if (startPoint != 0 || wayPointNumber.length == 1) {
                            startPoint += 1;
                        }
                        int endPoint = wayPointNumber[1];

                        if (route.getGeometry().length > 0) {
                            int geometryIterator = route.getGeometry().length;
                            for (int j = 0; j < geometryIterator; j++) {
                                if (j >= startPoint && j <= endPoint) {
                                    WptType wayPoint = routeType.getRtept().get(j);
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
                            int falseGeometryIterator = route.getSegments().get(0).getSteps().get(routeStepIterator).getWayPoints()[1];
                            for (int j = 0; j <= falseGeometryIterator; j++) {
                                WptType wayPoint = new WptType();
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
                RouteSummary routeSummary = route.getSummary();
                rteTypeExtensions.setAscent(routeSummary.getAscent());
                rteTypeExtensions.setAvgspeed(routeSummary.getAverageSpeed());
                rteTypeExtensions.setDescent(routeSummary.getDescent());
                rteTypeExtensions.setDistance(routeSummary.getDistance());
                rteTypeExtensions.setDuration(routeSummary.getDuration());
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
        if (AppConfig.getGlobal().getParameter("info", PARAM_NAME_SUPPORT_MAIL) != null) {
            try {
                String[] mail = AppConfig.getGlobal().getParameter("info", PARAM_NAME_SUPPORT_MAIL).split("@");
                orsMail.setDomain("@" + mail[1]);
                orsMail.setId(mail[0]);
                orsPerson.setEmail(orsMail);
            } catch (Exception ex) {
                orsMail.setDomain("");
                orsMail.setId("");
                orsPerson.setEmail(orsMail);
                ErrorLoggingUtility.logMissingConfigParameter(GpxResponseWriter.class, PARAM_NAME_SUPPORT_MAIL, "The parameter seems to be malformed");
            }
        } else {
            orsMail.setDomain("");
            orsMail.setId("");
            orsPerson.setEmail(orsMail);
            ErrorLoggingUtility.logMissingConfigParameter(GpxResponseWriter.class, PARAM_NAME_SUPPORT_MAIL);
        }

        LinkType orsLink = new LinkType();
        // set base_url
        if (AppConfig.getGlobal().getParameter("info", PARAM_NAME_BASE_URL) != null) {
            orsLink.setHref(AppConfig.getGlobal().getParameter("info", PARAM_NAME_BASE_URL));
            orsLink.setText(AppConfig.getGlobal().getParameter("info", PARAM_NAME_BASE_URL));
            orsLink.setType("text/html");
            orsPerson.setLink(orsLink);
        } else {
            orsLink.setHref("");
            orsLink.setText("");
            orsLink.setType("text/html");
            orsPerson.setLink(orsLink);
            ErrorLoggingUtility.logMissingConfigParameter(GpxResponseWriter.class, PARAM_NAME_BASE_URL);
        }

        // set author_tag
        if (AppConfig.getGlobal().getParameter("info", PARAM_NAME_AUTHOR_TAG) != null) {
            orsPerson.setName(AppConfig.getGlobal().getParameter("info", PARAM_NAME_AUTHOR_TAG));
        } else {
            orsPerson.setName("");
            ErrorLoggingUtility.logMissingConfigParameter(GpxResponseWriter.class, PARAM_NAME_AUTHOR_TAG);
        }
        metadata.setAuthor(orsPerson);

        // set copyright
        CopyrightType copyright = new CopyrightType();
        if (RoutingServiceSettings.getAttribution() != null) {
            copyright.setAuthor(RoutingServiceSettings.getAttribution());

        } else {
            copyright.setAuthor("");
            ErrorLoggingUtility.logMissingConfigParameter(GpxResponseWriter.class, "attribution");
        }
        // set content_licence
        if (AppConfig.getGlobal().getParameter("info", PARAM_NAME_CONTENT_LICENCE) != null) {
            copyright.setLicense(AppConfig.getGlobal().getParameter("info", PARAM_NAME_CONTENT_LICENCE));
        } else {
            copyright.setLicense("");
            ErrorLoggingUtility.logMissingConfigParameter(GpxResponseWriter.class, PARAM_NAME_CONTENT_LICENCE);
        }
        // create and set current date as XMLGregorianCalendar element
        Date date = new Date();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        copyright.setYear(cal);
        // Set the metadata information
        metadata.setCopyright(copyright);

        MetadataTypeExtensions ext = new MetadataTypeExtensions();
        ext.setSystemMessage(SystemMessage.getSystemMessage(rreq));
        metadata.setExtensions(ext);

        if (RoutingServiceSettings.getParameter(PARAM_NAME_ROUTING_DESCRIPTION) != null) {

            metadata.setDesc(RoutingServiceSettings.getParameter(PARAM_NAME_ROUTING_DESCRIPTION));
        } else {
            metadata.setDesc("");
            ErrorLoggingUtility.logMissingConfigParameter(GpxResponseWriter.class, PARAM_NAME_ROUTING_DESCRIPTION);
        }
        // set routing_name
        metadata.setName(RoutingServiceSettings.getRoutingName());
        metadata.setTime(cal);
        gpx.setMetadata(metadata);
        // set author_tag
        if (AppConfig.getGlobal().getParameter("info", PARAM_NAME_AUTHOR_TAG) != null) {
            gpx.setCreator(AppConfig.getGlobal().getParameter("info", PARAM_NAME_AUTHOR_TAG));
        } else {
            gpx.setCreator("");
            ErrorLoggingUtility.logMissingConfigParameter(GpxResponseWriter.class, PARAM_NAME_AUTHOR_TAG);
        }

        // set gpx extensions
        GpxExtensions gpxExtensions = new GpxExtensions();
        gpxExtensions.setAttribution(RoutingServiceSettings.getAttribution());
        gpxExtensions.setElevation(String.valueOf(includeElevation));
        gpxExtensions.setEngine(AppInfo.VERSION);
        gpxExtensions.setBuildDate(AppInfo.BUILD_DATE);
        gpxExtensions.setInstructions(String.valueOf(rreq.getIncludeInstructions()));
        gpxExtensions.setLanguage(rreq.getLanguage());
        gpxExtensions.setPreference(RoutingProfileType.getName(rreq.getSearchParameters().getWeightingMethod()));
        gpxExtensions.setProfile(WeightingMethod.getName(rreq.getSearchParameters().getProfileType()));
        gpxExtensions.setDistanceUnits(rreq.getUnits().name());
        gpx.setExtensions(gpxExtensions);
        return gpx.build();
    }
}
