package org.heigit.ors.api.util;

import com.typesafe.config.ConfigObject;
import org.apache.log4j.Logger;
import org.heigit.ors.api.CorsProperties;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.util.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppConfigMigration {
    private static final Logger LOGGER = Logger.getLogger(AppConfigMigration.class.getName());
    public static final String SERVICE_NAME_ISOCHRONES = "isochrones";
    public static final String SERVICE_NAME_FASTISOCHRONES = "fastisochrones.";
    public static final String SERVICE_NAME_MATRIX = "matrix";
    public static final String SERVICE_NAME_ROUTING = "routing";
    private static AppConfig config = AppConfig.getGlobal();

    private AppConfigMigration() {
    }

    public static void loadSystemMessagesfromAppConfig(List<SystemMessage.Message> messages) {
        for (ConfigObject message : config.getObjectList("system_message")) {
            try {
                if (message.toConfig().getBoolean("active")) {
                    List<SystemMessage.Condition> conditions = new ArrayList<>();
                    loadConditionsForMessage(message, conditions);
                    messages.add(new SystemMessage.Message(message.toConfig().getString("text"), conditions));
                }
            } catch (Exception e) {
                // ignore otherwise incomplete messages entirely
                LOGGER.warn(String.format("Invalid SystemMessage object in ors config %s.", message.toString().substring(18)));
            }
        }
    }

    private static void loadConditionsForMessage(ConfigObject message, List<SystemMessage.Condition> conditions) {
        try {
            ConfigObject condition = message.toConfig().getObject("condition");
            for (String key : condition.keySet()) {
                conditions.add(new SystemMessage.Condition(key, condition.toConfig().getString(key)));
            }
        } catch (Exception e) {
            // ignore missing condition block and keep message
            LOGGER.info("Invalid or missing condition in message object.");
        }
    }

    public static CorsProperties overrideCorsProperties(CorsProperties cors) {

        List<String> allowedOrigins = config.getStringList("ors.api_settings.cors.allowed.origins");
        if (!allowedOrigins.isEmpty())
            cors.setAllowedOriginsList(allowedOrigins);

        List<String> allowedHeaders = config.getStringList("ors.api_settings.cors.allowed.headers");
        if (!allowedHeaders.isEmpty())
            cors.setAllowedHeadersList(allowedHeaders);

        double maxAge = config.getDouble("api_settings.cors.preflight_max_age");
        if (!Double.isNaN(maxAge))
            cors.setPreflightMaxAge((long) maxAge);

        return cors;
    }

    public static EndpointsProperties overrideEndpointsProperties(EndpointsProperties endpoints) {
        String swaggerDocumentationUrl = config.getParameter("info", "swagger_documentation_url");
        if (!StringUtility.isNullOrEmpty(swaggerDocumentationUrl))
            endpoints.setSwaggerDocumentationUrl(swaggerDocumentationUrl);

        EndpointsProperties.EndpointIsochroneProperties isochrones = endpoints.getIsochrone();
        String value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, "enabled");
        if (value != null)
            isochrones.setEnabled(Boolean.parseBoolean(value));
        value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, "maximum_locations");
        if (!StringUtility.isNullOrEmpty(value))
            isochrones.setMaximumLocations(Integer.parseInt(value));
        value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, "allow_compute_area");
        if (value != null)
            isochrones.setAllowComputeArea(Boolean.parseBoolean(value));
        value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, "maximum_intervals");
        if (value != null)
            isochrones.setMaximumIntervals(Integer.parseInt(value));
        value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, "attribution");
        if (value != null)
            isochrones.setAttribution(value);

        value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, "maximum_range_distance");
        if (value != null)
            isochrones.setMaximumRangeDistanceDefault(Integer.parseInt(value));
        else {
            List<? extends ConfigObject> params = config.getObjectList(SERVICE_NAME_ISOCHRONES, "maximum_range_distance");
            int def = parseProfileValues(params, isochrones.getProfileMaxRangeDistances());
            if (def != -1)
                isochrones.setMaximumRangeDistanceDefault(def);
        }

        value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, "maximum_range_time");
        if (value != null)
            isochrones.setMaximumRangeTimeDefault(Integer.parseInt(value));
        else {
            List<? extends ConfigObject> params = config.getObjectList(SERVICE_NAME_ISOCHRONES, "maximum_range_time");
            int def = parseProfileValues(params, isochrones.getProfileMaxRangeTimes());
            if (def != -1)
                isochrones.setMaximumRangeTimeDefault(def);
        }

        EndpointsProperties.MaximumRangeProperties fastisochrones = isochrones.getFastisochrones();

        value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, SERVICE_NAME_FASTISOCHRONES + "maximum_range_distance");
        if (value != null)
            fastisochrones.setMaximumRangeDistanceDefault(Integer.parseInt(value));
        else {
            List<? extends ConfigObject> params = config.getObjectList(SERVICE_NAME_ISOCHRONES, SERVICE_NAME_FASTISOCHRONES + "maximum_range_distance");
            int def = parseProfileValues(params, fastisochrones.getProfileMaxRangeDistances());
            if (def != -1)
                fastisochrones.setMaximumRangeDistanceDefault(def);
        }

        value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, SERVICE_NAME_FASTISOCHRONES + "maximum_range_time");
        if (value != null)
            fastisochrones.setMaximumRangeTimeDefault(Integer.parseInt(value));
        else {
            List<? extends ConfigObject> params = config.getObjectList(SERVICE_NAME_ISOCHRONES, SERVICE_NAME_FASTISOCHRONES + "maximum_range_time");
            int def = parseProfileValues(params, fastisochrones.getProfileMaxRangeTimes());
            if (def != -1)
                fastisochrones.setMaximumRangeTimeDefault(def);
        }

        EndpointsProperties.EndpointMatrixProperties matrix = endpoints.getMatrix();
        value = config.getServiceParameter(SERVICE_NAME_MATRIX, "enabled");
        if (value != null)
            matrix.setEnabled(Boolean.parseBoolean(value));
        value = config.getServiceParameter(SERVICE_NAME_MATRIX, "attribution");
        if (value != null)
            matrix.setAttribution(value);
        value = config.getServiceParameter(SERVICE_NAME_MATRIX, "maximum_search_radius");
        if (value != null)
            matrix.setMaximumSearchRadius(Math.max(1, Double.parseDouble(value)));
        value = config.getServiceParameter(SERVICE_NAME_MATRIX, "maximum_visited_nodes");
        if (value != null)
            matrix.setMaximumVisitedNodes(Math.max(1, Integer.parseInt(value)));
        value = config.getServiceParameter(SERVICE_NAME_MATRIX, "u_turn_cost");
        if (value != null && Double.parseDouble(value) != -1.0)
            matrix.setUTurnCost(Double.parseDouble(value));
        value = config.getServiceParameter(SERVICE_NAME_MATRIX, "maximum_routes");
        if (value != null)
            matrix.setMaximumRoutes(Math.max(1, Integer.parseInt(value)));
        value = config.getServiceParameter(SERVICE_NAME_MATRIX, "maximum_routes_flexible");
        if (value != null)
            matrix.setMaximumRoutesFlexible(Math.max(1, Integer.parseInt(value)));

        EndpointsProperties.EndpointRoutingProperties routing = endpoints.getRouting();
        value = config.getServiceParameter(SERVICE_NAME_ROUTING, "enabled");
        if (value != null)
            routing.setEnabled(Boolean.parseBoolean(value));
        value = config.getServiceParameter(SERVICE_NAME_ROUTING, "attribution");
        if (value != null)
            routing.setAttribution(value);
        String baseUrl = config.getParameter("info", "base_url");
        if (!StringUtility.isNullOrEmpty(baseUrl))
            routing.setGpxBaseUrl(baseUrl);
        String supportMail = config.getParameter("info", "support_mail");
        if (!StringUtility.isNullOrEmpty(supportMail))
            routing.setGpxSupportMail(supportMail);
        String authorTag = config.getParameter("info", "author_tag");
        if (!StringUtility.isNullOrEmpty(authorTag))
            routing.setGpxAuthor(authorTag);
        String contentLicence = config.getParameter("info", "content_licence");
        if (!StringUtility.isNullOrEmpty(contentLicence))
            routing.setGpxContentLicence(contentLicence);
        value = config.getServiceParameter(SERVICE_NAME_ROUTING, "routing_name");
        if (value != null)
            routing.setGpxName(value);

        return endpoints;
    }

    private static int parseProfileValues(List<? extends ConfigObject> params, Map<Integer, Integer> map) {
        int def = -1;
        for (ConfigObject cfgObj : params) {
            if (cfgObj.containsKey("profiles") && cfgObj.containsKey("value")) {
                String[] profiles = cfgObj.toConfig().getString("profiles").split(",");
                int value = cfgObj.toConfig().getInt("value");
                for (String profileStr : profiles) {
                    profileStr = profileStr.trim();
                    if ("any".equalsIgnoreCase(profileStr)) {
                        def = value;
                    }
                    else {
                        Integer profile = RoutingProfileType.getFromString(profileStr);
                        if (profile != RoutingProfileType.UNKNOWN)
                            map.put(profile, value);
                    }
                }
            }
        }
        return def;
    }
}
