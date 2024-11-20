
package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "geometries"
})
@Getter
public class Layer implements Serializable {

    @JsonProperty("type")
    public String type;
    @JsonProperty("geometries")
    private List<Geometry> geometries;

    public Layer withType(String type) {
        this.type = type;
        return this;
    }

    public Layer withGeometries(List<Geometry> geometries) {
        this.geometries = geometries;
        return this;
    }

}
