package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxBase;

import javax.xml.bind.annotation.XmlAttribute;

public class GPXBounds extends BoundingBoxBase implements BoundingBox {
    public GPXBounds() {
        super();
    }

    public GPXBounds(BBox bounding) {
        super(bounding);
    }

    @XmlAttribute(name = "minLat")
    public double getMinLat() {
        return this.minLat;
    }
    @XmlAttribute(name = "minLon")
    public double getMinLon() {
        return this.minLon;
    }
    @XmlAttribute(name = "maxLat")
    public double getMaxLat() {
        return this.maxLat;
    }
    @XmlAttribute(name = "maxLon")
    public double getMaxLon() {
        return this.maxLon;
    }
}
