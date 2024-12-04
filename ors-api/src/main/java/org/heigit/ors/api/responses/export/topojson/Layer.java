
package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "geometries"
})
@Getter
@Builder
public class Layer implements Serializable {

    @JsonProperty("type")
    private String type;
    @JsonProperty("geometries")
    private List<Geometry> geometries;
}
