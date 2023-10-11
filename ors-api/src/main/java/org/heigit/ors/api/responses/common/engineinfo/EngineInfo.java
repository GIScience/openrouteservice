package org.heigit.ors.api.responses.common.engineinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.json.JSONObject;

@Schema(description = "Information about the openrouteservice engine used")
public class EngineInfo {
    @Schema(description = "The backend version of the openrouteservice that was queried", example = "8.0")
    @JsonProperty("version")
    private final String version;
    @Schema(description = "The date that the service was last updated", example = "2019-02-07T14:28:11Z")
    @JsonProperty("build_date")
    private final String buildDate;
    @Schema(description = "The date that the graph data was last updated", example = "2019-02-07T14:28:11Z")
    @JsonProperty("graph_date")
    private String graphDate;

    public EngineInfo(JSONObject infoIn) {
        version = infoIn.getString("version");
        buildDate = infoIn.getString("build_date");
        graphDate = "0000-00-00T00:00:00Z";
    }

    public String getVersion() {
        return version;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setGraphDate(String graphDate) {
        this.graphDate = graphDate;
    }
}
