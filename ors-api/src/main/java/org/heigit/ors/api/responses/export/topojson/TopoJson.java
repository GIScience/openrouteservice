package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "transform", "objects", "arcs", "bbox"})
@Getter
@Builder
public class TopoJson implements Serializable {

    @JsonProperty("type")
    @Builder.Default
    private String type = "Topology";
    @JsonProperty("objects")
    private Layers objects;
    @JsonProperty("arcs")
    private List<Arc> arcs;
    @JsonProperty("bbox")
    private List<Double> bbox;
}
