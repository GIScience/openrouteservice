package heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.AttributeValue;
import heigit.ors.isochrones.Isochrone;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GeoJSONIsochroneProperties {

    @JsonProperty(value = "group_index")
    private Integer groupIndex;

    @JsonProperty("value")
    private Double value;

    @JsonProperty("center")
    private Double[] center;

    @JsonProperty("area")
    private Double area;

    @JsonProperty("reachfactor")
    private Double reachfactor;

    @JsonProperty(value = "total_pop")
    private Double totalPop;


    public GeoJSONIsochroneProperties(Isochrone isochrone, Coordinate center, int group_index) {
        this.groupIndex = group_index;
        if (isochrone.hasArea())
            this.area = isochrone.getArea();
        this.value = isochrone.getValue();
        this.center = new Double[]{center.x, center.y};
        if (isochrone.hasReachfactor())
            this.reachfactor = isochrone.getReachfactor();
        if (isochrone.getAttributes() != null && isochrone.getAttributes().size() > 0)
            for (AttributeValue attributeValue : isochrone.getAttributes()) {
                if (attributeValue.getName().toLowerCase().equals("total_pop"))
                    this.totalPop = attributeValue.getValue();
            }
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public Double getValue() {
        return value;
    }

    public Double[] getCenter() {
        return center;
    }

    public Double getArea() {
        return area;
    }

    public Double getReachfactor() {
        return reachfactor;
    }

    public Double getTotalPop() {
        return totalPop;
    }
}
