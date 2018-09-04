package heigit.ors.api.responses.routing.BoundingBox;

import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXBounds;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlSeeAlso(GPXBounds.class)
@XmlTransient
public interface BoundingBox {
    double getMinLon();
    double getMaxLon();
    double getMinLat();
    double getMaxLat();
}
