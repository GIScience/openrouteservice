package org.heigit.ors.api.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.heigit.ors.api.config.SystemMessageProperties;
import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.RoutingRequest;
import org.heigit.ors.routing.WeightingMethod;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

public class SystemMessage {
    private static final Logger LOGGER = Logger.getLogger(SystemMessage.class.getName());
    private static List<Message> messages;

    private SystemMessage() {
    }

    public static String getSystemMessage(Object requestObj, SystemMessageProperties messageProperties) {
        if (messages == null) {
            loadMessages(messageProperties);
        }
        if (messages.isEmpty()) {
            return "";
        }
        if (requestObj == null) {
            requestObj = "";
        }
        RequestParams params = new RequestParams();
        // V1
        if (requestObj.getClass() == RoutingRequest.class) {
            extractParams((RoutingRequest) requestObj, params);
        } else if (requestObj.getClass() == org.heigit.ors.matrix.MatrixRequest.class) {
            extractParams((org.heigit.ors.matrix.MatrixRequest) requestObj, params);
        } else if (requestObj.getClass() == org.heigit.ors.isochrones.IsochroneRequest.class) {
            extractParams((org.heigit.ors.isochrones.IsochroneRequest) requestObj, params);
        }
        // V2
        else if (requestObj.getClass() == RouteRequest.class) {
            extractParams((RouteRequest) requestObj, params);
        } else if (requestObj.getClass() == MatrixRequest.class) {
            extractParams((MatrixRequest) requestObj, params);
        } else if (requestObj.getClass() == IsochronesRequest.class) {
            extractParams((IsochronesRequest) requestObj, params);
        }
        return selectMessage(params);
    }

    private static void extractParams(RoutingRequest req, RequestParams params) {
        params.setApiVersion("1");
        params.setApiFormat(req.getResponseFormat());
        params.setRequestService("routing");
        params.setRequestProfiles(RoutingProfileType.getName(req.getSearchParameters().getProfileType()));
        params.setRequestPreferences(WeightingMethod.getName(req.getSearchParameters().getWeightingMethod()));
    }

    private static void extractParams(org.heigit.ors.matrix.MatrixRequest req, RequestParams params) {
        params.setApiVersion("1");
        params.setApiFormat("json");
        params.setRequestService("matrix");
        params.setRequestProfiles(RoutingProfileType.getName(req.getProfileType()));
        params.setRequestPreference(MatrixMetricsType.getMetricsNamesFromInt(req.getMetrics()));
    }

    private static void extractParams(org.heigit.ors.isochrones.IsochroneRequest req, RequestParams params) {
        params.setApiVersion("1");
        params.setApiFormat("geojson");
        params.setRequestService("isochrones");
        params.setRequestProfile(req.getProfilesForAllTravellers());
        params.setRequestPreference(req.getWeightingsForAllTravellers());
    }

    private static void extractParams(RouteRequest req, RequestParams params) {
        params.setApiVersion("2");
        params.setApiFormat(req.getResponseType().toString());
        params.setRequestService("routing");
        params.setRequestProfiles(req.getProfile().toString());
        params.setRequestPreferences(req.hasRoutePreference() ? req.getRoutePreference().toString() : "");
    }

    private static void extractParams(MatrixRequest req, RequestParams params) {
        params.setApiVersion("2");
        params.setApiFormat("json");
        params.setRequestService("matrix");
        params.setRequestProfiles(req.getProfile() != null ? req.getProfile().toString() : "driving-car");
        params.setRequestPreference(req.getMetricsStrings().isEmpty() ? new HashSet<>(List.of("duration")) : req.getMetricsStrings());
    }

    private static void extractParams(IsochronesRequest req, RequestParams params) {
        params.setApiVersion("2");
        params.setApiFormat("geojson");
        params.setRequestService("isochrones");
        params.setRequestProfiles(req.getProfile() != null ? req.getProfile().toString() : "driving-car");
        params.setRequestPreferences(req.hasRangeType() ? req.getRangeType().name() : "TIME");
    }

    private static String selectMessage(RequestParams params) {
        for (Message message : messages) {
            if (message.applicableForRequest(params)) {
                return message.getText();
            }
        }
        return "";
    }

