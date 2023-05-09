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

package org.heigit.ors.api.responses.isochrones;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.Helper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.util.SystemMessage;
import org.heigit.ors.config.AppConfig;
import org.heigit.ors.config.IsochronesServiceSettings;
import org.heigit.ors.util.AppInfo;
import org.json.JSONObject;

@ApiModel(value = "IsochronesResponseInfo", description = "Information about the request")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IsochronesResponseInfo {
    @ApiModelProperty(value = "ID of the request (as passed in by the query)", example = "request123")
    @JsonProperty("id")
    private String id;

    @ApiModelProperty(value = "Copyright and attribution information", example = "openrouteservice.org | OpenStreetMap contributors")
    @JsonProperty("attribution")
    private String attribution;
    @ApiModelProperty(value = "The MD5 hash of the OSM planet file that was used for generating graphs", example = "c0327ba6")
    @JsonProperty("osm_file_md5_hash")
    private String osmFileMD5Hash;
    @ApiModelProperty(value = "The service that was requested", example = "isochrones")
    @JsonProperty("service")
    private final String service;
    @ApiModelProperty(value = "Time that the request was made (UNIX Epoch time)", example = "1549549847974")
    @JsonProperty("timestamp")
    private final long timeStamp;

    @ApiModelProperty(value = "The information that was used for generating the isochrones")
    @JsonProperty("query")
    private final IsochronesRequest request;

    @ApiModelProperty(value = "Information about the isochrones service")
    @JsonProperty("engine")
    private final EngineInfo engineInfo;

    @ApiModelProperty(value = "System message", example ="A message string configured in the service")
    @JsonProperty("system_message")
    private final String systemMessage;

    public IsochronesResponseInfo(IsochronesRequest request) {
        service = "isochrones";
        timeStamp = System.currentTimeMillis();

        if (AppConfig.hasValidMD5Hash())
            osmFileMD5Hash = AppConfig.getMD5Hash();

        if (!Helper.isEmpty(IsochronesServiceSettings.getAttribution()))
            attribution = IsochronesServiceSettings.getAttribution();

        if (request.hasId())
            id = request.getId();

        engineInfo = new EngineInfo(AppInfo.getEngineInfo());

        this.request = request;

        this.systemMessage = SystemMessage.getSystemMessage(request);
    }

    public void setGraphDate(String graphDate) {
        engineInfo.setGraphDate(graphDate);
    }

    @ApiModel(description = "Information about the version of the openrouteservice that was used to generate the isochrones")
    private class EngineInfo {
        @ApiModelProperty(value = "The backend version of the openrouteservice that was queried", example = "5.0")
        @JsonProperty("version")
        private final String version;
        @ApiModelProperty(value = "The date that the service was last updated", example = "2019-02-07T14:28:11Z")
        @JsonProperty("build_date")
        private final String buildDate;
        @ApiModelProperty(value = "The date that the graph data was last updated", example = "2019-02-07T14:28:11Z")
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
}
