package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.config.AppConfig;

import javax.xml.bind.annotation.XmlElement;

public class GPXAuthor {
    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "email")
    private GPXEmail email;

    @XmlElement(name = "link")
    private GPXLink link;

    public GPXAuthor() {
        this.name = AppConfig.Global().getParameter("info", "author_tag");
        this.email = new GPXEmail();
        this.link = new GPXLink();
    }
}
