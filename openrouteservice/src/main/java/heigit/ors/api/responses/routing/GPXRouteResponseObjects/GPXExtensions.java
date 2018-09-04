package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.api.requests.routing.RouteRequest;

import javax.xml.bind.annotation.XmlElement;

public class GPXExtensions {
    @XmlElement(name = "attribution")
    private String attribution;
    @XmlElement(name = "engine")
    private String engine;
    @XmlElement(name = "build_date")
    private String buildDate;
    @XmlElement(name = "profile")
    private String profile;
    @XmlElement(name="preference")
    private String preference;
    @XmlElement(name = "language")
    private String language;
    @XmlElement(name = "distance-units")
    private String units;
    @XmlElement(name = "instructions")
    private boolean includeInstructions;
    @XmlElement(name = "elevation")
    private boolean includeElevation;

    public GPXExtensions() {}

    public GPXExtensions(RouteRequest request) {

    }
}