    private static void loadMessages(SystemMessageProperties messageProperties) {
        messages = new ArrayList<>();

        for (SystemMessageProperties.MessageObject message : messageProperties) {
            try {
                if (message.isActive()) {
                    List<SystemMessage.Condition> conditions = new ArrayList<>();
                    loadConditionsForMessage(message, conditions);
                    messages.add(new SystemMessage.Message(message.getText(), conditions));
                }
            } catch (Exception e) {
                // ignore otherwise incomplete messages entirely
                LOGGER.warn("Invalid SystemMessage object in ors config %s.".formatted(message.toString().substring(18)));
            }
        }
        if (!messages.isEmpty())
            LOGGER.info("SystemMessage loaded %s messages.".formatted(messages.size()));
    }

    private static void loadConditionsForMessage(SystemMessageProperties.MessageObject message, List<Condition> conditions) {
        try {
            for (Map<String, String> conditionMap : message.getCondition()) {
                for (Map.Entry<String, String> condition : conditionMap.entrySet()) {
                    conditions.add(new Condition(condition.getKey(), condition.getValue()));
                }
            }
        } catch (Exception e) {
            // ignore missing condition block and keep message
            LOGGER.info("Invalid or missing condition in message object.");
        }
    }

    static class Message {
        @Getter
        private final String text;
        private final List<Condition> conditions;

        public Message(String text, List<Condition> conditions) {
            this.text = text;
            this.conditions = conditions;
        }

        public boolean applicableForRequest(RequestParams params) {
            for (Condition condition : conditions) {
                if (!condition.fulfilledBy(params)) {
                    return false;
                }
            }
            return true;
        }
    }

    static class Condition {
        private final String type;
        private final String[] values;

        public Condition(String type, String valuesCSV) {
            this.type = type;
            this.values = valuesCSV.split(",");
        }

        public boolean fulfilledBy(RequestParams params) {
            switch (type) {
                case "time_before" -> {
                    return new Timestamp(System.currentTimeMillis()).getTime() < Date.from(Instant.parse(this.values[0])).getTime();
                }
                case "time_after" -> {
                    return new Timestamp(System.currentTimeMillis()).getTime() > Date.from(Instant.parse(this.values[0])).getTime();
                }
                case "api_version" -> {
                    return matchApiVersion(params);
                }
                case "api_format" -> {
                    return matchApiFormat(params);
                }
                case "request_service" -> {
                    return matchRequestService(params);
                }
                case "request_profile" -> {
                    return matchRequestProfiles(params);
                }
                case "request_preference" -> {
                    return matchRequestPreferences(params);
                }
                default -> // unknown rule
                        LOGGER.warn("Invalid condition set in system_message.");
            }
            return false;
        }

        private boolean matchApiVersion(RequestParams params) {
            for (String val : this.values) {
                if (params.getApiVersion().equalsIgnoreCase(val)) return true;
            }
            return false;
        }

        private boolean matchApiFormat(RequestParams params) {
            for (String val : this.values) {
                if (params.getApiFormat().equalsIgnoreCase(val)) return true;
            }
            return false;
        }

        private boolean matchRequestService(RequestParams params) {
            for (String val : this.values) {
                if (params.getRequestService().equalsIgnoreCase(val)) return true;
            }
            return false;
        }

        private boolean matchRequestProfiles(RequestParams params) {
            for (String val : this.values) {
                for (String param : params.getRequestProfiles()) {
                    if (param.equalsIgnoreCase(val)) return true;
                }
            }
            return false;
        }

        private boolean matchRequestPreferences(RequestParams params) {
            for (String val : this.values) {
                for (String param : params.getRequestPreferences()) {
                    if (param.equalsIgnoreCase(val)) return true;
                }
            }
            return false;
        }
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    protected static class RequestParams {
        private String apiVersion = "";
        private String apiFormat = "";
        private String requestService = "";
        @Setter(AccessLevel.NONE)
        private final Set<String> requestProfiles = new HashSet<>();
        @Setter(AccessLevel.NONE)
        private final Set<String> requestPreferences = new HashSet<>();

        public void setRequestProfiles(String requestProfiles) {
            this.requestProfiles.add(requestProfiles);
        }

        public void setRequestProfile(Set<String> requestProfile) {
            this.requestProfiles.addAll(requestProfile);
        }

        public void setRequestPreferences(String requestPreferences) {
            this.requestPreferences.add(requestPreferences);
        }

        public void setRequestPreference(Set<String> requestPreference) {
            this.requestPreferences.addAll(requestPreference);
        }
    }
}
