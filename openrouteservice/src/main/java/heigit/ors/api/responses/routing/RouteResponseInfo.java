package heigit.ors.api.responses.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.Helper;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.config.AppConfig;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.AppInfo;
import org.json.JSONObject;

public class RouteResponseInfo {
    @JsonProperty("attribution")
    private String attribution;
    @JsonProperty("osm_file_md5_hash")
    private String osmFileMD5Hash;
    @JsonProperty("service")
    private String service;
    @JsonProperty("timestamp")
    private long timeStamp;

    @JsonProperty("query")
    private RouteRequest request;

    @JsonProperty("engine")
    private EngineInfo engineInfo;

    public RouteResponseInfo(RouteRequest request) {
        service = "routing";
        timeStamp = System.currentTimeMillis();

        if(AppConfig.hasValidMD5Hash())
            osmFileMD5Hash = AppConfig.getMD5Hash();

        if (!Helper.isEmpty(RoutingServiceSettings.getAttribution()))
            attribution =  RoutingServiceSettings.getAttribution();

        engineInfo = new EngineInfo(AppInfo.getEngineInfo());

        this.request = request;
    }

    private class EngineInfo {
        @JsonProperty("version")
        private String version;
        @JsonProperty("build_date")
        private String buildDate;

        public EngineInfo(JSONObject infoIn) {
            version = infoIn.getString("version");
            buildDate = infoIn.getString("build_date");
        }

        public String getVersion() {
            return version;
        }

        public String getBuildDate() {
            return buildDate;
        }
    }
}
