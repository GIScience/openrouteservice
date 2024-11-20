
package org.heigit.ors.api.responses.export.topojson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "layer"
})
public class Objects implements Serializable {

    @JsonProperty("layer")
    public Layer layer;

    @JsonIgnore

    public Objects withLayer(Layer layer) {
        this.layer = layer;
        return this;
    }
}
