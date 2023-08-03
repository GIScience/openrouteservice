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

package org.heigit.ors.api.responses.matrix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.Helper;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.util.AppInfo;
import org.heigit.ors.api.util.SystemMessage;
import org.heigit.ors.config.AppConfig;
import org.json.JSONObject;

@Schema(description = "Information about the request")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MatrixResponseInfo {
    @Schema(description = "ID of the request (as passed in by the query)", example = "request123")
    @JsonProperty("id")
    private String id;

    @Schema(description = "Copyright and attribution information", example = "openrouteservice.org, OpenStreetMap contributors")
    @JsonProperty("attribution")
    private String attribution;
    @Schema(description = "The MD5 hash of the OSM planet file that was used for generating graphs", example = "c0327ba6")
    @JsonProperty("osm_file_md5_hash")
    private String osmFileMD5Hash;
    @Schema(description = "The service that was requested", example = "matrix")
    @JsonProperty("service")
    private final String service;
    @Schema(description = "Time that the request was made (UNIX Epoch time)", example = "1549549847974")
    @JsonProperty("timestamp")
    private final long timeStamp;

    @Schema(description = "The information that was used for generating the matrix")
    @JsonProperty("query")
    private final MatrixRequest request;

    @Schema(description = "Information about the routing service")
    @JsonProperty("engine")
    private final EngineInfo engineInfo;

    @Schema(description = "System message", example = "A message string configured in the service")
    @JsonProperty("system_message")
    private final String systemMessage;

    public MatrixResponseInfo(MatrixRequest request, SystemMessageProperties systemMessageProperties, EndpointsProperties endpointsProperties) {
        service = "matrix";
        timeStamp = System.currentTimeMillis();

        if (AppConfig.hasValidMD5Hash())
            osmFileMD5Hash = AppConfig.getMD5Hash();

        if (!Helper.isEmpty(endpointsProperties.getMatrix().getAttribution()))
            attribution = endpointsProperties.getMatrix().getAttribution();

        if (request.hasId())
            id = request.getId();

        engineInfo = new EngineInfo(AppInfo.getEngineInfo());

        this.request = request;

        this.systemMessage = SystemMessage.getSystemMessage(request, systemMessageProperties);
    }

    @JsonIgnore
    public void setGraphDate(String graphDate) {
        engineInfo.setGraphDate(graphDate);
    }

    @Schema(description = "Information about the version of the openrouteservice that was used to generate the matrix")
    private static class EngineInfo {
        @Schema(description = "The backend version of the openrouteservice that was queried", example = "5.0")
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

    public String getAttribution() {
        return attribution;
    }

    public String getOsmFileMD5Hash() {
        return osmFileMD5Hash;
    }

    public String getService() {
        return service;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public MatrixRequest getRequest() {
        return request;
    }

    public EngineInfo getEngineInfo() {
        return engineInfo;
    }
}
