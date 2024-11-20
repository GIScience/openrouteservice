package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "transform", "objects", "arcs", "bbox"})
@Getter
@Setter
public class TopoJson implements Serializable {

    @JsonProperty("type")
    private String type;
    @JsonProperty("objects")
    private Objects objects;
    @JsonProperty("arcs")
    private List<List<List<Double>>> arcs;
    @JsonProperty("bbox")
    private List<Double> bbox;

    public TopoJson withType(String type) {
        this.type = type;
        return this;
    }

    public TopoJson withObjects(Objects objects) {
        this.objects = objects;
        return this;
    }

    public TopoJson withArcs(List<List<List<Double>>> arcs) {
        this.arcs = arcs;
        return this;
    }

    public TopoJson withBbox(List<Double> bbox) {
        this.bbox = bbox;
        return this;
    }
}
