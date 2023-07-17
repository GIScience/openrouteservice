package org.heigit.ors.api.util;

import com.typesafe.config.ConfigObject;
import org.apache.log4j.Logger;
import org.heigit.ors.api.CorsProperties;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

public class AppConfigMigration {
    private static final Logger LOGGER = Logger.getLogger(AppConfigMigration.class.getName());
    public static final String SERVICE_NAME_ISOCHRONES = "isochrones";
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
                LOGGER.warn("Invalid SystemMessage object in ors config %s.".formatted(message.toString().substring(18)));
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
}
