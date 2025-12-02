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
import org.heigit.ors.api.services.DynamicDataService;
import org.heigit.ors.api.services.EngineService;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.localization.LocalizationManager;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.util.AppInfo;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "Status service", description = "Get information on the status of the api")
@RequestMapping("/v2/status")
public class StatusAPI {

    private final EngineService engineService;
    private final EndpointsProperties endpointsProperties;
    DynamicDataService dynamicDataService;

    public StatusAPI(EngineService engineService, EndpointsProperties endpointsProperties, DynamicDataService dynamicDataService) {
        this.engineService = engineService;
        this.endpointsProperties = endpointsProperties;
        this.dynamicDataService = dynamicDataService;
    }

    @GetMapping
    public ResponseEntity<String> getStatus(HttpServletRequest request) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject jInfo = new JSONObject(true);
        jInfo.put("engine", AppInfo.getEngineInfo());

        RoutingProfileManager routingProfileManager = engineService.getRoutingProfileManager();
        if (routingProfileManager.isReady() && !routingProfileManager.getUniqueProfiles().isEmpty()) {
            addServicesInfo(jInfo);
            addLanguagesInfo(jInfo);
            addProfilesInfo(jInfo, routingProfileManager);
        }

        String jsonResponse = constructResponse(request, jInfo);
        return new ResponseEntity<>(jsonResponse, headers, HttpStatus.OK);
    }

    private void addServicesInfo(JSONObject jInfo) {
        List<String> services = new ArrayList<>(6);
        if (endpointsProperties.getRouting().isEnabled()) services.add("routing");
        if (endpointsProperties.getIsochrones().isEnabled()) services.add("isochrones");
        if (endpointsProperties.getMatrix().isEnabled()) services.add("matrix");
        if (endpointsProperties.getSnap().isEnabled()) services.add("snap");
        if (endpointsProperties.getExport().isEnabled()) services.add("export");
        if (endpointsProperties.getMatch().isEnabled()) services.add("match");
        jInfo.put("services", services);
    }

    private void addLanguagesInfo(JSONObject jInfo) throws Exception {
        jInfo.put("languages", LocalizationManager.getInstance().getLanguages());
    }

    private void addProfilesInfo(JSONObject jInfo, RoutingProfileManager profileManager) {
        JSONObject jProfiles = new JSONObject(true);
        String profileWithDynamicData = null;

        for (RoutingProfile rp : profileManager.getUniqueProfiles()) {
            JSONObject jProfileProps = createProfileProperties(rp);
            if (rp.hasDynamicData()) {
                jProfileProps.put("dynamic_data", rp.getDynamicDataStats());
                profileWithDynamicData = rp.getProfileConfiguration().getProfileName();
            }
            jProfiles.put(rp.getProfileConfiguration().getProfileName(), jProfileProps);
        }

        if (dynamicDataService.isEnabled() && profileWithDynamicData != null) {
            jInfo.put("dynamic_data_service", dynamicDataService.getFeatureStoreStats(profileWithDynamicData));
        }

        jInfo.put("profiles", jProfiles);
    }

    private JSONObject createProfileProperties(RoutingProfile rp) {
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
        if (profile.getBuild().getExtStorages() != null && !profile.getBuild().getExtStorages().isEmpty()) {
            jProfileProps.put("storages", profile.getBuild().getExtStorages());
        }
        var profileEVs = rp.getGraphhopper().getEncodingManager().getEncodedValues();
        if (profileEVs != null && !profileEVs.isEmpty()) {
            JSONArray jEVs = new JSONArray(profileEVs.stream().map(EncodedValue::getName).toArray());
            jProfileProps.put("encoded_values", jEVs);
        }

        return jProfileProps;
    }

    private String constructResponse(HttpServletRequest req, JSONObject json) {
        String type = getParam(req, "type", "json");
        boolean debug = getBooleanParam(req, "debug") || getBooleanParam(req, "pretty");
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

    protected boolean getBooleanParam(HttpServletRequest req, String string) {
        try {
            return Boolean.parseBoolean(getParam(req, string, "false"));
        } catch (Exception ex) {
            return false;
        }
    }

    protected String getParam(HttpServletRequest req, String parameterKey, String defaultValue) {
        String[] l = req.getParameterMap().get(parameterKey);
        if (l != null && l.length > 0)
            return l[0];
        return defaultValue;
    }
}
