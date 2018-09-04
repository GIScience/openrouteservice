package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.routing.RouteStep;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "extensions")
public class GPXRoutePointExtensionElement {
    @XmlElement(name = "distance")
    private double distance;
    @XmlElement(name="duration")
    private double duration;
    @XmlElement(name = "type")
    private int type;
    @XmlElement(name = "step")
    private int step;

    public GPXRoutePointExtensionElement() {

    }

    public GPXRoutePointExtensionElement(RouteStep step, int stepNumber) {
        distance = step.getDistance();
        duration = step.getDuration();
        type = step.getType();
        this.step = stepNumber;
    }
}
