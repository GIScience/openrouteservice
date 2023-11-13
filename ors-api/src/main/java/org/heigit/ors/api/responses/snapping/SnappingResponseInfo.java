package org.heigit.ors.api.responses.snapping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.Helper;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.requests.snapping.SnappingApiRequest;
import org.heigit.ors.api.responses.common.engineinfo.EngineInfo;
import org.heigit.ors.api.util.AppInfo;
import org.heigit.ors.api.util.SystemMessage;
import org.heigit.ors.config.AppConfig;

@Schema(description = "Information about the request")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class SnappingResponseInfo {
    @Schema(description = "Copyright and attribution information", example = "openrouteservice.org | OpenStreetMap contributors")
    @JsonProperty("attribution")
    private String attribution;
    @Schema(description = "The MD5 hash of the OSM planet file that was used for generating graphs", example = "c0327ba6")
    @JsonProperty("osm_file_md5_hash")
    private String osmFileMD5Hash;
    @Schema(description = "The service that was requested", example = "snap")
    @JsonProperty("service")
    private final String service;
    @Schema(description = "Time that the request was made (UNIX Epoch time)", example = "1549549847974")
    @JsonProperty("timestamp")
    private final long timeStamp;

    @Schema(description = "The information that was used for generating the request")
    @JsonProperty("query")
    private final SnappingApiRequest request;

    @Schema(description = "Information about the snapping service")
    @JsonProperty("engine")
    private final EngineInfo engineInfo;

    @Schema(description = "System message", example = "A message string configured in the service")
    @JsonProperty("system_message")
    private final String systemMessage;

    public SnappingResponseInfo(SnappingApiRequest request, SystemMessageProperties systemMessageProperties, EndpointsProperties endpointsProperties) {
        service = "snap";
        timeStamp = System.currentTimeMillis();

        if (AppConfig.hasValidMD5Hash())
            osmFileMD5Hash = AppConfig.getMD5Hash();

        if (!Helper.isEmpty(endpointsProperties.getSnap().getAttribution()))
            attribution = endpointsProperties.getSnap().getAttribution();

        engineInfo = new EngineInfo(AppInfo.getEngineInfo());

        this.request = request;

        this.systemMessage = SystemMessage.getSystemMessage(request, systemMessageProperties);
    }

    @JsonIgnore
    public void setGraphDate(String graphDate) {
        engineInfo.setGraphDate(graphDate);
    }

}
