package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.routing.RouteStep;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class GPXRoutePointElement {
    @XmlAttribute(name = "lat")
    private double latitude;
    @XmlAttribute(name = "lon")
    private double longitude;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "desc")
    private String instructionDescription;
    @XmlElement(name = "ele")
    private double elevation;

    @XmlElement(name = "extensions")
    private GPXRoutePointExtensionElement element;

    public GPXRoutePointElement() { }

    public GPXRoutePointElement( RouteStep step, double longitude, double latitude, double elevation, int stepNumber) {
        this.latitude = latitude;
        this.longitude = longitude;
        if(!Double.isNaN(elevation))
            this.elevation = elevation;

        this.name = step.getName();
        this.instructionDescription = step.getInstruction();

        this.element = new GPXRoutePointExtensionElement(step, stepNumber);
    }
}
