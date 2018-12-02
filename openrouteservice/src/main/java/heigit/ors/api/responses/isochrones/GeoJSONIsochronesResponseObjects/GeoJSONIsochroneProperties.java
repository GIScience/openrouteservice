package heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.AttributeValue;
import heigit.ors.isochrones.Isochrone;

public class GeoJSONIsochroneProperties {

    @JsonProperty("group_index")
    private int group_index;

    @JsonProperty("value")
    private double value;

    @JsonProperty("center")
    private Double[] center;

    @JsonProperty("area")
    private double area;

    @JsonProperty("reachfactor")
    private double reachfactor;

    @JsonProperty("total_pop")
    private AttributeValue total_pop;


    public GeoJSONIsochroneProperties(Isochrone isochrone, Coordinate center, int group_index) {
        this.group_index = group_index;
        if (isochrone.hasArea())
            this.area = isochrone.getArea();
        this.value = isochrone.getValue();
        this.center = new Double[]{center.x, center.y};
        if (isochrone.hasReachfactor())
            this.reachfactor = isochrone.getReachfactor();
        if (isochrone.getAttributes() != null && isochrone.getAttributes().size() > 0)
            for (AttributeValue attributeValue : isochrone.getAttributes()) {
                if (attributeValue.getName().toLowerCase().equals("total_pop"))
                    this.total_pop = attributeValue;
            }
    }

    public int getGroup_index() {
        return group_index;
    }

    public double getValue() {
        return value;
    }

    public Double[] getCenter() {
        return center;
    }

    public double getArea() {
        return area;
    }

    public double getReachfactor() {
        return reachfactor;
    }

    public AttributeValue getTotal_pop() {
        return total_pop;
    }
}
