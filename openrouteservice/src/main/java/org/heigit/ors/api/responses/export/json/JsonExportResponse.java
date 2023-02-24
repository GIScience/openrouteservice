package org.heigit.ors.api.responses.export.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;
import io.swagger.annotations.ApiModel;
import org.heigit.ors.api.responses.export.ExportResponse;
import org.heigit.ors.api.responses.routing.json.JSONWarning;
import org.heigit.ors.export.ExportResult;
import org.heigit.ors.common.Pair;
import org.heigit.ors.export.ExportWarning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiModel(description = "The Export Response contains nodes and edge weights from the requested BBox")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonExportResponse extends ExportResponse {

    @JsonProperty("nodes")
    public List<JsonNode> nodes;

    @JsonProperty("edges")
    public List<JsonEdge> edges;

    @JsonProperty("edges_extra")
    public List<JsonEdgeExtra> edgesExtra;

    @JsonProperty("warning")
    public JSONWarning warning;

    @JsonProperty("nodes_count")
    public Long nodesCount;

    @JsonProperty("edges_count")
    public Long edgesCount;

    public JsonExportResponse(ExportResult exportResult) {
        super(exportResult);

        nodes = new ArrayList<>();
        for (Map.Entry<Integer, Coordinate> location : exportResult.getLocations().entrySet()) {
            nodes.add(new JsonNode(location));
        }
        nodesCount = nodes.stream().count();

        edges = new ArrayList<>();
        for (Map.Entry<Pair<Integer, Integer>, Double> edgeWeight : exportResult.getEdgeWeigths().entrySet()) {
            edges.add(new JsonEdge(edgeWeight));
        }
        edgesCount = edges.stream().count();

        if (exportResult.hasEdgeExtras()) {
            edgesExtra = new ArrayList<>();
            for (Map.Entry<Pair<Integer, Integer>, Map<String, Object>> edge : exportResult.getEdgeExtras().entrySet()) {
                edgesExtra.add(new JsonEdgeExtra(edge));
            }
        }

        if (exportResult.hasWarning()) {
            ExportWarning warning = exportResult.getWarning();
            this.warning = new JSONWarning(warning.getWarningCode(), warning.getWarningMessage());
        }
    }
}
