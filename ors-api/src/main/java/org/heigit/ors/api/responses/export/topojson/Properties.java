package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import org.heigit.ors.api.responses.export.json.WeightSerializer;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class Properties implements Serializable {
    @JsonProperty("weight")
    @JsonSerialize(using = WeightSerializer.class)
    private Double weight;
    @JsonProperty("node_from")
    private Long nodeFrom;
    @JsonProperty("node_to")
    private Long nodeTo;
    @JsonProperty("osm_id")
    private Long osmId;
    @JsonProperty("both_directions")
    private Boolean bothDirections;
    @JsonProperty("speed")
    private Double speed;
    @JsonProperty("speed_reverse")
    private Double speedReverse;
    @JsonProperty("ors_ids")
    private List<Integer> orsIds;
    @JsonProperty("ors_nodes")
    private List<Integer> orsNodes;
    @JsonProperty("distances")
    private List<Double> distances;
}
