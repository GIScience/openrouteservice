package org.heigit.ors.api.responses.export;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.export.ExportResult;

//TODO Refactoring: should this include ExportResponseInfo, as does RouteResponse?
public class ExportResponse {
    @JsonIgnore
    protected BoundingBox bbox;

    @JsonIgnore
    protected ExportResult exportResults;

    public ExportResponse() {};

    // In RouteResponse, this method was used to get metadata from RouteRequest.
    public ExportResponse(ExportResult result) {
        this.exportResults = result;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public ExportResult getExportResults() {
        return exportResults;
    }
}
