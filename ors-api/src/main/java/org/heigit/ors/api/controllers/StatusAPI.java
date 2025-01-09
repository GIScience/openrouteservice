/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api.controllers;

import com.graphhopper.routing.ev.EncodedValue;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.util.AppInfo;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.localization.LocalizationManager;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@Tag(name = "Status service", description = "Get information on the status of the api")
@RequestMapping("/v2/status")
public class StatusAPI {

    private final EndpointsProperties endpointsProperties;

    public StatusAPI(EndpointsProperties endpointsProperties) {
        this.endpointsProperties = endpointsProperties;
    }

    @GetMapping
    public ResponseEntity getStatus(HttpServletRequest request) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject jInfo = new JSONObject(true);

        jInfo.put("engine", AppInfo.getEngineInfo());

        if (RoutingProfileManagerStatus.isReady()) {
            RoutingProfileManager profileManager = RoutingProfileManager.getInstance();

            if (!profileManager.getUniqueProfiles().isEmpty()) {

                List<String> list = new ArrayList<>(4);
                if (endpointsProperties.getRouting().isEnabled())
                    list.add("routing");
                if (endpointsProperties.getIsochrones().isEnabled())
                    list.add("isochrones");
                if (endpointsProperties.getMatrix().isEnabled())
                    list.add("matrix");
                if (endpointsProperties.getSnap().isEnabled())
                    list.add("snap");
                jInfo.put("services", list);
                jInfo.put("languages", LocalizationManager.getInstance().getLanguages());

                JSONObject jProfiles = new JSONObject(true);
                for (RoutingProfile rp : profileManager.getUniqueProfiles()) {
                    ProfileProperties profile = rp.getProfileConfiguration();
                    JSONObject jProfileProps = new JSONObject(true);

                    jProfileProps.put("encoder_name", profile.getEncoderName().getEncoderName());
                    jProfileProps.put("graph_build_date", rp.getGraphProperties().get("datareader.import.date"));
                    jProfileProps.put("osm_date", rp.getGraphProperties().get("datareader.data.date"));

                    JSONObject jProfileLimits = new JSONObject();
                    if (profile.getService().getMaximumDistance() != null)
                        jProfileLimits.put("maximum_distance", profile.getService().getMaximumDistance());
                    if (profile.getService().getMaximumDistanceDynamicWeights() != null)
                        jProfileLimits.put("maximum_distance_dynamic_weights", profile.getService().getMaximumDistanceDynamicWeights());
                    if (profile.getService().getMaximumDistanceAvoidAreas() != null)
                        jProfileLimits.put("maximum_distance_avoid_areas", profile.getService().getMaximumDistanceAvoidAreas());
                    if (profile.getService().getMaximumWayPoints() != null)
                        jProfileLimits.put("maximum_waypoints", profile.getService().getMaximumWayPoints());

                    if (!jProfileLimits.isEmpty())
                        jProfileProps.put("limits", jProfileLimits);

                    if (profile.getBuild().getExtStorages() != null && !profile.getBuild().getExtStorages().isEmpty())
                        jProfileProps.put("storages", profile.getBuild().getExtStorages());

                    var profile_evs = rp.getGraphhopper().getEncodingManager().getEncodedValues();
                    if (profile_evs != null && !profile_evs.isEmpty()) {
                        JSONArray jEVs = new JSONArray(profile_evs.stream().map(EncodedValue::getName).toArray());
                        jProfileProps.put("encoded_values",jEVs);
                    }
                    jProfiles.put(profile.getProfileName(), jProfileProps);
                }

                jInfo.put("profiles", jProfiles);
            }
        }

        String jsonResponse = constructResponse(request, jInfo);

        return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);
    }

    private String constructResponse(HttpServletRequest req, JSONObject json) {
        String type = getParam(req, "type", "json");
        boolean debug = getBooleanParam(req, "debug", false) || getBooleanParam(req, "pretty", false);
        if ("jsonp".equals(type)) {

            String callbackName = getParam(req, "callback", null);
            if (callbackName == null) {
                throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "No callback provided, necessary if type=jsonp");
            }

            if (debug) {
                return callbackName + "(" + json.toString(2) + ")";
            } else {
                return callbackName + "(" + json.toString() + ")";
            }
        } else {
            if (debug) {
                return json.toString(2);
            } else {
                return json.toString();
            }
        }
    }

    private String formatDateTime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
    }

    protected boolean getBooleanParam(HttpServletRequest req, String string, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(getParam(req, string, "" + defaultValue));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    protected String getParam(HttpServletRequest req, String string, String defaultValue) {
        String[] l = req.getParameterMap().get(string);
        if (l != null && l.length > 0)
            return l[0];

        return defaultValue;
    }
}
