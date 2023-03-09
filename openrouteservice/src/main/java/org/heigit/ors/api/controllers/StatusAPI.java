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

import com.graphhopper.storage.StorableProperties;
import org.heigit.ors.kafka.ORSKafkaConsumer;
import org.heigit.ors.localization.LocalizationManager;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.heigit.ors.config.IsochronesServiceSettings;
import org.heigit.ors.config.MapMatchingServiceSettings;
import org.heigit.ors.config.MatrixServiceSettings;
import org.heigit.ors.config.RoutingServiceSettings;
import org.heigit.ors.util.AppInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v2/status")
public class StatusAPI {
    @GetMapping
    public ResponseEntity fetchHealth(HttpServletRequest request) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        org.json.JSONObject jInfo = new org.json.JSONObject(true);

        jInfo.put("engine", AppInfo.getEngineInfo());

        if (RoutingProfileManagerStatus.isReady()) {
            RoutingProfileManager profileManager = RoutingProfileManager.getInstance();

            if (!profileManager.getProfiles().getUniqueProfiles().isEmpty()) {

                List<String> list = new ArrayList<>(4);
                if (RoutingServiceSettings.getEnabled())
                    list.add("routing");
                if (IsochronesServiceSettings.getEnabled())
                    list.add("isochrones");
                if (MatrixServiceSettings.getEnabled())
                    list.add("matrix");
                if (MapMatchingServiceSettings.getEnabled())
                    list.add("mapmatching");
                jInfo.put("services", list);
                jInfo.put("languages", LocalizationManager.getInstance().getLanguages());

                if (profileManager.updateEnabled()) {
                    jInfo.put("next_update", formatDateTime(profileManager.getNextUpdateTime()));
                    String status = profileManager.getUpdatedStatus();
                    if (status != null)
                        jInfo.put("update_status", status);
                }

                org.json.JSONObject jProfiles = new org.json.JSONObject(true);
                int i = 1;

                for (RoutingProfile rp : profileManager.getProfiles().getUniqueProfiles()) {
                    RouteProfileConfiguration rpc = rp.getConfiguration();
                    org.json.JSONObject jProfileProps = new org.json.JSONObject(true);

                    jProfileProps.put("profiles", rpc.getProfiles());
                    StorableProperties storageProps = rp.getGraphProperties();
                    jProfileProps.put("creation_date", storageProps.get("osmreader.import.date"));

                    if (rpc.getExtStorages() != null && rpc.getExtStorages().size() > 0)
                        jProfileProps.put("storages", rpc.getExtStorages());

                    org.json.JSONObject jProfileLimits = new org.json.JSONObject(true);
                    if (rpc.getMaximumDistance() > 0)
                        jProfileLimits.put("maximum_distance", rpc.getMaximumDistance());

                    if (rpc.getMaximumDistanceDynamicWeights() > 0)
                        jProfileLimits.put("maximum_distance_dynamic_weights", rpc.getMaximumDistanceDynamicWeights());

                    if (rpc.getMaximumDistanceAvoidAreas() > 0)
                        jProfileLimits.put("maximum_distance_avoid_areas", rpc.getMaximumDistanceAvoidAreas());

                    if (rpc.getMaximumWayPoints() > 0)
                        jProfileLimits.put("maximum_waypoints", rpc.getMaximumWayPoints());

                    if (jProfileLimits.length() > 0)
                        jProfileProps.put("limits", jProfileLimits);

                    jProfiles.put("profile " + i, jProfileProps);

                    i++;
                }

                jInfo.put("profiles", jProfiles);
            }

            if (ORSKafkaConsumer.isEnabled()) {
                org.json.JSONObject jKafka = new org.json.JSONObject(true);
                jKafka.put("runners", ORSKafkaConsumer.getEnabledRunners());
                jKafka.put("processed", profileManager.getKafkaMessagesProcessed());
                jKafka.put("failed", profileManager.getKafkaMessagesFailed());
                jInfo.put("kafkaConsumer", jKafka);
            }
        }

        String jsonResponse = constructResponse(request, jInfo);

        return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);
    }

    private String constructResponse(HttpServletRequest req, org.json.JSONObject json) {
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
