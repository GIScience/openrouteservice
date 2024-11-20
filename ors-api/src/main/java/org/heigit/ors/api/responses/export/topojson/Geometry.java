package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "properties", "arcs"})
@Getter
@Builder
public class Geometry implements Serializable {

    @JsonProperty("type")
    private String type;
    @JsonProperty("properties")
    @JsonSerialize(using = TopoJsonPropertiesSerializer.class)
    private transient Map<String, Object> properties;
    @JsonProperty("arcs")
    private List<Integer> arcs;

}
